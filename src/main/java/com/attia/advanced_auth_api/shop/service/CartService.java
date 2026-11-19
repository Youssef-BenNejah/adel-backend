package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.document.Pack;
import com.attia.advanced_auth_api.shop.document.Product;
import com.attia.advanced_auth_api.shop.document.Promotion;
import com.attia.advanced_auth_api.shop.document.Size;
import com.attia.advanced_auth_api.shop.dto.CartDtos;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * backend-api-spec.md §10 — owns cart/checkout pricing server-side so totals can never be
 * tampered with. {@code POST /orders} reuses {@link #quote} verbatim rather than duplicating it.
 */
@Service
public class CartService {
	private final ProductService productService;
	private final PackService packService;
	private final PromotionService promotionService;
	private final PricingService pricingService;
	private final SettingsService settingsService;

	public CartService(
			ProductService productService,
			PackService packService,
			PromotionService promotionService,
			PricingService pricingService,
			SettingsService settingsService) {
		this.productService = productService;
		this.packService = packService;
		this.promotionService = promotionService;
		this.pricingService = pricingService;
		this.settingsService = settingsService;
	}

	public CartDtos.QuoteResponse quote(CartDtos.QuoteRequest request) {
		List<Promotion> activePromotions = promotionService.listActive();
		List<CartDtos.Line> lines = new ArrayList<>();

		for (CartDtos.ItemRequest item : request.items() == null ? List.<CartDtos.ItemRequest>of() : request.items()) {
			if (item.packId() != null) {
				lines.add(buildPackLine(item));
			} else {
				lines.add(buildProductLine(item, activePromotions));
			}
		}

		int subtotal = lines.stream().mapToInt(CartDtos.Line::getLineTotal).sum();

		CartDtos.AppliedPromo appliedPromo = null;
		int discount = 0;
		if (request.promoCode() != null && !request.promoCode().isBlank()) {
			List<CartDtos.ValidateCodeLine> validateLines = lines.stream()
					.map(l -> new CartDtos.ValidateCodeLine(l.getId(), l.getUnitPrice(), l.getQty()))
					.toList();
			CartDtos.ValidateCodeResponse result = promotionService.validateCode(request.promoCode(), validateLines);
			if (result.getPromo() instanceof Promotion promo) {
				discount = result.getDiscount();
				appliedPromo = CartDtos.AppliedPromo.builder().code(promo.getCode()).discount(discount).build();
			}
		}

		int shipping = settingsService.resolveShipping(subtotal);
		int total = subtotal - discount + shipping;

		return CartDtos.QuoteResponse.builder()
				.lines(lines)
				.subtotal(subtotal)
				.appliedPromo(appliedPromo)
				.discount(discount)
				.shipping(shipping)
				.total(total)
				.build();
	}

	private CartDtos.Line buildProductLine(CartDtos.ItemRequest item, List<Promotion> activePromotions) {
		if (item.productId() == null || item.sizeMl() == null) {
			throw ApiException.validation("productId et sizeMl sont requis pour une ligne produit.");
		}
		Product product = productService.get(item.productId());
		Size size = product.getSizes().stream()
				.filter(s -> s.getMl() == item.sizeMl())
				.findFirst()
				.orElseThrow(() -> ApiException.validation("Taille introuvable pour ce produit: " + item.sizeMl() + "ml"));
		int qty = item.qty() == null || item.qty() < 1 ? 1 : item.qty();

		Optional<Promotion> promo = pricingService.bestAutomaticPromotion(product, activePromotions);
		int unitPrice = promo.map(p -> pricingService.discountedPrice(size.getPrice(), p)).orElse(size.getPrice());

		String lineId = product.getId() + "-" + size.getMl();
		return CartDtos.Line.builder()
				.id(lineId)
				.sku(product.getSku())
				.name(product.getName() + " — " + size.getMl() + "ml")
				.unitPrice(unitPrice)
				.qty(qty)
				.lineTotal(unitPrice * qty)
				.build();
	}

	/** §3.9 — flat pack price, never a sum of the slotted products' own prices; qty is always 1. */
	private CartDtos.Line buildPackLine(CartDtos.ItemRequest item) {
		Pack pack = packService.get(item.packId(), true);
		List<String> slotIds = item.slotProductIds() == null ? List.of() : item.slotProductIds();
		if (slotIds.size() != pack.getSize()) {
			throw ApiException.validation("Ce pack nécessite exactement " + pack.getSize() + " produits.");
		}
		List<Product> slotProducts = slotIds.stream().map(productService::get).toList();
		for (int i = 0; i < slotIds.size(); i++) {
			if (!packService.isEligible(pack, slotIds.get(i))) {
				throw ApiException.unprocessable(com.attia.advanced_auth_api.shop.common.ErrorCode.UNPROCESSABLE,
						"Produit non éligible pour ce pack: " + slotIds.get(i));
			}
		}
		String names = slotProducts.stream().map(Product::getName).collect(Collectors.joining(", "));
		String lineId = "custom-pack-" + pack.getId() + "-" + String.join("-", slotIds);
		return CartDtos.Line.builder()
				.id(lineId)
				.sku("PACK")
				.name(pack.getName() + " (" + names + ")")
				.unitPrice(pack.getPrice())
				.qty(1)
				.lineTotal(pack.getPrice())
				.build();
	}
}

package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.document.Product;
import com.attia.advanced_auth_api.shop.document.ProductTag;
import com.attia.advanced_auth_api.shop.document.Promotion;
import com.attia.advanced_auth_api.shop.dto.ProductDtos;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * backend-api-spec.md §3.3 (displayed price resolution) and §3.4 (promo badge vs. tag). Never
 * trust a client-computed price — every price shown anywhere is resolved here, server-side.
 *
 * <p>Deviation from §3.3 step 1, called out per §17.6: when several automatic promotions match a
 * product, this picks the one yielding the <em>largest actual TND discount</em>, not the one with
 * the numerically largest raw {@code value} — comparing a percent against a fixed amount directly
 * is the known bug §17.6 flags, and the doc explicitly invites fixing it in a re-implementation.
 */
@Service
public class PricingService {

	public record Resolved(int displayPrice, int originalPrice, Promotion appliedPromo) {
	}

	public Optional<Promotion> bestAutomaticPromotion(Product product, List<Promotion> activePromotions) {
		return activePromotions.stream()
				.filter(p -> p.getCode() == null || p.getCode().isBlank())
				.filter(p -> matchesScope(p, product.getId()))
				.max(java.util.Comparator.comparingInt(p -> discountAmount(product.getPrice(), p)));
	}

	public boolean matchesScope(Promotion promotion, String productId) {
		return promotion.getScope() == Promotion.Scope.all
				|| (promotion.getProductIds() != null && promotion.getProductIds().contains(productId));
	}

	public int discountAmount(int price, Promotion promotion) {
		return price - discountedPrice(price, promotion);
	}

	public int discountedPrice(int price, Promotion promotion) {
		if (promotion.getDiscountType() == Promotion.DiscountType.percent) {
			return (int) Math.round(price * (1 - promotion.getValue() / 100.0));
		}
		return Math.max(0, (int) Math.round(price - promotion.getValue()));
	}

	public Resolved resolve(Product product, List<Promotion> activePromotions) {
		Optional<Promotion> best = bestAutomaticPromotion(product, activePromotions);
		if (best.isPresent()) {
			int discounted = discountedPrice(product.getPrice(), best.get());
			return new Resolved(discounted, product.getPrice(), best.get());
		}
		return new Resolved(product.getPrice(), product.getPrice(), null);
	}

	/** §3.4 — a product is "promo" if it has a live automatic promotion OR the static tag says so. */
	public boolean isPromo(Product product, List<Promotion> activePromotions) {
		return bestAutomaticPromotion(product, activePromotions).isPresent() || product.getTag() == ProductTag.Promo;
	}

	public boolean isBestseller(Product product) {
		return product.getTag() == ProductTag.Bestseller;
	}

	public ProductDtos.PromotionSummary toSummary(Promotion promotion) {
		if (promotion == null) {
			return null;
		}
		return new ProductDtos.PromotionSummary(
				promotion.getId(), promotion.getLabel(), promotion.getCode(),
				promotion.getDiscountType().name(), promotion.getValue());
	}
}

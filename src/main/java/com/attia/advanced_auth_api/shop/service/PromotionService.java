package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.Ids;
import com.attia.advanced_auth_api.shop.document.Promotion;
import com.attia.advanced_auth_api.shop.dto.CartDtos;
import com.attia.advanced_auth_api.shop.dto.PromotionDtos;
import com.attia.advanced_auth_api.shop.repository.PromotionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** backend-api-spec.md §2.3 / §3.8 / §8. */
@Service
public class PromotionService {
	private final PromotionRepository promotionRepository;

	public PromotionService(PromotionRepository promotionRepository) {
		this.promotionRepository = promotionRepository;
	}

	public List<Promotion> listAll() {
		return promotionRepository.findAll();
	}

	public List<Promotion> listActive() {
		return promotionRepository.findByActiveTrue();
	}

	public long countActive() {
		return promotionRepository.countByActiveTrue();
	}

	public Promotion get(String id) {
		return promotionRepository.findById(id).orElseThrow(() -> ApiException.notFound("Promotion introuvable."));
	}

	public Promotion create(PromotionDtos.CreateRequest request) {
		Promotion.Scope scope = request.scope() == null ? Promotion.Scope.all : request.scope();
		Promotion promotion = Promotion.builder()
				.id(Ids.promo())
				.label(blankToDefault(request.label(), "Promotion"))
				.code(normalizeCode(request.code()))
				.scope(scope)
				.productIds(scope == Promotion.Scope.all ? List.of() : safe(request.productIds()))
				.discountType(request.discountType() == Promotion.DiscountType.fixed
						? Promotion.DiscountType.fixed : Promotion.DiscountType.percent)
				.value(Math.max(0, request.value() == null ? 0 : request.value()))
				.active(request.active() == null || request.active())
				.createdAt(System.currentTimeMillis())
				.build();
		return promotionRepository.save(promotion);
	}

	public Promotion update(String id, PromotionDtos.UpdateRequest request) {
		Promotion promotion = get(id);
		if (request.label() != null) {
			promotion.setLabel(blankToDefault(request.label(), "Promotion"));
		}
		if (request.code() != null) {
			promotion.setCode(normalizeCode(request.code()));
		}
		Promotion.Scope scope = request.scope() != null ? request.scope() : promotion.getScope();
		promotion.setScope(scope);
		if (scope == Promotion.Scope.all) {
			promotion.setProductIds(List.of());
		} else if (request.productIds() != null) {
			promotion.setProductIds(request.productIds());
		}
		if (request.discountType() != null) {
			promotion.setDiscountType(request.discountType());
		}
		if (request.value() != null) {
			promotion.setValue(Math.max(0, request.value()));
		}
		if (request.active() != null) {
			promotion.setActive(request.active());
		}
		return promotionRepository.save(promotion);
	}

	public void delete(String id) {
		get(id);
		promotionRepository.deleteById(id);
	}

	public Promotion toggle(String id) {
		Promotion promotion = get(id);
		promotion.setActive(!promotion.isActive());
		return promotionRepository.save(promotion);
	}

	/** §3.8 — code lookup + scoped discount computation. */
	public CartDtos.ValidateCodeResponse validateCode(String code, List<CartDtos.ValidateCodeLine> items) {
		Optional<Promotion> found = findByCode(code);
		if (found.isEmpty()) {
			return CartDtos.ValidateCodeResponse.builder().promo(null).discount(0).reason("INVALID_CODE").build();
		}
		Promotion promo = found.get();
		int discount = computeCodeDiscount(promo, items);
		if (discount <= 0) {
			return CartDtos.ValidateCodeResponse.builder().promo(null).discount(0).reason("NO_MATCHING_ITEMS").build();
		}
		return CartDtos.ValidateCodeResponse.builder().promo(promo).discount(discount).reason(null).build();
	}

	public Optional<Promotion> findByCode(String code) {
		if (code == null || code.isBlank()) {
			return Optional.empty();
		}
		String normalized = code.trim().toUpperCase(Locale.ROOT);
		return listActive().stream()
				.filter(p -> normalized.equals(p.getCode()))
				.findFirst();
	}

	public boolean matchesScope(Promotion promo, String cartLineId) {
		if (promo.getScope() == Promotion.Scope.all) {
			return true;
		}
		if (promo.getProductIds().contains(cartLineId)) {
			return true;
		}
		return promo.getProductIds().stream().anyMatch(pid -> cartLineId.startsWith(pid + "-"));
	}

	public int computeCodeDiscount(Promotion promo, List<CartDtos.ValidateCodeLine> items) {
		int subtotal = items.stream()
				.filter(item -> matchesScope(promo, item.id()))
				.mapToInt(item -> item.price() * item.qty())
				.sum();
		if (subtotal == 0) {
			return 0;
		}
		if (promo.getDiscountType() == Promotion.DiscountType.percent) {
			return (int) Math.round(subtotal * promo.getValue() / 100.0);
		}
		return Math.min(subtotal, (int) Math.round((double) promo.getValue()));
	}

	private String normalizeCode(String code) {
		if (code == null || code.isBlank()) {
			return null;
		}
		return code.trim().toUpperCase(Locale.ROOT);
	}

	private String blankToDefault(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value.trim();
	}

	private List<String> safe(List<String> values) {
		return values == null ? new ArrayList<>() : values;
	}
}

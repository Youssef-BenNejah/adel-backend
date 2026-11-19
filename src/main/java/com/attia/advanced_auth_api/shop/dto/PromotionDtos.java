package com.attia.advanced_auth_api.shop.dto;

import com.attia.advanced_auth_api.shop.document.Promotion;
import java.util.List;

/** backend-api-spec.md §2.3 / §8. */
public final class PromotionDtos {
	private PromotionDtos() {
	}

	public record CreateRequest(
			String label,
			String code,
			Promotion.Scope scope,
			List<String> productIds,
			Promotion.DiscountType discountType,
			Integer value,
			Boolean active) {
	}

	public record UpdateRequest(
			String label,
			String code,
			Promotion.Scope scope,
			List<String> productIds,
			Promotion.DiscountType discountType,
			Integer value,
			Boolean active) {
	}
}

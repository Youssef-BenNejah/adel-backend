package com.attia.advanced_auth_api.shop.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** backend-api-spec.md §10 / §8.6. */
public final class CartDtos {
	private CartDtos() {
	}

	public record ItemRequest(String productId, Integer sizeMl, Integer qty, String packId, List<String> slotProductIds) {
	}

	public record QuoteRequest(List<ItemRequest> items, String promoCode) {
	}

	public record ValidateCodeLine(String id, int price, int qty) {
	}

	public record ValidateCodeRequest(String code, List<ValidateCodeLine> items) {
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class Line {
		private String id;
		private String sku;
		private String name;
		private int unitPrice;
		private int qty;
		private int lineTotal;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class AppliedPromo {
		private String code;
		private int discount;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class QuoteResponse {
		private List<Line> lines;
		private int subtotal;
		private AppliedPromo appliedPromo;
		private int discount;
		private int shipping;
		private int total;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class ValidateCodeResponse {
		private Object promo;
		private int discount;
		private String reason;
	}
}

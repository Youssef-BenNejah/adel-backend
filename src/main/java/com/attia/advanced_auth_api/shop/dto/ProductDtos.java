package com.attia.advanced_auth_api.shop.dto;

import com.attia.advanced_auth_api.shop.document.Notes;
import com.attia.advanced_auth_api.shop.document.ProductTag;
import com.attia.advanced_auth_api.shop.document.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** backend-api-spec.md §2.2 / §6. */
public final class ProductDtos {
	private ProductDtos() {
	}

	public record SizeRequest(int ml, int price) {
	}

	public record CreateRequest(
			String name,
			String category,
			String family,
			String description,
			List<SizeRequest> sizes,
			Integer oldPrice,
			ProductTag tag,
			String notesTop,
			String notesHeart,
			String notesBase,
			/** Up to 3 already-uploaded, ready {@code Media} ids to link in order — image-upload-api.md §5.2. */
			List<String> mediaIds) {
	}

	public record UpdateRequest(
			String name,
			String category,
			String family,
			String description,
			List<SizeRequest> sizes,
			Integer oldPrice,
			ProductTag tag,
			String notesTop,
			String notesHeart,
			String notesBase,
			List<String> mediaIds) {
	}

	public record PromotionSummary(String id, String label, String code, String discountType, int value) {
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class Response {
		private String id;
		private String sku;
		private String name;
		private String category;
		private String family;
		private List<String> images;
		private String image;
		private String description;
		private int price;
		private Integer oldPrice;
		private ProductTag tag;
		private String rating;
		private int reviews;
		private Notes notes;
		private List<Size> sizes;
		private int displayPrice;
		private int originalPrice;
		private PromotionSummary appliedPromo;
	}
}

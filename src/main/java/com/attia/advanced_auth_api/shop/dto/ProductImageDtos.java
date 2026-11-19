package com.attia.advanced_auth_api.shop.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** image-upload-api.md §5. */
public final class ProductImageDtos {
	private ProductImageDtos() {
	}

	public record ReplaceRequest(List<String> mediaIds) {
	}

	public record AppendRequest(String mediaId) {
	}

	public record ReorderRequest(List<String> order) {
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class LinkResponse {
		private int position;
		private MediaDtos.Response media;
	}
}

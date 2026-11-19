package com.attia.advanced_auth_api.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** backend-api-spec.md §14 / image-upload-api.md §6. */
public final class LandingDtos {
	private LandingDtos() {
	}

	/** Public/admin read shape — {@code image} resolved from the linked {@code Media}, never a raw id. */
	@Getter
	@Builder
	@AllArgsConstructor
	public static class HeroSlideResponse {
		private String id;
		private String image;
		private String eyebrow;
		private String title;
		private boolean active;
		private int order;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class GalleryResponse {
		private String id;
		private String image;
		private int order;
	}

	public record HeroSlideCreateRequest(String mediaId, String eyebrow, String title) {
	}

	public record HeroSlideUpdateRequest(String mediaId, String eyebrow, String title) {
	}

	public record MoveRequest(int direction) {
	}

	public record CategoryTileSetRequest(String mediaId) {
	}

	public record GalleryCreateRequest(String mediaId) {
	}

	public record GalleryUpdateRequest(String mediaId) {
	}
}

package com.attia.advanced_auth_api.shop.dto;

import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** image-upload-api.md §2–§4. */
public final class MediaDtos {
	private MediaDtos() {
	}

	public record InitiateUploadRequest(String filename, String mimeType, long bytes) {
	}

	public record CompleteUploadRequest(String checksum) {
	}

	/**
	 * §3.1 — everything the browser needs to PUT/POST straight to Cloudinary itself (direct-to-
	 * storage, per §3's whole point): the signed params are Cloudinary-specific, beyond the
	 * spec's generic {@code {mediaId, uploadUrl, uploadMethod, expiresAt}} sketch, but required
	 * for a real (not simulated) presigned flow.
	 */
	@Getter
	@Builder
	@AllArgsConstructor
	public static class InitiateUploadResponse {
		private String mediaId;
		private String uploadUrl;
		private String uploadMethod;
		private Instant expiresAt;
		private String apiKey;
		private long timestamp;
		private String signature;
		private String publicId;
	}

	/** Public shape of {@link com.attia.advanced_auth_api.shop.document.Media} — never leaks storage internals. */
	@Getter
	@Builder
	@AllArgsConstructor
	public static class Response {
		private String id;
		private String status;
		private String url;
		private Map<String, String> variants;
		private String mimeType;
		private Integer width;
		private Integer height;
		private Long bytes;
		private String checksum;
		private String originalName;
		private String uploadedBy;
		private Instant createdAt;
		private long linkCount;
	}
}

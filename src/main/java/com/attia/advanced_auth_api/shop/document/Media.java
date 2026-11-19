package com.attia.advanced_auth_api.shop.document;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** image-upload-api.md §2 — the decoupled, "dumb" media resource. */
@Document(collection = "media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {
	@Id
	private String id;
	private MediaStatus status;
	private String url;
	@Builder.Default
	private Map<String, String> variants = new LinkedHashMap<>();
	private String mimeType;
	private Integer width;
	private Integer height;
	private Long bytes;
	@Indexed
	private String checksum;
	private String originalName;
	private String uploadedBy;
	/** The Cloudinary public_id these bytes live under — never derived from client input (§11.4). */
	private String storageKey;
	/** Presigned-upload bookkeeping (Flow A only); null once the upload completes or for Flow B. */
	private Instant pendingExpiresAt;
	@CreatedDate
	private Instant createdAt;
}

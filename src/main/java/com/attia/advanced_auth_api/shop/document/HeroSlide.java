package com.attia.advanced_auth_api.shop.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** backend-api-spec.md §2.8. {@code mediaId} replaces the raw {@code image} string (image-upload-api.md §6). */
@Document(collection = "hero_slides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeroSlide {
	@Id
	private String id;
	private String mediaId;
	@Builder.Default
	private String eyebrow = "";
	@Builder.Default
	private String title = "";
	@Builder.Default
	private boolean active = true;
	private int order;
}

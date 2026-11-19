package com.attia.advanced_auth_api.shop.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * One row per category slug — backend-api-spec.md §2.8's {@code CategoryTileImage} map, modeled
 * as a document per slug rather than a single giant map document. {@code null} mediaId ⇒
 * storefront falls back to the first product photo found in that category.
 */
@Document(collection = "category_tiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTile {
	@Id
	private String categorySlug;
	private String mediaId;
}

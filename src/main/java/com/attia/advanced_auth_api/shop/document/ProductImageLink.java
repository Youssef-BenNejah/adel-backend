package com.attia.advanced_auth_api.shop.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/** image-upload-api.md §5 — the {@code (productId, position)} join record, max 3 per product. */
@Document(collection = "product_image_links")
@CompoundIndex(name = "product_position_unique", def = "{'productId': 1, 'position': 1}", unique = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageLink {
	@Id
	private String id;
	private String productId;
	private String mediaId;
	private int position;
}

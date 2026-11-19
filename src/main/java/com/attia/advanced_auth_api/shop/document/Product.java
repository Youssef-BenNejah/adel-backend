package com.attia.advanced_auth_api.shop.document;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * backend-api-spec.md §2.2. {@code images}/{@code image} are NOT stored here (they are derived
 * from {@link ProductImageLink} rows at read time, per image-upload-api.md §5) — only the raw
 * catalog fields live on this document.
 */
@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
	@Id
	private String id;
	private String sku;
	private String name;
	@Indexed
	private String category;
	private String family;
	@Builder.Default
	private String description = "";
	@Builder.Default
	private Integer oldPrice = null;
	private ProductTag tag;
	@Builder.Default
	private String rating = "5.0";
	@Builder.Default
	private int reviews = 0;
	private Notes notes;
	@Builder.Default
	private List<Size> sizes = new ArrayList<>();

	public int getPrice() {
		return sizes != null && !sizes.isEmpty() ? sizes.get(0).getPrice() : 0;
	}
}

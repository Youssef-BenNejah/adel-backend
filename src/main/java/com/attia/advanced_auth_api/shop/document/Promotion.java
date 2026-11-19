package com.attia.advanced_auth_api.shop.document;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** backend-api-spec.md §2.3. */
@Document(collection = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
	@Id
	private String id;
	private String label;
	/** {@code null} = automatic discount (no code needed). */
	private String code;
	private Scope scope;
	@Builder.Default
	private List<String> productIds = new ArrayList<>();
	private DiscountType discountType;
	private int value;
	@Builder.Default
	private boolean active = true;
	private long createdAt;

	public enum Scope {
		all,
		products
	}

	public enum DiscountType {
		percent,
		fixed
	}
}

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

/** backend-api-spec.md §2.4 — "build your own" bundle. */
@Document(collection = "packs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pack {
	@Id
	private String id;
	@Builder.Default
	private String name = "Pack";
	private int price;
	@Builder.Default
	private int size = 3;
	private Promotion.Scope scope;
	@Builder.Default
	private List<String> productIds = new ArrayList<>();
	@Builder.Default
	private boolean active = true;
	private int order;
}

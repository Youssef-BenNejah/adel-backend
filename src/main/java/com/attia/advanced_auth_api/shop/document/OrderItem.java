package com.attia.advanced_auth_api.shop.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Immutable cart-line snapshot embedded in {@link Order#getItems()} — backend-api-spec.md §2.5. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
	private String id;
	private String sku;
	private String name;
	private int price;
	private int qty;
}

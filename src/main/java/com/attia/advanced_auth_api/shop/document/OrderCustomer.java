package com.attia.advanced_auth_api.shop.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Checkout-form snapshot embedded in {@link Order#getCustomer()} — backend-api-spec.md §2.5. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCustomer {
	private String name;
	private String phone;
	@Builder.Default
	private String email = "";
	private String address;
	private String city;
	@Builder.Default
	private String note = "";
}

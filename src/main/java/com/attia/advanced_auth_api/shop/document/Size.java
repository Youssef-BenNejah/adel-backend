package com.attia.advanced_auth_api.shop.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Embedded in {@link Product#getSizes()} — backend-api-spec.md §2.2. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Size {
	private int ml;
	private int price;
}

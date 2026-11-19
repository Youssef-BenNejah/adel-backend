package com.attia.advanced_auth_api.shop.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Embedded in {@link Product#getNotes()} — backend-api-spec.md §2.2. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notes {
	@Builder.Default
	private String top = "";
	@Builder.Default
	private String heart = "";
	@Builder.Default
	private String base = "";
}

package com.attia.advanced_auth_api.shop.dto;

import com.attia.advanced_auth_api.shop.document.Promotion;
import java.util.List;

/** backend-api-spec.md §2.4 / §9. */
public final class PackDtos {
	private PackDtos() {
	}

	public record CreateRequest(
			String name,
			Integer price,
			Integer size,
			Promotion.Scope scope,
			List<String> productIds,
			Boolean active) {
	}

	public record UpdateRequest(
			String name,
			Integer price,
			Integer size,
			Promotion.Scope scope,
			List<String> productIds,
			Boolean active) {
	}
}

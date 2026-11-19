package com.attia.advanced_auth_api.shop.dto;

/** backend-api-spec.md §5. */
public final class CategoryDtos {
	private CategoryDtos() {
	}

	public record CreateRequest(String label, String sub) {
	}

	public record UpdateRequest(String label, String sub, Integer order) {
	}
}

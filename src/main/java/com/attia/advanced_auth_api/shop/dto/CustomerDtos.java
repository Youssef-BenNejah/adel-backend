package com.attia.advanced_auth_api.shop.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** backend-api-spec.md §2.6 / §12. */
public final class CustomerDtos {
	private CustomerDtos() {
	}

	public record CreateRequest(String name, String email, String phone, String password) {
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class Response {
		private String id;
		private String name;
		private String email;
		private String phone;
		private Instant createdAt;
		private String status;
	}
}

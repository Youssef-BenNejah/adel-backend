package com.attia.advanced_auth_api.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** backend-api-spec.md §4. */
public final class AdminAuthDtos {
	private AdminAuthDtos() {
	}

	public record LoginRequest(String username, String password) {
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class AdminPrincipal {
		private String username;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class LoginResponse {
		private String token;
		private AdminPrincipal admin;
	}
}

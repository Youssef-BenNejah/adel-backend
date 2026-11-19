package com.attia.advanced_auth_api.service;

import com.attia.advanced_auth_api.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthResult {
	private final String accessToken;
	private final String refreshToken;
	private final UserResponse user;
}

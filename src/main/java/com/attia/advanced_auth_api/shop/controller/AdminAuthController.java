package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.document.Role;
import com.attia.advanced_auth_api.dto.LoginRequest;
import com.attia.advanced_auth_api.service.AuthResult;
import com.attia.advanced_auth_api.service.AuthService;
import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.common.ErrorCode;
import com.attia.advanced_auth_api.shop.config.ShopAdminProperties;
import com.attia.advanced_auth_api.shop.dto.AdminAuthDtos;
import com.attia.advanced_auth_api.util.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * backend-api-spec.md §4 — admin auth, re-implemented on top of the existing hashed-password
 * {@code UserDocument}/{@code Role.ADMIN} system (the very table §17.2 asks a real backend to
 * have) instead of a parallel one. {@code username} is just the configured admin's display name;
 * the real credential lookup is by the seeded admin's email.
 */
@RestController
@RequestMapping("/api/v1/auth/admin")
public class AdminAuthController {
	private final AuthService authService;
	private final ShopAdminProperties adminProperties;

	public AdminAuthController(AuthService authService, ShopAdminProperties adminProperties) {
		this.authService = authService;
		this.adminProperties = adminProperties;
	}

	@PostMapping("/login")
	public Envelope<AdminAuthDtos.LoginResponse> login(
			@RequestBody AdminAuthDtos.LoginRequest request, HttpServletRequest httpRequest) {
		if (request.username() == null || !request.username().trim().equals(adminProperties.getUsername())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Identifiants invalides.");
		}
		LoginRequest loginRequest = LoginRequest.builder()
				.email(adminProperties.getEmail())
				.password(request.password())
				.build();
		AuthResult result = authService.login(loginRequest, resolveClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
		if (result.getUser().getRole() != Role.ADMIN) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Identifiants invalides.");
		}
		return Envelope.of(AdminAuthDtos.LoginResponse.builder()
				.token(result.getAccessToken())
				.admin(AdminAuthDtos.AdminPrincipal.builder().username(adminProperties.getUsername()).build())
				.build());
	}

	@PostMapping("/logout")
	public void logout(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
		String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
		authService.logout(token, null, null, null);
	}

	@GetMapping("/me")
	public Envelope<AdminAuthDtos.AdminPrincipal> me(Authentication authentication) {
		AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
		if (principal.getRole() != Role.ADMIN) {
			throw new ApiException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "Accès réservé à l'administrateur.");
		}
		return Envelope.of(AdminAuthDtos.AdminPrincipal.builder().username(adminProperties.getUsername()).build());
	}

	private String resolveClientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}

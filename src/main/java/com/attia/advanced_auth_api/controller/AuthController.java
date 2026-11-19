package com.attia.advanced_auth_api.controller;

import com.attia.advanced_auth_api.config.AppProperties;
import com.attia.advanced_auth_api.config.JwtProperties;
import com.attia.advanced_auth_api.dto.ApiResponse;
import com.attia.advanced_auth_api.dto.AuthResponse;
import com.attia.advanced_auth_api.dto.LoginRequest;
import com.attia.advanced_auth_api.dto.RegisterRequest;
import com.attia.advanced_auth_api.dto.UserResponse;
import com.attia.advanced_auth_api.service.AuthResult;
import com.attia.advanced_auth_api.service.AuthService;
import com.attia.advanced_auth_api.util.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private static final String REFRESH_COOKIE = "refresh_token";
	private final AuthService authService;
	private final AppProperties appProperties;
	private final JwtProperties jwtProperties;

	public AuthController(AuthService authService, AppProperties appProperties, JwtProperties jwtProperties) {
		this.authService = authService;
		this.appProperties = appProperties;
		this.jwtProperties = jwtProperties;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<UserResponse>> register(
			@Valid @RequestBody RegisterRequest request,
			HttpServletRequest httpRequest) {
		UserResponse user = authService.register(request, resolveClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
		return ResponseEntity.ok(ApiResponse.success("Registration successful", user));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		AuthResult result = authService.login(request, resolveClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
		setRefreshCookie(httpResponse, result.getRefreshToken());
		AuthResponse response = AuthResponse.builder()
				.accessToken(result.getAccessToken())
				.user(result.getUser())
				.build();
		return ResponseEntity.ok(ApiResponse.success("Login successful", response));
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<AuthResponse>> refresh(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		AuthResult result = authService.refresh(refreshToken, resolveClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
		setRefreshCookie(httpResponse, result.getRefreshToken());
		AuthResponse response = AuthResponse.builder()
				.accessToken(result.getAccessToken())
				.user(result.getUser())
				.build();
		return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
			@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		String accessToken = extractBearer(authorization);
		authService.logout(accessToken, refreshToken, resolveClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
		clearRefreshCookie(httpResponse);
		return ResponseEntity.ok(ApiResponse.success("Logged out", null));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
		AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
		UserResponse user = authService.getCurrentUser(principal.getId());
		return ResponseEntity.ok(ApiResponse.success("Current user", user));
	}

	private void setRefreshCookie(HttpServletResponse response, String token) {
		ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, token)
				.httpOnly(true)
				.secure(!appProperties.isDev())
				.sameSite("Strict")
				.path("/api/v1/auth")
				.maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpiry()))
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private void clearRefreshCookie(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
				.httpOnly(true)
				.secure(!appProperties.isDev())
				.sameSite("Strict")
				.path("/api/v1/auth")
				.maxAge(0)
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private String extractBearer(String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return null;
		}
		return authorization.substring(7);
	}

	private String resolveClientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}

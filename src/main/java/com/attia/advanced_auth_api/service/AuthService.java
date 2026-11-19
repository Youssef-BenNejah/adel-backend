package com.attia.advanced_auth_api.service;

import com.attia.advanced_auth_api.config.SecurityProperties;
import com.attia.advanced_auth_api.document.AuditAction;
import com.attia.advanced_auth_api.document.RefreshTokenDocument;
import com.attia.advanced_auth_api.document.Role;
import com.attia.advanced_auth_api.document.UserDocument;
import com.attia.advanced_auth_api.dto.LoginRequest;
import com.attia.advanced_auth_api.dto.RegisterRequest;
import com.attia.advanced_auth_api.dto.UserResponse;
import com.attia.advanced_auth_api.repository.RefreshTokenRepository;
import com.attia.advanced_auth_api.repository.UserRepository;
import com.attia.advanced_auth_api.util.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenBlacklistService tokenBlacklistService;
	private final SecurityProperties securityProperties;
	private final AuditService auditService;

	public AuthService(
			UserRepository userRepository,
			RefreshTokenRepository refreshTokenRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider,
			TokenBlacklistService tokenBlacklistService,
			SecurityProperties securityProperties,
			AuditService auditService) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.tokenBlacklistService = tokenBlacklistService;
		this.securityProperties = securityProperties;
		this.auditService = auditService;
	}

	public UserResponse register(RegisterRequest request, String ipAddress, String userAgent) {
		String email = request.getEmail().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}
		UserDocument user = UserDocument.builder()
				.email(email)
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.role(Role.USER)
				.build();
		userRepository.save(user);
		auditService.log(user.getId(), AuditAction.REGISTER, ipAddress, userAgent);
		return toUserResponse(user);
	}

	public AuthResult login(LoginRequest request, String ipAddress, String userAgent) {

		UserDocument user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
		if (!user.isEnabled()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled");
		}
		unlockIfExpired(user);
		if (user.isLocked()) {
			auditService.log(user.getId(), AuditAction.LOGIN_FAILED, ipAddress, userAgent);
			throw new ResponseStatusException(HttpStatus.LOCKED, "Account is locked");
		}
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			handleFailedLogin(user);
			auditService.log(user.getId(), AuditAction.LOGIN_FAILED, ipAddress, userAgent);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
		resetFailedAttempts(user);
		String accessToken = jwtTokenProvider.generateAccessToken(user);
		String refreshToken = jwtTokenProvider.generateRefreshToken(user);
		saveRefreshToken(user, refreshToken);
		auditService.log(user.getId(), AuditAction.LOGIN, ipAddress, userAgent);
		return AuthResult.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.user(toUserResponse(user))
				.build();
	}

	public AuthResult refresh(String refreshToken, String ipAddress, String userAgent) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token missing");
		}
		String tokenType;
		String userId;
		try {
			tokenType = jwtTokenProvider.getTokenType(refreshToken);
			userId = jwtTokenProvider.getSubject(refreshToken);
		} catch (JwtException | IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
		}
		if (!"refresh".equals(tokenType)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
		}
		UserDocument user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
		if (!user.isEnabled()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled");
		}
		RefreshTokenDocument storedToken = findActiveRefreshToken(userId, refreshToken);
		storedToken.setRevoked(true);
		refreshTokenRepository.save(storedToken);

		String newAccessToken = jwtTokenProvider.generateAccessToken(user);
		String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
		saveRefreshToken(user, newRefreshToken);
		auditService.log(user.getId(), AuditAction.REFRESH, ipAddress, userAgent);
		return AuthResult.builder()
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken)
				.user(toUserResponse(user))
				.build();
	}

	public void logout(String accessToken, String refreshToken, String ipAddress, String userAgent) {
		if (accessToken != null && !accessToken.isBlank()) {
			tokenBlacklistService.blacklist(accessToken);
		}
		if (refreshToken != null && !refreshToken.isBlank()) {
			try {
				String userId = jwtTokenProvider.getSubject(refreshToken);
				RefreshTokenDocument storedToken = findActiveRefreshToken(userId, refreshToken);
				storedToken.setRevoked(true);
				refreshTokenRepository.save(storedToken);
				auditService.log(userId, AuditAction.LOGOUT, ipAddress, userAgent);
			} catch (Exception ex) {
				// ignore invalid refresh token during logout
			}
		}
	}

	public UserResponse getCurrentUser(String userId) {
		UserDocument user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		return toUserResponse(user);
	}

	private void saveRefreshToken(UserDocument user, String refreshToken) {
		RefreshTokenDocument doc = RefreshTokenDocument.builder()
				.userId(user.getId())
				.tokenHash(hashRefreshToken(refreshToken))
				.revoked(false)
				.expiresAt(jwtTokenProvider.getExpiration(refreshToken))
				.build();
		refreshTokenRepository.save(doc);
	}

	private RefreshTokenDocument findActiveRefreshToken(String userId, String refreshToken) {
		String expectedHash = hashRefreshToken(refreshToken);
		List<RefreshTokenDocument> tokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
		for (RefreshTokenDocument token : tokens) {
			if (hashesEqual(expectedHash, token.getTokenHash())) {
				return token;
			}
		}
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
	}

	private void handleFailedLogin(UserDocument user) {
		int failed = user.getFailedAttempts() + 1;
		user.setFailedAttempts(failed);
		if (failed >= securityProperties.getMaxLoginAttempts()) {
			user.setLocked(true);
			user.setLockExpiresAt(Instant.now().plusSeconds(securityProperties.getLockDurationMinutes() * 60L));
		}
		userRepository.save(user);
	}

	private void resetFailedAttempts(UserDocument user) {
		user.setFailedAttempts(0);
		user.setLocked(false);
		user.setLockExpiresAt(null);
		userRepository.save(user);
	}

	private void unlockIfExpired(UserDocument user) {
		if (user.isLocked() && user.getLockExpiresAt() != null
				&& user.getLockExpiresAt().isBefore(Instant.now())) {
			user.setLocked(false);
			user.setFailedAttempts(0);
			user.setLockExpiresAt(null);
			userRepository.save(user);
		}
	}

	private UserResponse toUserResponse(UserDocument user) {
		return UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.role(user.getRole())
				.createdAt(user.getCreatedAt())
				.build();
	}

	private String hashRefreshToken(String refreshToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hashed);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available", ex);
		}
	}

	private boolean hashesEqual(String expected, String actual) {
		if (expected == null || actual == null) {
			return false;
		}
		return MessageDigest.isEqual(
				expected.getBytes(StandardCharsets.UTF_8),
				actual.getBytes(StandardCharsets.UTF_8));
	}
}

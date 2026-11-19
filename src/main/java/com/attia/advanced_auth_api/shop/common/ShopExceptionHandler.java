package com.attia.advanced_auth_api.shop.common;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Translates every exception raised by a shop controller into the spec's error envelope
 * (backend-api-spec.md §16.1). Scoped to {@code shop.controller} so the pre-existing
 * {@link com.attia.advanced_auth_api.exception.GlobalExceptionHandler} keeps handling the
 * original auth API untouched.
 */
@RestControllerAdvice(basePackages = "com.attia.advanced_auth_api.shop.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ShopExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorBody> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getStatus())
				.body(ErrorBody.of(ex.getCode(), ex.getMessage(), ex.getFields()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorBody> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fields = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			fields.put(error.getField(), error.getDefaultMessage());
		}
		return ResponseEntity.badRequest()
				.body(ErrorBody.of(ErrorCode.VALIDATION_ERROR, "Requête invalide.", fields));
	}

	@ExceptionHandler({ MissingServletRequestParameterException.class, HttpMessageNotReadableException.class,
			MissingServletRequestPartException.class, IllegalArgumentException.class })
	public ResponseEntity<ErrorBody> handleBadRequest(Exception ex) {
		return ResponseEntity.badRequest()
				.body(ErrorBody.of(ErrorCode.VALIDATION_ERROR, ex.getMessage(), null));
	}

	/**
	 * {@code AdminAuthController} reuses the pre-existing {@code AuthService}, which raises this
	 * (not {@link ApiException}) on bad credentials/locked accounts — translate it the same way.
	 */
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorBody> handleResponseStatus(ResponseStatusException ex) {
		HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
		if (status == null) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		String code = switch (status) {
			case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED;
			case FORBIDDEN, LOCKED -> ErrorCode.FORBIDDEN;
			case NOT_FOUND -> ErrorCode.NOT_FOUND;
			case CONFLICT -> ErrorCode.CONFLICT;
			default -> ErrorCode.INTERNAL;
		};
		String message = ex.getReason() == null ? ex.getMessage() : ex.getReason();
		return ResponseEntity.status(status).body(ErrorBody.of(code, message, null));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorBody> handleMaxUpload(MaxUploadSizeExceededException ex) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(ErrorBody.of(ErrorCode.INVALID_IMAGE, "Fichier trop volumineux.", null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorBody> handleGeneric(Exception ex) {
		log.error("Unhandled shop exception", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorBody.of(ErrorCode.INTERNAL, "Erreur interne.", null));
	}
}

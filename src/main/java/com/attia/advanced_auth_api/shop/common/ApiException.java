package com.attia.advanced_auth_api.shop.common;

import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Carries a shop error envelope ({@code {"error":{"code","message","fields"}}}) to the handler. */
@Getter
public class ApiException extends RuntimeException {
	private final HttpStatus status;
	private final String code;
	private final Map<String, String> fields;

	public ApiException(HttpStatus status, String code, String message) {
		this(status, code, message, null);
	}

	public ApiException(HttpStatus status, String code, String message, Map<String, String> fields) {
		super(message);
		this.status = status;
		this.code = code;
		this.fields = fields;
	}

	public static ApiException notFound(String message) {
		return new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message);
	}

	public static ApiException validation(String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message);
	}

	public static ApiException validation(String message, Map<String, String> fields) {
		return new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, fields);
	}

	public static ApiException unprocessable(String code, String message) {
		return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
	}

	public static ApiException conflict(String code, String message) {
		return new ApiException(HttpStatus.CONFLICT, code, message);
	}
}

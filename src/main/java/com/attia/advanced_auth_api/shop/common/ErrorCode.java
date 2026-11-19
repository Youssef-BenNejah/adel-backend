package com.attia.advanced_auth_api.shop.common;

/** Error codes from backend-api-spec.md §16.1 plus media-specific ones from image-upload-api.md. */
public final class ErrorCode {
	public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
	public static final String UNAUTHORIZED = "UNAUTHORIZED";
	public static final String FORBIDDEN = "FORBIDDEN";
	public static final String NOT_FOUND = "NOT_FOUND";
	public static final String CONFLICT = "CONFLICT";
	public static final String UNPROCESSABLE = "UNPROCESSABLE";
	public static final String INTERNAL = "INTERNAL";
	public static final String INVALID_IMAGE = "INVALID_IMAGE";
	public static final String UPLOAD_NOT_FOUND = "UPLOAD_NOT_FOUND";
	public static final String IMAGE_LIMIT_REACHED = "IMAGE_LIMIT_REACHED";
	public static final String EMAIL_TAKEN = "EMAIL_TAKEN";

	private ErrorCode() {
	}
}

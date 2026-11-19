package com.attia.advanced_auth_api.shop.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** The {@code { "error": { "code", "message", "fields" } }} envelope, per backend-api-spec.md §16.1. */
@Getter
@AllArgsConstructor
public class ErrorBody {
	private final ErrorPayload error;

	public static ErrorBody of(String code, String message, Map<String, String> fields) {
		return new ErrorBody(new ErrorPayload(code, message, fields));
	}

	@Getter
	@AllArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ErrorPayload {
		private final String code;
		private final String message;
		private final Map<String, String> fields;
	}
}

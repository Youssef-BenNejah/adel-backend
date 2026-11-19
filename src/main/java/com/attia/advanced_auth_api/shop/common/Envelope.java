package com.attia.advanced_auth_api.shop.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The {@code { "data": ..., "meta": ... }} success envelope used by every shop endpoint,
 * per backend-api-spec.md §1.
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Envelope<T> {
	private final T data;
	private final PageMeta meta;

	public static <T> Envelope<T> of(T data) {
		return new Envelope<>(data, null);
	}

	public static <T> Envelope<T> of(T data, PageMeta meta) {
		return new Envelope<>(data, meta);
	}
}

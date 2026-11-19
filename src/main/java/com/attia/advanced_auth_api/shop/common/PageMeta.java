package com.attia.advanced_auth_api.shop.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Pagination metadata, per backend-api-spec.md §1 / §16.2. */
@Getter
@AllArgsConstructor
public class PageMeta {
	private final int page;
	private final int pageSize;
	private final long total;
	private final int pageCount;

	public static PageMeta of(int page, int pageSize, long total) {
		int pageCount = pageSize <= 0 ? 0 : (int) Math.ceil(total / (double) pageSize);
		return new PageMeta(page, pageSize, total, Math.max(pageCount, 0));
	}
}

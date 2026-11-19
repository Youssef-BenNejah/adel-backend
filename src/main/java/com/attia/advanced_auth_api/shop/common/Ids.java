package com.attia.advanced_auth_api.shop.common;

import java.security.SecureRandom;
import java.util.Locale;

/** Human-readable prefixed ID generation, per backend-api-spec.md §1. */
public final class Ids {
	private static final SecureRandom RANDOM = new SecureRandom();

	private Ids() {
	}

	public static String order() {
		return "CMD-" + base36(System.currentTimeMillis()).toUpperCase(Locale.ROOT);
	}

	public static String customer() {
		return "CLI-" + base36(System.currentTimeMillis()).toUpperCase(Locale.ROOT);
	}

	public static String promo() {
		return "promo-" + System.currentTimeMillis() + "-" + rand3();
	}

	public static String pack() {
		return "pack-" + System.currentTimeMillis() + "-" + rand3();
	}

	public static String slide() {
		return "slide-" + System.currentTimeMillis() + "-" + rand3();
	}

	public static String gallery() {
		return "gallery-" + System.currentTimeMillis() + "-" + rand3();
	}

	/** {@code med_<ulid-ish>} — sortable (time-prefixed), unguessable (random suffix). */
	public static String media() {
		return "med_" + base36(System.currentTimeMillis()).toUpperCase(Locale.ROOT)
				+ Integer.toString(RANDOM.nextInt(36 * 36 * 36 * 36), 36).toUpperCase(Locale.ROOT);
	}

	public static String productCustom(String categorySlug) {
		return categorySlug + "-custom-" + System.currentTimeMillis();
	}

	private static String base36(long value) {
		return Long.toString(value, 36);
	}

	private static String rand3() {
		return String.valueOf(100 + RANDOM.nextInt(900));
	}
}

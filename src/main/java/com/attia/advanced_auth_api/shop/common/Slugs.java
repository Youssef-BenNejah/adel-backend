package com.attia.advanced_auth_api.shop.common;

import java.text.Normalizer;
import java.util.function.Predicate;

/** Category slug generation, per backend-api-spec.md §3.1. */
public final class Slugs {
	private Slugs() {
	}

	public static String slugify(String label) {
		if (label == null) {
			return "categorie";
		}
		String normalized = Normalizer.normalize(label.toLowerCase(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
		String slug = normalized.replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
		return slug.isEmpty() ? "categorie" : slug;
	}

	public static String uniqueSlug(String base, Predicate<String> isTaken) {
		if (!isTaken.test(base)) {
			return base;
		}
		int suffix = 2;
		while (isTaken.test(base + "-" + suffix)) {
			suffix++;
		}
		return base + "-" + suffix;
	}
}

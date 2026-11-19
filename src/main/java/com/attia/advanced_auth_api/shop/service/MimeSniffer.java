package com.attia.advanced_auth_api.shop.service;

/**
 * Real magic-number sniffing of uploaded bytes — image-upload-api.md §7/§11.3: never trust the
 * client's declared {@code Content-Type}.
 */
public final class MimeSniffer {
	private MimeSniffer() {
	}

	public static final java.util.Set<String> ALLOWED = java.util.Set.of("image/jpeg", "image/png", "image/webp");

	public static String sniff(byte[] bytes) {
		if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
			return "image/jpeg";
		}
		if (bytes.length >= 8
				&& (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
				&& bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A) {
			return "image/png";
		}
		if (bytes.length >= 12
				&& bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
				&& bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
			return "image/webp";
		}
		return "application/octet-stream";
	}

	public static String extensionFor(String mimeType) {
		return switch (mimeType) {
			case "image/jpeg" -> "jpg";
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			default -> "bin";
		};
	}
}

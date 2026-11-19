package com.attia.advanced_auth_api.shop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Upload validation rules — image-upload-api.md §7. Where bytes actually live is Cloudinary's concern ({@link CloudinaryProperties}). */
@ConfigurationProperties(prefix = "shop.media")
@Getter
@Setter
public class ShopMediaProperties {
	private long maxBytes = 8L * 1024 * 1024;
	private int minDimension = 600;
	private int maxDimension = 8000;
	private int presignExpiryMinutes = 10;
	private int abandonedUploadHours = 24;
	private int orphanGraceDays = 7;
}

package com.attia.advanced_auth_api.shop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Credentials for the Cloudinary account media is stored in — image-upload-api.md §3/§9. */
@ConfigurationProperties(prefix = "cloudinary")
@Getter
@Setter
public class CloudinaryProperties {
	private String cloudName;
	private String apiKey;
	private String apiSecret;
	/** Every upload is namespaced under this folder, so it's easy to tell Adel Parfum's media apart in the account. */
	private String folder = "adel-parfum/media";

	public boolean isConfigured() {
		return cloudName != null && !cloudName.isBlank()
				&& apiKey != null && !apiKey.isBlank()
				&& apiSecret != null && !apiSecret.isBlank();
	}
}

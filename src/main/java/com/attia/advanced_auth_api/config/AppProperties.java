package com.attia.advanced_auth_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
	private String env;
	private int port;

	public boolean isDev() {
		return env != null && env.equalsIgnoreCase("dev");
	}
}

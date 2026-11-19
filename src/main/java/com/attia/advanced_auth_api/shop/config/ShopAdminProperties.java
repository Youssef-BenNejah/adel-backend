package com.attia.advanced_auth_api.shop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Seeds the single {@code AdminUser} row (backend-api-spec.md §2.9) on top of the existing
 * {@code UserDocument}/{@code Role.ADMIN} auth system, so the shop needs no parallel user table.
 */
@ConfigurationProperties(prefix = "shop.admin")
@Getter
@Setter
public class ShopAdminProperties {
	private String username = "admin";
	private String email = "admin@adelparfum.tn";
	private String password = "adel2026";
}

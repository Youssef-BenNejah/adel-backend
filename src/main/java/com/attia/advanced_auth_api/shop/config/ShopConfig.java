package com.attia.advanced_auth_api.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** {@code @ConfigurationPropertiesScan} on the main class already registers the shop's properties classes. */
@Configuration
@EnableScheduling
public class ShopConfig {
}

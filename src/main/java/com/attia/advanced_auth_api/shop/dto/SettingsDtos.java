package com.attia.advanced_auth_api.shop.dto;

/** backend-api-spec.md §2.7 / §13. */
public final class SettingsDtos {
	private SettingsDtos() {
	}

	public record UpdateRequest(String storeName, Integer freeShippingThreshold, Integer shippingFee, String freeShippingCode) {
	}
}

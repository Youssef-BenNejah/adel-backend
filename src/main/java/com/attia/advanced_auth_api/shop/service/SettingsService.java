package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.document.Settings;
import com.attia.advanced_auth_api.shop.dto.SettingsDtos;
import com.attia.advanced_auth_api.shop.repository.SettingsRepository;
import org.springframework.stereotype.Service;

/** backend-api-spec.md §2.7 / §13 — singleton resource. */
@Service
public class SettingsService {
	private final SettingsRepository settingsRepository;

	public SettingsService(SettingsRepository settingsRepository) {
		this.settingsRepository = settingsRepository;
	}

	public Settings get() {
		return settingsRepository.findById(Settings.SINGLETON_ID)
				.orElseGet(() -> settingsRepository.save(Settings.builder().build()));
	}

	public Settings update(SettingsDtos.UpdateRequest request) {
		Settings settings = get();
		if (request.storeName() != null) {
			settings.setStoreName(request.storeName());
		}
		if (request.freeShippingThreshold() != null) {
			settings.setFreeShippingThreshold(request.freeShippingThreshold());
		}
		if (request.shippingFee() != null) {
			settings.setShippingFee(request.shippingFee());
		}
		if (request.freeShippingCode() != null) {
			settings.setFreeShippingCode(request.freeShippingCode());
		}
		return settingsRepository.save(settings);
	}

	/** §3.5 — shipping fee resolution. */
	public int resolveShipping(int cartSubtotal) {
		Settings settings = get();
		if (cartSubtotal > 0 && cartSubtotal < settings.getFreeShippingThreshold()) {
			return settings.getShippingFee();
		}
		return 0;
	}
}

package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.document.Settings;
import com.attia.advanced_auth_api.shop.dto.SettingsDtos;
import com.attia.advanced_auth_api.shop.service.SettingsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** backend-api-spec.md §13 — singleton, publicly readable. */
@RestController
@RequestMapping("/api/v1")
public class SettingsController {
	private final SettingsService settingsService;

	public SettingsController(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	@GetMapping("/settings")
	public Envelope<Settings> get() {
		return Envelope.of(settingsService.get());
	}

	@PatchMapping("/admin/settings")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<Settings> update(@RequestBody SettingsDtos.UpdateRequest request) {
		return Envelope.of(settingsService.update(request));
	}
}

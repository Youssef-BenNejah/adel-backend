package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.dto.DashboardDtos;
import com.attia.advanced_auth_api.shop.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** backend-api-spec.md §15. */
@RestController
@RequestMapping("/api/v1")
public class DashboardController {
	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/admin/dashboard")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<DashboardDtos.Response> dashboard() {
		return Envelope.of(dashboardService.dashboard());
	}

	@GetMapping("/home/rails")
	public Envelope<DashboardDtos.HomeRails> homeRails() {
		return Envelope.of(dashboardService.homeRails());
	}
}

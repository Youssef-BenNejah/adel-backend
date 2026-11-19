package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.document.Pack;
import com.attia.advanced_auth_api.shop.dto.PackDtos;
import com.attia.advanced_auth_api.shop.service.PackService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** backend-api-spec.md §9. */
@RestController
@RequestMapping("/api/v1")
public class PackController {
	private final PackService packService;

	public PackController(PackService packService) {
		this.packService = packService;
	}

	@GetMapping("/packs")
	public Envelope<List<Pack>> listActive() {
		return Envelope.of(packService.listActive());
	}

	@GetMapping("/packs/{id}")
	public Envelope<Pack> getActive(@PathVariable String id) {
		return Envelope.of(packService.get(id, true));
	}

	@GetMapping("/admin/packs")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<Pack>> listAll() {
		return Envelope.of(packService.listAll());
	}

	@GetMapping("/admin/packs/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<Pack> getAny(@PathVariable String id) {
		return Envelope.of(packService.get(id, false));
	}

	@PostMapping("/admin/packs")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Envelope<Pack> create(@RequestBody PackDtos.CreateRequest request) {
		return Envelope.of(packService.create(request));
	}

	@PatchMapping("/admin/packs/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<Pack> update(@PathVariable String id, @RequestBody PackDtos.UpdateRequest request) {
		return Envelope.of(packService.update(id, request));
	}

	@DeleteMapping("/admin/packs/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String id) {
		packService.delete(id);
	}

	@PatchMapping("/admin/packs/{id}/toggle")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<Pack> toggle(@PathVariable String id) {
		return Envelope.of(packService.toggle(id));
	}
}

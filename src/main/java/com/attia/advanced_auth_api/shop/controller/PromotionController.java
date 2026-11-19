package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.document.Promotion;
import com.attia.advanced_auth_api.shop.dto.CartDtos;
import com.attia.advanced_auth_api.shop.dto.PromotionDtos;
import com.attia.advanced_auth_api.shop.service.PromotionService;
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

/** backend-api-spec.md §8. */
@RestController
@RequestMapping("/api/v1")
public class PromotionController {
	private final PromotionService promotionService;

	public PromotionController(PromotionService promotionService) {
		this.promotionService = promotionService;
	}

	@GetMapping("/admin/promotions")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<Promotion>> list() {
		return Envelope.of(promotionService.listAll());
	}

	@PostMapping("/admin/promotions")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Envelope<Promotion> create(@RequestBody PromotionDtos.CreateRequest request) {
		return Envelope.of(promotionService.create(request));
	}

	@PatchMapping("/admin/promotions/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<Promotion> update(@PathVariable String id, @RequestBody PromotionDtos.UpdateRequest request) {
		return Envelope.of(promotionService.update(id, request));
	}

	@DeleteMapping("/admin/promotions/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String id) {
		promotionService.delete(id);
	}

	@PatchMapping("/admin/promotions/{id}/toggle")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<Promotion> toggle(@PathVariable String id) {
		return Envelope.of(promotionService.toggle(id));
	}

	@PostMapping("/cart/validate-code")
	public Envelope<CartDtos.ValidateCodeResponse> validateCode(@RequestBody CartDtos.ValidateCodeRequest request) {
		return Envelope.of(promotionService.validateCode(request.code(), request.items()));
	}
}

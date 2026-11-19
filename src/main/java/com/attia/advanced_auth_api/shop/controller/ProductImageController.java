package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.dto.ProductImageDtos;
import com.attia.advanced_auth_api.shop.service.ProductImageLinkService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** image-upload-api.md §5 — product ↔ media links ("max 3, ordered"), enforced here only. */
@RestController
public class ProductImageController {
	private final ProductImageLinkService linkService;

	public ProductImageController(ProductImageLinkService linkService) {
		this.linkService = linkService;
	}

	@GetMapping("/api/v1/products/{id}/images")
	public Envelope<List<ProductImageDtos.LinkResponse>> list(@PathVariable String id) {
		return Envelope.of(linkService.list(id));
	}

	@PutMapping("/api/v1/admin/products/{id}/images")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<ProductImageDtos.LinkResponse>> replace(
			@PathVariable String id, @RequestBody ProductImageDtos.ReplaceRequest request) {
		return Envelope.of(linkService.replace(id, request.mediaIds()));
	}

	@PostMapping("/api/v1/admin/products/{id}/images")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Envelope<ProductImageDtos.LinkResponse> append(
			@PathVariable String id, @RequestBody ProductImageDtos.AppendRequest request) {
		return Envelope.of(linkService.append(id, request.mediaId()));
	}

	@DeleteMapping("/api/v1/admin/products/{id}/images/{position}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeAt(@PathVariable String id, @PathVariable int position) {
		linkService.removeAt(id, position);
	}

	@PatchMapping("/api/v1/admin/products/{id}/images/reorder")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<ProductImageDtos.LinkResponse>> reorder(
			@PathVariable String id, @RequestBody ProductImageDtos.ReorderRequest request) {
		return Envelope.of(linkService.reorder(id, request.order()));
	}
}

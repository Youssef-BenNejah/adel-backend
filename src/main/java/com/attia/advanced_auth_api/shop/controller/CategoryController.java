package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.document.Category;
import com.attia.advanced_auth_api.shop.dto.CategoryDtos;
import com.attia.advanced_auth_api.shop.service.CategoryService;
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

/** backend-api-spec.md §5. */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public Envelope<List<Category>> list() {
		return Envelope.of(categoryService.list());
	}

	@GetMapping("/{slug}")
	public Envelope<Category> get(@PathVariable String slug) {
		return Envelope.of(categoryService.get(slug));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Envelope<Category> create(@RequestBody CategoryDtos.CreateRequest request) {
		return Envelope.of(categoryService.create(request));
	}

	@PatchMapping("/{slug}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<Category> update(@PathVariable String slug, @RequestBody CategoryDtos.UpdateRequest request) {
		return Envelope.of(categoryService.update(slug, request));
	}

	@DeleteMapping("/{slug}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String slug) {
		categoryService.delete(slug);
	}
}

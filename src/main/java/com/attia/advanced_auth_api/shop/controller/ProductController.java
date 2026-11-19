package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.common.PageMeta;
import com.attia.advanced_auth_api.shop.document.Product;
import com.attia.advanced_auth_api.shop.document.ProductTag;
import com.attia.advanced_auth_api.shop.dto.ProductDtos;
import com.attia.advanced_auth_api.shop.service.ProductService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** backend-api-spec.md §6 / §15.2. */
@RestController
@RequestMapping("/api/v1")
public class ProductController {
	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/products")
	public Envelope<List<ProductDtos.Response>> list(
			@RequestParam(required = false) List<String> category,
			@RequestParam(required = false) String family,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Integer priceMin,
			@RequestParam(required = false) Integer priceMax,
			@RequestParam(required = false) ProductTag tag,
			@RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "12") int pageSize) {
		ProductService.PageResult result = productService.search(
				new ProductService.SearchParams(category, family, q, priceMin, priceMax, tag, sort, page, pageSize));
		return Envelope.of(productService.enrichAll(result.items()), PageMeta.of(page, pageSize, result.total()));
	}

	@GetMapping("/products/{id}")
	public Envelope<ProductDtos.Response> get(@PathVariable String id) {
		Product product = productService.get(id);
		return Envelope.of(productService.enrichAll(List.of(product)).get(0));
	}

	@GetMapping("/products/{id}/related")
	public Envelope<List<ProductDtos.Response>> related(
			@PathVariable String id, @RequestParam(defaultValue = "4") int limit) {
		return Envelope.of(productService.enrichAll(productService.related(id, limit)));
	}

	@PostMapping("/products")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Envelope<ProductDtos.Response> create(@RequestBody ProductDtos.CreateRequest request) {
		Product product = productService.create(request);
		return Envelope.of(productService.enrichAll(List.of(product)).get(0));
	}

	@PatchMapping("/products/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<ProductDtos.Response> update(@PathVariable String id, @RequestBody ProductDtos.UpdateRequest request) {
		Product product = productService.update(id, request);
		return Envelope.of(productService.enrichAll(List.of(product)).get(0));
	}

	@DeleteMapping("/products/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String id) {
		productService.delete(id);
	}

	/** §15.2 — alias of the public list, raw (no pricing enrichment), no visibility filtering. */
	@GetMapping("/admin/products")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<Product>> adminList(
			@RequestParam(required = false) List<String> category,
			@RequestParam(required = false) String family,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Integer priceMin,
			@RequestParam(required = false) Integer priceMax,
			@RequestParam(required = false) ProductTag tag,
			@RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int pageSize) {
		ProductService.PageResult result = productService.search(
				new ProductService.SearchParams(category, family, q, priceMin, priceMax, tag, sort, page, pageSize));
		return Envelope.of(result.items(), PageMeta.of(page, pageSize, result.total()));
	}
}

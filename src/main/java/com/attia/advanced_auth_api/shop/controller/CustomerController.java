package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.common.PageMeta;
import com.attia.advanced_auth_api.shop.document.Customer;
import com.attia.advanced_auth_api.shop.dto.CustomerDtos;
import com.attia.advanced_auth_api.shop.service.CustomerService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** backend-api-spec.md §12 — admin-only CRM. */
@RestController
@RequestMapping("/api/v1/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {
	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@GetMapping
	public Envelope<List<CustomerDtos.Response>> list(
			@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int pageSize) {
		CustomerService.PageResult result = customerService.search(q, page, pageSize);
		List<CustomerDtos.Response> items = result.items().stream().map(customerService::toResponse).toList();
		return Envelope.of(items, PageMeta.of(page, pageSize, result.total()));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Envelope<CustomerDtos.Response> create(@RequestBody CustomerDtos.CreateRequest request) {
		Customer customer = customerService.create(request);
		return Envelope.of(customerService.toResponse(customer));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String id) {
		customerService.delete(id);
	}
}

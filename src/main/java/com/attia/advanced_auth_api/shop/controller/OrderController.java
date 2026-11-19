package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.common.PageMeta;
import com.attia.advanced_auth_api.shop.document.Order;
import com.attia.advanced_auth_api.shop.document.OrderStatus;
import com.attia.advanced_auth_api.shop.dto.OrderDtos;
import com.attia.advanced_auth_api.shop.service.OrderService;
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

/** backend-api-spec.md §11. */
@RestController
@RequestMapping("/api/v1")
public class OrderController {
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("/orders")
	@ResponseStatus(HttpStatus.CREATED)
	public Envelope<Order> checkout(@RequestBody OrderDtos.CreateRequest request) {
		return Envelope.of(orderService.checkout(request));
	}

	@GetMapping("/admin/orders")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<Order>> list(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int pageSize) {
		OrderStatus parsedStatus = status == null || status.isBlank() ? null : OrderStatus.fromLabel(status);
		OrderService.PageResult result = orderService.search(new OrderService.SearchParams(q, parsedStatus, page, pageSize));
		return Envelope.of(result.items(), PageMeta.of(page, pageSize, result.total()));
	}

	@GetMapping("/admin/orders/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<Order> get(@PathVariable String id) {
		return Envelope.of(orderService.get(id));
	}

	@PatchMapping("/admin/orders/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<Order> updateStatus(@PathVariable String id, @RequestBody OrderDtos.StatusUpdateRequest request) {
		return Envelope.of(orderService.updateStatus(id, request.status()));
	}

	@DeleteMapping("/admin/orders/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String id) {
		orderService.delete(id);
	}
}

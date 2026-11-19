package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.dto.CartDtos;
import com.attia.advanced_auth_api.shop.service.CartService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** backend-api-spec.md §10. */
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@PostMapping("/quote")
	public Envelope<CartDtos.QuoteResponse> quote(@RequestBody CartDtos.QuoteRequest request) {
		return Envelope.of(cartService.quote(request));
	}
}

package com.attia.advanced_auth_api.shop.dto;

import com.attia.advanced_auth_api.shop.document.OrderStatus;
import java.util.List;

/** backend-api-spec.md §11. */
public final class OrderDtos {
	private OrderDtos() {
	}

	public record CustomerRequest(
			String firstName,
			String lastName,
			String phone,
			String email,
			String street,
			String city,
			String note) {
	}

	public record CreateRequest(List<CartDtos.ItemRequest> items, String promoCode, CustomerRequest customer) {
	}

	public record StatusUpdateRequest(OrderStatus status) {
	}
}

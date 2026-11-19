package com.attia.advanced_auth_api.shop.document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** backend-api-spec.md §2.5. */
@Document(collection = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
	@Id
	private String id;
	private Instant date;
	@Builder.Default
	private List<OrderItem> items = new ArrayList<>();
	private int subtotal;
	private int discount;
	private int shipping;
	private int total;
	private OrderCustomer customer;
	@Builder.Default
	private OrderStatus status = OrderStatus.EN_ATTENTE;
}

package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.Ids;
import com.attia.advanced_auth_api.shop.document.Order;
import com.attia.advanced_auth_api.shop.document.OrderCustomer;
import com.attia.advanced_auth_api.shop.document.OrderItem;
import com.attia.advanced_auth_api.shop.document.OrderStatus;
import com.attia.advanced_auth_api.shop.dto.CartDtos;
import com.attia.advanced_auth_api.shop.dto.OrderDtos;
import com.attia.advanced_auth_api.shop.repository.OrderRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/** backend-api-spec.md §2.5 / §11 — orders / checkout. */
@Service
public class OrderService {
	private final OrderRepository orderRepository;
	private final MongoTemplate mongoTemplate;
	private final CartService cartService;

	public OrderService(OrderRepository orderRepository, MongoTemplate mongoTemplate, CartService cartService) {
		this.orderRepository = orderRepository;
		this.mongoTemplate = mongoTemplate;
		this.cartService = cartService;
	}

	/** §11.1 — never trust a client-submitted price/subtotal/discount/total; recompute via §10. */
	public Order checkout(OrderDtos.CreateRequest request) {
		if (request.items() == null || request.items().isEmpty()) {
			throw ApiException.validation("Le panier est vide.");
		}
		OrderDtos.CustomerRequest c = request.customer();
		if (c == null || isBlank(c.firstName()) || isBlank(c.lastName()) || isBlank(c.street())
				|| isBlank(c.city()) || isBlank(c.phone())) {
			throw ApiException.validation(
					"Prénom, nom, téléphone, adresse et ville sont requis pour la commande.");
		}

		CartDtos.QuoteResponse quote = cartService.quote(new CartDtos.QuoteRequest(request.items(), request.promoCode()));

		List<OrderItem> items = quote.getLines().stream()
				.map(line -> OrderItem.builder()
						.id(line.getId())
						.sku(line.getSku())
						.name(line.getName())
						.price(line.getUnitPrice())
						.qty(line.getQty())
						.build())
				.toList();

		OrderCustomer customer = OrderCustomer.builder()
				.name((c.firstName() + " " + c.lastName()).trim())
				.phone(c.phone())
				.email(c.email() == null ? "" : c.email())
				.address(c.street())
				.city(c.city())
				.note(c.note() == null ? "" : c.note())
				.build();

		Order order = Order.builder()
				.id(Ids.order())
				.date(Instant.now())
				.items(items)
				.subtotal(quote.getSubtotal())
				.discount(quote.getDiscount())
				.shipping(quote.getShipping())
				.total(quote.getTotal())
				.customer(customer)
				.status(OrderStatus.EN_ATTENTE)
				.build();
		return orderRepository.save(order);
	}

	public record SearchParams(String q, OrderStatus status, int page, int pageSize) {
	}

	public record PageResult(List<Order> items, long total) {
	}

	public PageResult search(SearchParams params) {
		Query query = new Query();
		if (params.status() != null) {
			query.addCriteria(Criteria.where("status").is(params.status()));
		}
		if (params.q() != null && !params.q().isBlank()) {
			String regex = java.util.regex.Pattern.quote(params.q());
			query.addCriteria(new Criteria().orOperator(
					Criteria.where("id").regex(regex, "i"),
					Criteria.where("customer.name").regex(regex, "i")));
		}
		long total = mongoTemplate.count(query, Order.class);
		int page = Math.max(params.page(), 1);
		int pageSize = params.pageSize() > 0 ? params.pageSize() : 10;
		query.with(PageRequest.of(page - 1, pageSize, org.springframework.data.domain.Sort.by(
				org.springframework.data.domain.Sort.Direction.DESC, "date")));
		return new PageResult(mongoTemplate.find(query, Order.class), total);
	}

	public Order get(String id) {
		return orderRepository.findById(id).orElseThrow(() -> ApiException.notFound("Commande introuvable."));
	}

	public Order updateStatus(String id, OrderStatus status) {
		Order order = get(id);
		order.setStatus(status);
		return orderRepository.save(order);
	}

	public void delete(String id) {
		get(id);
		orderRepository.deleteById(id);
	}

	public List<Order> all() {
		return orderRepository.findAll();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

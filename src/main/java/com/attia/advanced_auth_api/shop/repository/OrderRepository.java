package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.Order;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
	Page<Order> findByDateBetweenOrderByDateDesc(Instant from, Instant to, Pageable pageable);

	long count();
}

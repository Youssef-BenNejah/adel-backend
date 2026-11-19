package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {
	boolean existsByEmailIgnoreCase(String email);

	long count();
}

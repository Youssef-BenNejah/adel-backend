package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.Product;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
	long countByCategory(String category);

	List<Product> findByCategory(String category);
}

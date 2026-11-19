package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.Category;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
	List<Category> findAllByOrderByOrderAsc();

	boolean existsBySlug(String slug);

	long count();
}

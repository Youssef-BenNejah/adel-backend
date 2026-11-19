package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.Promotion;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PromotionRepository extends MongoRepository<Promotion, String> {
	List<Promotion> findByActiveTrue();

	long countByActiveTrue();
}

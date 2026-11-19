package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.Pack;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PackRepository extends MongoRepository<Pack, String> {
	List<Pack> findByActiveTrueOrderByOrderAsc();

	List<Pack> findAllByOrderByOrderAsc();
}

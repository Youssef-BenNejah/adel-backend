package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.HeroSlide;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HeroSlideRepository extends MongoRepository<HeroSlide, String> {
	List<HeroSlide> findAllByOrderByOrderAsc();

	List<HeroSlide> findByActiveTrueOrderByOrderAsc();
}

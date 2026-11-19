package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.GalleryImage;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GalleryImageRepository extends MongoRepository<GalleryImage, String> {
	List<GalleryImage> findAllByOrderByOrderAsc();
}

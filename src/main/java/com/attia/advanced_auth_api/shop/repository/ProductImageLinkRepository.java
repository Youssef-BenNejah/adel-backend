package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.ProductImageLink;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductImageLinkRepository extends MongoRepository<ProductImageLink, String> {
	List<ProductImageLink> findByProductIdOrderByPositionAsc(String productId);

	List<ProductImageLink> findByProductIdInOrderByPositionAsc(List<String> productIds);

	long countByMediaId(String mediaId);

	void deleteByProductId(String productId);

	void deleteByProductIdAndPosition(String productId, int position);
}

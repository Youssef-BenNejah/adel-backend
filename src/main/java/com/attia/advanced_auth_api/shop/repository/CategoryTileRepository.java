package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.CategoryTile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryTileRepository extends MongoRepository<CategoryTile, String> {
}

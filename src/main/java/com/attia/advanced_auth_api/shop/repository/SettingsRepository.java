package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.Settings;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SettingsRepository extends MongoRepository<Settings, String> {
}

package com.attia.advanced_auth_api.repository;

import com.attia.advanced_auth_api.document.RefreshTokenDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenRepository extends MongoRepository<RefreshTokenDocument, String> {
	List<RefreshTokenDocument> findByUserIdAndRevokedFalse(String userId);
	Optional<RefreshTokenDocument> findByTokenHash(String tokenHash);
}
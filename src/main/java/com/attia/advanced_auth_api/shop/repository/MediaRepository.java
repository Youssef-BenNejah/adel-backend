package com.attia.advanced_auth_api.shop.repository;

import com.attia.advanced_auth_api.shop.document.Media;
import com.attia.advanced_auth_api.shop.document.MediaStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MediaRepository extends MongoRepository<Media, String> {
	Optional<Media> findFirstByChecksumAndStatus(String checksum, MediaStatus status);

	List<Media> findByStatusAndCreatedAtBefore(MediaStatus status, Instant before);
}

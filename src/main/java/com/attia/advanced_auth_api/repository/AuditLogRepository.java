package com.attia.advanced_auth_api.repository;

import com.attia.advanced_auth_api.document.AuditLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLogDocument, String> {
}
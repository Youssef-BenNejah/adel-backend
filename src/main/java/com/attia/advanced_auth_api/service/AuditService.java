package com.attia.advanced_auth_api.service;

import com.attia.advanced_auth_api.document.AuditAction;
import com.attia.advanced_auth_api.document.AuditLogDocument;
import com.attia.advanced_auth_api.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
	private final AuditLogRepository repository;

	public AuditService(AuditLogRepository repository) {
		this.repository = repository;
	}

	@Async("auditTaskExecutor")
	public void log(String userId, AuditAction action, String ipAddress, String userAgent) {
		AuditLogDocument doc = AuditLogDocument.builder()
				.userId(userId)
				.action(action)
				.ipAddress(ipAddress)
				.userAgent(userAgent)
				.build();
		repository.save(doc);
	}
}

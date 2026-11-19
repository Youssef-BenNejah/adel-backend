package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.ErrorCode;
import com.attia.advanced_auth_api.shop.common.Ids;
import com.attia.advanced_auth_api.shop.document.Customer;
import com.attia.advanced_auth_api.shop.dto.CustomerDtos;
import com.attia.advanced_auth_api.shop.repository.CustomerRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** backend-api-spec.md §2.6 / §12 — admin-managed CRM contacts, not storefront accounts. */
@Service
public class CustomerService {
	private final CustomerRepository customerRepository;
	private final MongoTemplate mongoTemplate;
	private final PasswordEncoder passwordEncoder;

	public CustomerService(CustomerRepository customerRepository, MongoTemplate mongoTemplate, PasswordEncoder passwordEncoder) {
		this.customerRepository = customerRepository;
		this.mongoTemplate = mongoTemplate;
		this.passwordEncoder = passwordEncoder;
	}

	public record PageResult(List<Customer> items, long total) {
	}

	public PageResult search(String q, int page, int pageSize) {
		Query query = new Query();
		if (q != null && !q.isBlank()) {
			String regex = java.util.regex.Pattern.quote(q);
			query.addCriteria(new Criteria().orOperator(
					Criteria.where("name").regex(regex, "i"),
					Criteria.where("email").regex(regex, "i")));
		}
		long total = mongoTemplate.count(query, Customer.class);
		int p = Math.max(page, 1);
		int size = pageSize > 0 ? pageSize : 10;
		query.with(PageRequest.of(p - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
		return new PageResult(mongoTemplate.find(query, Customer.class), total);
	}

	public Customer create(CustomerDtos.CreateRequest request) {
		if (isBlank(request.name()) || isBlank(request.email()) || isBlank(request.password())) {
			throw ApiException.validation("name, email et password sont requis.");
		}
		String email = request.email().trim().toLowerCase();
		if (customerRepository.existsByEmailIgnoreCase(email)) {
			throw new ApiException(org.springframework.http.HttpStatus.CONFLICT, ErrorCode.EMAIL_TAKEN,
					"Un compte existe déjà avec cet e-mail.");
		}
		Customer customer = Customer.builder()
				.id(Ids.customer())
				.name(request.name())
				.email(email)
				.phone(request.phone() == null ? "" : request.phone())
				.passwordHash(passwordEncoder.encode(request.password()))
				.createdAt(Instant.now())
				.status("active")
				.build();
		return customerRepository.save(customer);
	}

	public void delete(String id) {
		if (!customerRepository.existsById(id)) {
			throw ApiException.notFound("Client introuvable.");
		}
		customerRepository.deleteById(id);
	}

	public CustomerDtos.Response toResponse(Customer customer) {
		return CustomerDtos.Response.builder()
				.id(customer.getId())
				.name(customer.getName())
				.email(customer.getEmail())
				.phone(customer.getPhone())
				.createdAt(customer.getCreatedAt())
				.status(customer.getStatus())
				.build();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

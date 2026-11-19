package com.attia.advanced_auth_api.shop.document;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** CRM record (admin-managed lead), not a storefront account — backend-api-spec.md §2.6. */
@Document(collection = "shop_customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
	@Id
	private String id;
	private String name;
	@Indexed(unique = true)
	private String email;
	@Builder.Default
	private String phone = "";
	/** Bcrypt-hashed, never returned in any response. */
	private String passwordHash;
	@CreatedDate
	private Instant createdAt;
	@Builder.Default
	private String status = "active";
}

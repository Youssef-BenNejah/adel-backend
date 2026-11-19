package com.attia.advanced_auth_api.dto;

import com.attia.advanced_auth_api.document.Role;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
	private String id;
	private String email;
	private Role role;
	private Instant createdAt;
}

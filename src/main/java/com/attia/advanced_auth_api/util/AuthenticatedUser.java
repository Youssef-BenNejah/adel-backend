package com.attia.advanced_auth_api.util;

import com.attia.advanced_auth_api.document.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthenticatedUser {
	private final String id;
	private final String email;
	private final Role role;
}

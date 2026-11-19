package com.attia.advanced_auth_api.shop.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Singleton — backend-api-spec.md §2.7. */
@Document(collection = "shop_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settings {
	public static final String SINGLETON_ID = "default";

	@Id
	@Builder.Default
	private String id = SINGLETON_ID;
	@Builder.Default
	private String storeName = "Adel Parfum";
	@Builder.Default
	private int freeShippingThreshold = 150;
	@Builder.Default
	private int shippingFee = 8;
	@Builder.Default
	private String freeShippingCode = "ADEL10";
}

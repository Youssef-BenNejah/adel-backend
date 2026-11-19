package com.attia.advanced_auth_api.shop.document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;

/**
 * The fixed, ordered status list from backend-api-spec.md §2.5. Serializes over the wire as the
 * French label ("En attente", …) per the spec, not the Java enum constant name — only Mongo
 * persistence (unaffected by the Jackson annotations below) uses the constant name.
 */
public enum OrderStatus {
	EN_ATTENTE("En attente"),
	CONFIRMEE("Confirmée"),
	EXPEDIEE("Expédiée"),
	LIVREE("Livrée"),
	ANNULEE("Annulée");

	private final String label;

	OrderStatus(String label) {
		this.label = label;
	}

	@JsonValue
	public String getLabel() {
		return label;
	}

	public static final List<OrderStatus> ORDERED = List.of(
			EN_ATTENTE, CONFIRMEE, EXPEDIEE, LIVREE, ANNULEE);

	@JsonCreator
	public static OrderStatus fromLabel(String label) {
		for (OrderStatus status : ORDERED) {
			if (status.label.equalsIgnoreCase(label) || status.name().equalsIgnoreCase(label)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown order status: " + label);
	}
}

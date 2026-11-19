package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.Ids;
import com.attia.advanced_auth_api.shop.document.Pack;
import com.attia.advanced_auth_api.shop.document.Promotion;
import com.attia.advanced_auth_api.shop.dto.PackDtos;
import com.attia.advanced_auth_api.shop.repository.PackRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** backend-api-spec.md §2.4 / §3.9 / §9. */
@Service
public class PackService {
	private final PackRepository packRepository;

	public PackService(PackRepository packRepository) {
		this.packRepository = packRepository;
	}

	public List<Pack> listActive() {
		return packRepository.findByActiveTrueOrderByOrderAsc();
	}

	public List<Pack> listAll() {
		return packRepository.findAllByOrderByOrderAsc();
	}

	public Pack get(String id, boolean requireActive) {
		Pack pack = packRepository.findById(id).orElseThrow(() -> ApiException.notFound("Pack introuvable."));
		if (requireActive && !pack.isActive()) {
			throw ApiException.notFound("Pack introuvable.");
		}
		return pack;
	}

	public Pack create(PackDtos.CreateRequest request) {
		Promotion.Scope scope = request.scope() == null ? Promotion.Scope.all : request.scope();
		Pack pack = Pack.builder()
				.id(Ids.pack())
				.name(blankToDefault(request.name(), "Pack"))
				.price(Math.max(0, request.price() == null ? 0 : request.price()))
				.size(clamp(request.size() == null ? 3 : request.size(), 1, 10))
				.scope(scope)
				.productIds(scope == Promotion.Scope.all ? List.of() : safe(request.productIds()))
				.active(request.active() == null || request.active())
				.order((int) packRepository.findAllByOrderByOrderAsc().size())
				.build();
		return packRepository.save(pack);
	}

	public Pack update(String id, PackDtos.UpdateRequest request) {
		Pack pack = get(id, false);
		if (request.name() != null) {
			pack.setName(blankToDefault(request.name(), "Pack"));
		}
		if (request.price() != null) {
			pack.setPrice(Math.max(0, request.price()));
		}
		if (request.size() != null) {
			pack.setSize(clamp(request.size(), 1, 10));
		}
		Promotion.Scope scope = request.scope() != null ? request.scope() : pack.getScope();
		pack.setScope(scope);
		if (scope == Promotion.Scope.all) {
			pack.setProductIds(List.of());
		} else if (request.productIds() != null) {
			pack.setProductIds(request.productIds());
		}
		if (request.active() != null) {
			pack.setActive(request.active());
		}
		return packRepository.save(pack);
	}

	public void delete(String id) {
		get(id, false);
		packRepository.deleteById(id);
		List<Pack> remaining = packRepository.findAllByOrderByOrderAsc();
		for (int i = 0; i < remaining.size(); i++) {
			remaining.get(i).setOrder(i);
		}
		packRepository.saveAll(remaining);
	}

	public Pack toggle(String id) {
		Pack pack = get(id, false);
		pack.setActive(!pack.isActive());
		return packRepository.save(pack);
	}

	/** §3.9 — every slot must be filled with an eligible product. */
	public boolean isEligible(Pack pack, String productId) {
		return pack.getScope() == Promotion.Scope.all || pack.getProductIds().contains(productId);
	}

	private String blankToDefault(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value.trim();
	}

	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private List<String> safe(List<String> values) {
		return values == null ? new ArrayList<>() : values;
	}
}

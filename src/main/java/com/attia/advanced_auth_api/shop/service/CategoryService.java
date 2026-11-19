package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.Slugs;
import com.attia.advanced_auth_api.shop.document.Category;
import com.attia.advanced_auth_api.shop.dto.CategoryDtos;
import com.attia.advanced_auth_api.shop.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/** backend-api-spec.md §3.1 / §5. */
@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public List<Category> list() {
		return categoryRepository.findAllByOrderByOrderAsc();
	}

	public Category get(String slug) {
		return categoryRepository.findById(slug).orElseThrow(() -> ApiException.notFound("Catégorie introuvable."));
	}

	public Category create(CategoryDtos.CreateRequest request) {
		if (request.label() == null || request.label().isBlank()) {
			throw ApiException.validation("Le libellé est requis.");
		}
		String base = Slugs.slugify(request.label());
		String slug = Slugs.uniqueSlug(base, categoryRepository::existsBySlug);
		Category category = Category.builder()
				.slug(slug)
				.label(request.label().trim())
				.sub(request.sub() == null ? "" : request.sub())
				.order((int) categoryRepository.count())
				.build();
		return categoryRepository.save(category);
	}

	/** §5.4 — {@code slug} is immutable; editing the label never regenerates it. */
	public Category update(String slug, CategoryDtos.UpdateRequest request) {
		Category category = get(slug);
		if (request.label() != null) {
			category.setLabel(request.label());
		}
		if (request.sub() != null) {
			category.setSub(request.sub());
		}
		if (request.order() != null) {
			category.setOrder(request.order());
		}
		return categoryRepository.save(category);
	}

	/** §5.5 — no cascade; remaining categories' order is re-compacted afterward. */
	public void delete(String slug) {
		get(slug);
		categoryRepository.deleteById(slug);
		List<Category> remaining = categoryRepository.findAllByOrderByOrderAsc();
		for (int i = 0; i < remaining.size(); i++) {
			remaining.get(i).setOrder(i);
		}
		categoryRepository.saveAll(remaining);
	}
}

package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.Ids;
import com.attia.advanced_auth_api.shop.document.Category;
import com.attia.advanced_auth_api.shop.document.Notes;
import com.attia.advanced_auth_api.shop.document.Product;
import com.attia.advanced_auth_api.shop.document.ProductTag;
import com.attia.advanced_auth_api.shop.document.Promotion;
import com.attia.advanced_auth_api.shop.document.Size;
import com.attia.advanced_auth_api.shop.dto.ProductDtos;
import com.attia.advanced_auth_api.shop.repository.CategoryRepository;
import com.attia.advanced_auth_api.shop.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/** backend-api-spec.md §2.2 / §3.2 / §3.6 / §6. */
@Service
public class ProductService {
	private static final int DEFAULT_PAGE_SIZE = 12;

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final MongoTemplate mongoTemplate;
	private final PricingService pricingService;
	private final PromotionService promotionService;
	private final ProductImageLinkService imageLinkService;

	public ProductService(
			ProductRepository productRepository,
			CategoryRepository categoryRepository,
			MongoTemplate mongoTemplate,
			PricingService pricingService,
			PromotionService promotionService,
			ProductImageLinkService imageLinkService) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.mongoTemplate = mongoTemplate;
		this.pricingService = pricingService;
		this.promotionService = promotionService;
		this.imageLinkService = imageLinkService;
	}

	public record SearchParams(
			List<String> category, String family, String q, Integer priceMin, Integer priceMax,
			ProductTag tag, String sort, int page, int pageSize) {
	}

	public record PageResult(List<Product> items, long total) {
	}

	public PageResult search(SearchParams params) {
		Query query = new Query();
		List<Criteria> criteria = new ArrayList<>();
		if (params.category() != null && !params.category().isEmpty()) {
			criteria.add(Criteria.where("category").in(params.category()));
		}
		if (params.family() != null && !params.family().isBlank()) {
			criteria.add(Criteria.where("family").is(params.family()));
		}
		if (params.tag() != null) {
			criteria.add(Criteria.where("tag").is(params.tag()));
		}
		if (params.q() != null && !params.q().isBlank()) {
			String regex = java.util.regex.Pattern.quote(params.q());
			criteria.add(new Criteria().orOperator(
					Criteria.where("name").regex(regex, "i"),
					Criteria.where("sku").regex(regex, "i"),
					Criteria.where("family").regex(regex, "i")));
		}
		if (params.priceMin() != null) {
			criteria.add(Criteria.where("sizes.0.price").gte(params.priceMin()));
		}
		if (params.priceMax() != null) {
			criteria.add(Criteria.where("sizes.0.price").lte(params.priceMax()));
		}
		if (!criteria.isEmpty()) {
			query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
		}

		long total = mongoTemplate.count(query, Product.class);

		Sort sort = switch (params.sort() == null ? "default" : params.sort()) {
			case "price-asc" -> Sort.by(Sort.Direction.ASC, "sizes.0.price");
			case "price-desc" -> Sort.by(Sort.Direction.DESC, "sizes.0.price");
			case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
			default -> Sort.unsorted();
		};
		int page = Math.max(params.page(), 1);
		int pageSize = params.pageSize() > 0 ? params.pageSize() : DEFAULT_PAGE_SIZE;
		query.with(PageRequest.of(page - 1, pageSize, sort));
		List<Product> items = mongoTemplate.find(query, Product.class);
		return new PageResult(items, total);
	}

	public Product get(String id) {
		return productRepository.findById(id).orElseThrow(() -> ApiException.notFound("Produit introuvable."));
	}

	public List<Product> related(String id, int limit) {
		Product product = get(id);
		return productRepository.findByCategory(product.getCategory()).stream()
				.filter(p -> !p.getId().equals(id))
				.limit(limit)
				.toList();
	}

	/** §3.2 — SKU/ID generation on create. */
	public Product create(ProductDtos.CreateRequest request) {
		if (isBlank(request.name()) || isBlank(request.category()) || isBlank(request.family())) {
			throw ApiException.validation("name, category et family sont requis.");
		}
		Category category = categoryRepository.findById(request.category())
				.orElseThrow(() -> ApiException.notFound("Catégorie introuvable: " + request.category()));

		String prefix = category.getSlug().substring(0, 1).toUpperCase(Locale.ROOT);
		long n = productRepository.countByCategory(category.getSlug()) + 1;
		String sku = String.format("%s%03d-C", prefix, n);
		String id = Ids.productCustom(category.getSlug());

		Product product = Product.builder()
				.id(id)
				.sku(sku)
				.name(request.name())
				.category(category.getSlug())
				.family(request.family())
				.description(request.description() == null ? "" : request.description())
				.sizes(normalizeSizes(request.sizes(), 0))
				.oldPrice(request.oldPrice())
				.tag(request.tag())
				.rating("5.0")
				.reviews(0)
				.notes(Notes.builder()
						.top(orEmpty(request.notesTop()))
						.heart(orEmpty(request.notesHeart()))
						.base(orEmpty(request.notesBase()))
						.build())
				.build();
		productRepository.save(product);

		if (request.mediaIds() != null && !request.mediaIds().isEmpty()) {
			imageLinkService.replace(id, request.mediaIds().stream().limit(3).toList());
		}
		return product;
	}

	public Product update(String id, ProductDtos.UpdateRequest request) {
		Product product = get(id);
		if (request.name() != null) {
			product.setName(request.name());
		}
		if (request.category() != null) {
			categoryRepository.findById(request.category())
					.orElseThrow(() -> ApiException.notFound("Catégorie introuvable: " + request.category()));
			product.setCategory(request.category());
		}
		if (request.family() != null) {
			product.setFamily(request.family());
		}
		if (request.description() != null) {
			product.setDescription(request.description());
		}
		if (request.sizes() != null) {
			product.setSizes(normalizeSizes(request.sizes(), basePriceOf(product)));
		}
		if (request.oldPrice() != null) {
			product.setOldPrice(request.oldPrice());
		}
		if (request.tag() != null) {
			product.setTag(request.tag());
		}
		Notes notes = product.getNotes() == null ? Notes.builder().build() : product.getNotes();
		if (request.notesTop() != null) {
			notes.setTop(request.notesTop());
		}
		if (request.notesHeart() != null) {
			notes.setHeart(request.notesHeart());
		}
		if (request.notesBase() != null) {
			notes.setBase(request.notesBase());
		}
		product.setNotes(notes);
		productRepository.save(product);

		if (request.mediaIds() != null) {
			imageLinkService.replace(id, request.mediaIds().stream().limit(3).toList());
		}
		return product;
	}

	/** §6.6 — hard delete, no cascade against orders/promotions/packs (§17.7, preserved as-is). */
	public void delete(String id) {
		get(id);
		productRepository.deleteById(id);
		imageLinkService.deleteAllForProduct(id);
	}

	/** §2.2's {@code normalizeSizes} fallback rule. */
	private List<Size> normalizeSizes(List<ProductDtos.SizeRequest> requested, int basePrice) {
		if (requested != null && requested.size() == 3) {
			List<Size> sizes = new ArrayList<>();
			for (ProductDtos.SizeRequest s : requested) {
				if (s.price() < 0) {
					throw ApiException.validation("Le prix d'une taille doit être >= 0.");
				}
				sizes.add(Size.builder().ml(s.ml()).price(s.price()).build());
			}
			return sizes;
		}
		return List.of(
				Size.builder().ml(30).price(basePrice).build(),
				Size.builder().ml(50).price(basePrice + 10).build(),
				Size.builder().ml(100).price(basePrice + 24).build());
	}

	private int basePriceOf(Product product) {
		return product.getSizes() != null && !product.getSizes().isEmpty() ? product.getSizes().get(0).getPrice() : 0;
	}

	public ProductDtos.Response enrich(Product product, List<Promotion> activePromotions) {
		PricingService.Resolved resolved = pricingService.resolve(product, activePromotions);
		List<String> images = imageLinkService.urls(product.getId());
		return ProductDtos.Response.builder()
				.id(product.getId())
				.sku(product.getSku())
				.name(product.getName())
				.category(product.getCategory())
				.family(product.getFamily())
				.images(images)
				.image(images.isEmpty() ? null : images.get(0))
				.description(product.getDescription())
				.price(product.getPrice())
				.oldPrice(product.getOldPrice())
				.tag(product.getTag())
				.rating(product.getRating())
				.reviews(product.getReviews())
				.notes(product.getNotes())
				.sizes(product.getSizes())
				.displayPrice(resolved.displayPrice())
				.originalPrice(resolved.originalPrice())
				.appliedPromo(pricingService.toSummary(resolved.appliedPromo()))
				.build();
	}

	public List<ProductDtos.Response> enrichAll(List<Product> products) {
		List<Promotion> active = promotionService.listActive();
		return products.stream().map(p -> enrich(p, active)).toList();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String orEmpty(String value) {
		return value == null ? "" : value;
	}
}

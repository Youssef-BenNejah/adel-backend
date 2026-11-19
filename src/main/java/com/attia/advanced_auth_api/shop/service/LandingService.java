package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.Ids;
import com.attia.advanced_auth_api.shop.document.CategoryTile;
import com.attia.advanced_auth_api.shop.document.GalleryImage;
import com.attia.advanced_auth_api.shop.document.HeroSlide;
import com.attia.advanced_auth_api.shop.dto.LandingDtos;
import com.attia.advanced_auth_api.shop.repository.CategoryTileRepository;
import com.attia.advanced_auth_api.shop.repository.GalleryImageRepository;
import com.attia.advanced_auth_api.shop.repository.HeroSlideRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/** backend-api-spec.md §2.8 / §14 — landing page content, each slot now a {@code mediaId}. */
@Service
public class LandingService {
	private final HeroSlideRepository heroSlideRepository;
	private final CategoryTileRepository categoryTileRepository;
	private final GalleryImageRepository galleryImageRepository;

	public LandingService(
			HeroSlideRepository heroSlideRepository,
			CategoryTileRepository categoryTileRepository,
			GalleryImageRepository galleryImageRepository) {
		this.heroSlideRepository = heroSlideRepository;
		this.categoryTileRepository = categoryTileRepository;
		this.galleryImageRepository = galleryImageRepository;
	}

	// ---- Hero slides (§14.1) ----

	/** Public read: never render an empty carousel — fall back to the full list. */
	public List<HeroSlide> publicHeroSlides() {
		List<HeroSlide> active = heroSlideRepository.findByActiveTrueOrderByOrderAsc();
		return active.isEmpty() ? heroSlideRepository.findAllByOrderByOrderAsc() : active;
	}

	public List<HeroSlide> allHeroSlides() {
		return heroSlideRepository.findAllByOrderByOrderAsc();
	}

	public HeroSlide createHeroSlide(LandingDtos.HeroSlideCreateRequest request) {
		HeroSlide slide = HeroSlide.builder()
				.id(Ids.slide())
				.mediaId(request.mediaId())
				.eyebrow(request.eyebrow() == null ? "" : request.eyebrow())
				.title(request.title() == null ? "" : request.title())
				.active(true)
				.order(heroSlideRepository.findAllByOrderByOrderAsc().size())
				.build();
		return heroSlideRepository.save(slide);
	}

	public HeroSlide updateHeroSlide(String id, LandingDtos.HeroSlideUpdateRequest request) {
		HeroSlide slide = heroSlideRepository.findById(id).orElseThrow(() -> ApiException.notFound("Slide introuvable."));
		if (request.mediaId() != null) {
			slide.setMediaId(request.mediaId());
		}
		if (request.eyebrow() != null) {
			slide.setEyebrow(request.eyebrow());
		}
		if (request.title() != null) {
			slide.setTitle(request.title());
		}
		return heroSlideRepository.save(slide);
	}

	public void deleteHeroSlide(String id) {
		heroSlideRepository.deleteById(id);
		recompactOrder(heroSlideRepository.findAllByOrderByOrderAsc(), heroSlideRepository::saveAll, HeroSlide::setOrder);
	}

	public HeroSlide toggleHeroSlide(String id) {
		HeroSlide slide = heroSlideRepository.findById(id).orElseThrow(() -> ApiException.notFound("Slide introuvable."));
		slide.setActive(!slide.isActive());
		return heroSlideRepository.save(slide);
	}

	public List<HeroSlide> moveHeroSlide(String id, int direction) {
		List<HeroSlide> all = heroSlideRepository.findAllByOrderByOrderAsc();
		swapAdjacent(all, id, direction, HeroSlide::getId, HeroSlide::getOrder, HeroSlide::setOrder);
		heroSlideRepository.saveAll(all);
		return heroSlideRepository.findAllByOrderByOrderAsc();
	}

	// ---- Category tiles (§14.2) ----

	public java.util.Map<String, String> publicCategoryTiles() {
		java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
		for (CategoryTile tile : categoryTileRepository.findAll()) {
			map.put(tile.getCategorySlug(), tile.getMediaId());
		}
		return map;
	}

	public CategoryTile setCategoryTile(String slug, String mediaId) {
		CategoryTile tile = categoryTileRepository.findById(slug)
				.orElse(CategoryTile.builder().categorySlug(slug).build());
		tile.setMediaId(mediaId);
		return categoryTileRepository.save(tile);
	}

	/** Reset to {@code null} — the map key conceptually always exists. */
	public CategoryTile resetCategoryTile(String slug) {
		return setCategoryTile(slug, null);
	}

	// ---- Gallery (§14.3) ----

	public List<GalleryImage> allGallery() {
		return galleryImageRepository.findAllByOrderByOrderAsc();
	}

	public GalleryImage createGalleryImage(String mediaId) {
		GalleryImage image = GalleryImage.builder()
				.id(Ids.gallery())
				.mediaId(mediaId)
				.order(galleryImageRepository.findAllByOrderByOrderAsc().size())
				.build();
		return galleryImageRepository.save(image);
	}

	public GalleryImage updateGalleryImage(String id, String mediaId) {
		GalleryImage image = galleryImageRepository.findById(id)
				.orElseThrow(() -> ApiException.notFound("Image de galerie introuvable."));
		image.setMediaId(mediaId);
		return galleryImageRepository.save(image);
	}

	public void deleteGalleryImage(String id) {
		galleryImageRepository.deleteById(id);
		recompactOrder(galleryImageRepository.findAllByOrderByOrderAsc(), galleryImageRepository::saveAll, GalleryImage::setOrder);
	}

	public List<GalleryImage> moveGalleryImage(String id, int direction) {
		List<GalleryImage> all = galleryImageRepository.findAllByOrderByOrderAsc();
		swapAdjacent(all, id, direction, GalleryImage::getId, GalleryImage::getOrder, GalleryImage::setOrder);
		galleryImageRepository.saveAll(all);
		return galleryImageRepository.findAllByOrderByOrderAsc();
	}

	// ---- shared order helpers ----

	private <T> void recompactOrder(List<T> items, java.util.function.Consumer<List<T>> saveAll, java.util.function.BiConsumer<T, Integer> setOrder) {
		for (int i = 0; i < items.size(); i++) {
			setOrder.accept(items.get(i), i);
		}
		saveAll.accept(items);
	}

	/** §14.1/§14.3 move semantics — swap with the adjacent row; no-op (not an error) at an edge. */
	private <T> void swapAdjacent(
			List<T> all, String id, int direction,
			java.util.function.Function<T, String> getId,
			java.util.function.Function<T, Integer> getOrder,
			java.util.function.BiConsumer<T, Integer> setOrder) {
		all.sort(Comparator.comparing(getOrder));
		int index = -1;
		for (int i = 0; i < all.size(); i++) {
			if (getId.apply(all.get(i)).equals(id)) {
				index = i;
				break;
			}
		}
		if (index < 0) {
			throw ApiException.notFound("Introuvable.");
		}
		int targetIndex = index + direction;
		if (targetIndex < 0 || targetIndex >= all.size()) {
			return;
		}
		T current = all.get(index);
		T target = all.get(targetIndex);
		int currentOrder = getOrder.apply(current);
		int targetOrder = getOrder.apply(target);
		setOrder.accept(current, targetOrder);
		setOrder.accept(target, currentOrder);
	}
}

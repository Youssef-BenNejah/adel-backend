package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.document.CategoryTile;
import com.attia.advanced_auth_api.shop.document.GalleryImage;
import com.attia.advanced_auth_api.shop.document.HeroSlide;
import com.attia.advanced_auth_api.shop.dto.LandingDtos;
import com.attia.advanced_auth_api.shop.service.LandingService;
import com.attia.advanced_auth_api.shop.service.MediaService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** backend-api-spec.md §14 — landing page content. */
@RestController
@RequestMapping("/api/v1")
public class LandingController {
	private final LandingService landingService;
	private final MediaService mediaService;

	public LandingController(LandingService landingService, MediaService mediaService) {
		this.landingService = landingService;
		this.mediaService = mediaService;
	}

	// ---- Hero slides ----

	@GetMapping("/landing/hero-slides")
	public Envelope<List<LandingDtos.HeroSlideResponse>> publicHeroSlides() {
		return Envelope.of(landingService.publicHeroSlides().stream().map(this::toResponse).toList());
	}

	@GetMapping("/admin/landing/hero-slides")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<LandingDtos.HeroSlideResponse>> adminHeroSlides() {
		return Envelope.of(landingService.allHeroSlides().stream().map(this::toResponse).toList());
	}

	@PostMapping("/admin/landing/hero-slides")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Envelope<LandingDtos.HeroSlideResponse> createHeroSlide(@RequestBody LandingDtos.HeroSlideCreateRequest request) {
		return Envelope.of(toResponse(landingService.createHeroSlide(request)));
	}

	@PatchMapping("/admin/landing/hero-slides/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<LandingDtos.HeroSlideResponse> updateHeroSlide(
			@PathVariable String id, @RequestBody LandingDtos.HeroSlideUpdateRequest request) {
		return Envelope.of(toResponse(landingService.updateHeroSlide(id, request)));
	}

	@DeleteMapping("/admin/landing/hero-slides/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteHeroSlide(@PathVariable String id) {
		landingService.deleteHeroSlide(id);
	}

	@PatchMapping("/admin/landing/hero-slides/{id}/toggle")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<LandingDtos.HeroSlideResponse> toggleHeroSlide(@PathVariable String id) {
		return Envelope.of(toResponse(landingService.toggleHeroSlide(id)));
	}

	@PatchMapping("/admin/landing/hero-slides/{id}/move")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<LandingDtos.HeroSlideResponse>> moveHeroSlide(
			@PathVariable String id, @RequestBody LandingDtos.MoveRequest request) {
		return Envelope.of(landingService.moveHeroSlide(id, request.direction()).stream().map(this::toResponse).toList());
	}

	// ---- Category tiles ----

	@GetMapping("/landing/category-tiles")
	public Envelope<Map<String, String>> publicCategoryTiles() {
		Map<String, String> resolved = new LinkedHashMap<>();
		landingService.publicCategoryTiles().forEach((slug, mediaId) ->
				resolved.put(slug, mediaId == null ? null : mediaService.findMedia(mediaId).map(m -> m.getUrl()).orElse(null)));
		return Envelope.of(resolved);
	}

	@PutMapping("/admin/landing/category-tiles/{slug}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<CategoryTile> setCategoryTile(
			@PathVariable String slug, @RequestBody LandingDtos.CategoryTileSetRequest request) {
		return Envelope.of(landingService.setCategoryTile(slug, request.mediaId()));
	}

	@DeleteMapping("/admin/landing/category-tiles/{slug}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<Map<String, Object>> resetCategoryTile(@PathVariable String slug) {
		landingService.resetCategoryTile(slug);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("image", null);
		return Envelope.of(body);
	}

	// ---- Gallery ----

	@GetMapping("/landing/gallery")
	public Envelope<List<LandingDtos.GalleryResponse>> publicGallery() {
		return Envelope.of(landingService.allGallery().stream().map(this::toResponse).toList());
	}

	@GetMapping("/admin/landing/gallery")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<LandingDtos.GalleryResponse>> adminGallery() {
		return Envelope.of(landingService.allGallery().stream().map(this::toResponse).toList());
	}

	@PostMapping("/admin/landing/gallery")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Envelope<LandingDtos.GalleryResponse> createGalleryImage(@RequestBody LandingDtos.GalleryCreateRequest request) {
		return Envelope.of(toResponse(landingService.createGalleryImage(request.mediaId())));
	}

	@PatchMapping("/admin/landing/gallery/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<LandingDtos.GalleryResponse> updateGalleryImage(
			@PathVariable String id, @RequestBody LandingDtos.GalleryUpdateRequest request) {
		return Envelope.of(toResponse(landingService.updateGalleryImage(id, request.mediaId())));
	}

	@DeleteMapping("/admin/landing/gallery/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteGalleryImage(@PathVariable String id) {
		landingService.deleteGalleryImage(id);
	}

	@PatchMapping("/admin/landing/gallery/{id}/move")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<LandingDtos.GalleryResponse>> moveGalleryImage(
			@PathVariable String id, @RequestBody LandingDtos.MoveRequest request) {
		return Envelope.of(landingService.moveGalleryImage(id, request.direction()).stream().map(this::toResponse).toList());
	}

	private LandingDtos.HeroSlideResponse toResponse(HeroSlide slide) {
		String image = slide.getMediaId() == null ? null : mediaService.findMedia(slide.getMediaId()).map(m -> m.getUrl()).orElse(null);
		return LandingDtos.HeroSlideResponse.builder()
				.id(slide.getId())
				.image(image)
				.eyebrow(slide.getEyebrow())
				.title(slide.getTitle())
				.active(slide.isActive())
				.order(slide.getOrder())
				.build();
	}

	private LandingDtos.GalleryResponse toResponse(GalleryImage image) {
		String url = image.getMediaId() == null ? null : mediaService.findMedia(image.getMediaId()).map(m -> m.getUrl()).orElse(null);
		return LandingDtos.GalleryResponse.builder().id(image.getId()).image(url).order(image.getOrder()).build();
	}
}

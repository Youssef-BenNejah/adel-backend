package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.ErrorCode;
import com.attia.advanced_auth_api.shop.document.Media;
import com.attia.advanced_auth_api.shop.document.ProductImageLink;
import com.attia.advanced_auth_api.shop.dto.ProductImageDtos;
import com.attia.advanced_auth_api.shop.repository.ProductImageLinkRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** image-upload-api.md §5 — enforces "max 3, ordered" server-side, independent of how it's called. */
@Service
public class ProductImageLinkService {
	private static final int MAX_PER_PRODUCT = 3;

	private final ProductImageLinkRepository linkRepository;
	private final MediaService mediaService;

	public ProductImageLinkService(ProductImageLinkRepository linkRepository, MediaService mediaService) {
		this.linkRepository = linkRepository;
		this.mediaService = mediaService;
	}

	public List<ProductImageDtos.LinkResponse> list(String productId) {
		return linkRepository.findByProductIdOrderByPositionAsc(productId).stream()
				.flatMap(link -> mediaService.findMedia(link.getMediaId()).map(media -> toResponse(link, media)).stream())
				.toList();
	}

	/** A missing Media (e.g. a dangling link left by a prior bug) degrades to "no image" instead of a 404. */
	public List<String> urls(String productId) {
		return linkRepository.findByProductIdOrderByPositionAsc(productId).stream()
				.flatMap(link -> mediaService.findMedia(link.getMediaId()).map(com.attia.advanced_auth_api.shop.dto.MediaDtos.Response::getUrl).stream())
				.toList();
	}

	/** §5.2 — replace the whole ordered set; the single idempotent call the admin UI needs. */
	public List<ProductImageDtos.LinkResponse> replace(String productId, List<String> mediaIds) {
		if (mediaIds.size() > MAX_PER_PRODUCT) {
			throw ApiException.unprocessable(ErrorCode.UNPROCESSABLE,
					"Un produit ne peut avoir plus de " + MAX_PER_PRODUCT + " photos.");
		}
		List<Media> readyMedia = new ArrayList<>();
		for (String mediaId : mediaIds) {
			readyMedia.add(mediaService.requireReady(mediaId));
		}
		linkRepository.deleteByProductId(productId);
		List<ProductImageLink> saved = new ArrayList<>();
		for (int position = 0; position < readyMedia.size(); position++) {
			saved.add(linkRepository.save(ProductImageLink.builder()
					.id(UUID.randomUUID().toString())
					.productId(productId)
					.mediaId(readyMedia.get(position).getId())
					.position(position)
					.build()));
		}
		return saved.stream()
				.map(link -> toResponse(link, mediaService.getMedia(link.getMediaId())))
				.toList();
	}

	/** §5.3 — append at the next free position. */
	public ProductImageDtos.LinkResponse append(String productId, String mediaId) {
		List<ProductImageLink> existing = linkRepository.findByProductIdOrderByPositionAsc(productId);
		if (existing.size() >= MAX_PER_PRODUCT) {
			throw new ApiException(org.springframework.http.HttpStatus.CONFLICT, ErrorCode.IMAGE_LIMIT_REACHED,
					"Un produit ne peut avoir plus de 3 photos.");
		}
		Media media = mediaService.requireReady(mediaId);
		ProductImageLink link = linkRepository.save(ProductImageLink.builder()
				.id(UUID.randomUUID().toString())
				.productId(productId)
				.mediaId(media.getId())
				.position(existing.size())
				.build());
		return toResponse(link, mediaService.getMedia(media.getId()));
	}

	/** §5.4 — unlink at a position; higher positions are not auto-shifted. */
	public void removeAt(String productId, int position) {
		linkRepository.deleteByProductIdAndPosition(productId, position);
	}

	/** §5.5 — reorder is just §5.2 replayed with the same ids reordered. */
	public List<ProductImageDtos.LinkResponse> reorder(String productId, List<String> order) {
		return replace(productId, order);
	}

	public void deleteAllForProduct(String productId) {
		linkRepository.deleteByProductId(productId);
	}

	private ProductImageDtos.LinkResponse toResponse(ProductImageLink link, com.attia.advanced_auth_api.shop.dto.MediaDtos.Response media) {
		return ProductImageDtos.LinkResponse.builder()
				.position(link.getPosition())
				.media(media)
				.build();
	}
}

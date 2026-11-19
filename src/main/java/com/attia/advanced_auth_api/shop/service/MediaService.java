package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.ErrorCode;
import com.attia.advanced_auth_api.shop.common.Ids;
import com.attia.advanced_auth_api.shop.config.ShopMediaProperties;
import com.attia.advanced_auth_api.shop.document.Media;
import com.attia.advanced_auth_api.shop.document.MediaStatus;
import com.attia.advanced_auth_api.shop.dto.MediaDtos;
import com.attia.advanced_auth_api.shop.repository.MediaRepository;
import com.attia.advanced_auth_api.shop.repository.ProductImageLinkRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * image-upload-api.md — the decoupled Media resource, both upload flows, and its lifecycle.
 * Bytes live in Cloudinary ({@link CloudinaryClient}); this class only owns validation, the
 * {@code Media} row's lifecycle, and composing the three variant widths as Cloudinary
 * transformation URLs (no local resizing or re-uploading needed — §8 is basically free here).
 */
@Service
@Slf4j
public class MediaService {
	private static final Map<String, Integer> VARIANT_WIDTHS = Map.of("thumb", 160, "md", 800, "lg", 1600);

	private final MediaRepository mediaRepository;
	private final ProductImageLinkRepository linkRepository;
	private final CloudinaryClient cloudinary;
	private final ImageProcessingService imageProcessing;
	private final ShopMediaProperties properties;

	public MediaService(
			MediaRepository mediaRepository,
			ProductImageLinkRepository linkRepository,
			CloudinaryClient cloudinary,
			ImageProcessingService imageProcessing,
			ShopMediaProperties properties) {
		this.mediaRepository = mediaRepository;
		this.linkRepository = linkRepository;
		this.cloudinary = cloudinary;
		this.imageProcessing = imageProcessing;
		this.properties = properties;
	}

	/** §3.1 — init: validate before issuing any storage credentials, then hand back signed Cloudinary upload params. */
	public MediaDtos.InitiateUploadResponse initiateUpload(MediaDtos.InitiateUploadRequest request, String adminId) {
		validateMimeAndSize(request.mimeType(), request.bytes());
		String id = Ids.media();
		Instant expiresAt = Instant.now().plus(properties.getPresignExpiryMinutes(), ChronoUnit.MINUTES);
		CloudinaryClient.SignedUpload signed = cloudinary.buildSignedUpload(id);

		Media media = Media.builder()
				.id(id)
				.status(MediaStatus.pending)
				.storageKey(signed.publicId())
				.originalName(sanitizeFilename(request.filename()))
				.uploadedBy(adminId)
				.variants(new LinkedHashMap<>())
				.pendingExpiresAt(expiresAt)
				.createdAt(Instant.now())
				.build();
		mediaRepository.save(media);

		return MediaDtos.InitiateUploadResponse.builder()
				.mediaId(id)
				.uploadUrl(signed.uploadUrl())
				.uploadMethod("POST")
				.expiresAt(expiresAt)
				.apiKey(signed.apiKey())
				.timestamp(signed.timestamp())
				.signature(signed.signature())
				.publicId(signed.publicId())
				.build();
	}

	/** §3.3 — finalize: the browser already PUT the bytes straight to Cloudinary; verify + pull metadata. */
	public MediaDtos.Response completeUpload(String mediaId, String adminId) {
		Media media = mediaRepository.findById(mediaId)
				.orElseThrow(() -> ApiException.notFound("Media introuvable."));
		if (media.getStatus() == MediaStatus.ready) {
			return toResponse(media);
		}
		CloudinaryClient.Resource resource = cloudinary.getResource(media.getStorageKey());
		finalizeReady(media, resource);
		return toResponse(media);
	}

	/** §4 — server-proxied one-shot upload: validate the bytes here, then forward them to Cloudinary. */
	public MediaDtos.Response oneShotUpload(MultipartFile file, String adminId) {
		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (java.io.IOException ex) {
			throw ApiException.validation("Fichier illisible.");
		}
		String sniffedMime = MimeSniffer.sniff(bytes);
		validateMimeAndSize(sniffedMime, bytes.length);
		validateDimensions(imageProcessing.readDimensions(bytes));

		String checksum = sha256(bytes);
		Optional<Media> existing = mediaRepository.findFirstByChecksumAndStatus(checksum, MediaStatus.ready);
		if (existing.isPresent()) {
			return toResponse(existing.get());
		}

		String id = Ids.media();
		CloudinaryClient.Resource resource = cloudinary.upload(bytes, id, file.getOriginalFilename());
		Media media = Media.builder()
				.id(id)
				.status(MediaStatus.pending)
				.storageKey(resource.publicId())
				.originalName(sanitizeFilename(file.getOriginalFilename()))
				.uploadedBy(adminId)
				.variants(new LinkedHashMap<>())
				.checksum(checksum)
				.createdAt(Instant.now())
				.build();
		finalizeReady(media, resource);
		return toResponse(media);
	}

	private void finalizeReady(Media media, CloudinaryClient.Resource resource) {
		validateDimensions(new ImageProcessingService.Dimensions(resource.width(), resource.height()));
		media.setStatus(MediaStatus.ready);
		media.setMimeType(mimeTypeForFormat(resource.format()));
		media.setWidth(resource.width());
		media.setHeight(resource.height());
		media.setBytes(resource.bytes());
		if (media.getChecksum() == null) {
			media.setChecksum(resource.etag());
		}
		media.setUrl(resource.secureUrl());
		media.setStorageKey(resource.publicId());
		media.setPendingExpiresAt(null);

		Map<String, String> variants = new LinkedHashMap<>();
		for (Map.Entry<String, Integer> entry : VARIANT_WIDTHS.entrySet()) {
			variants.put(entry.getKey(), cloudinary.transformationUrl(resource.publicId(), resource.format(), entry.getValue()));
		}
		media.setVariants(variants);
		mediaRepository.save(media);
	}

	private void validateDimensions(ImageProcessingService.Dimensions dims) {
		if (dims.width() < properties.getMinDimension() || dims.height() < properties.getMinDimension()) {
			throw ApiException.unprocessable(ErrorCode.INVALID_IMAGE,
					"Image trop petite (minimum " + properties.getMinDimension() + "x" + properties.getMinDimension() + "px).");
		}
		if (dims.width() > properties.getMaxDimension() || dims.height() > properties.getMaxDimension()) {
			throw ApiException.unprocessable(ErrorCode.INVALID_IMAGE,
					"Image trop grande (maximum " + properties.getMaxDimension() + "x" + properties.getMaxDimension() + "px).");
		}
	}

	public Media requireReady(String mediaId) {
		Media media = mediaRepository.findById(mediaId)
				.orElseThrow(() -> ApiException.notFound("Media introuvable: " + mediaId));
		if (media.getStatus() != MediaStatus.ready) {
			throw ApiException.unprocessable(ErrorCode.UNPROCESSABLE, "Media pas encore prêt: " + mediaId);
		}
		return media;
	}

	public MediaDtos.Response getMedia(String id) {
		Media media = mediaRepository.findById(id).orElseThrow(() -> ApiException.notFound("Media introuvable."));
		return toResponse(media);
	}

	/** Tolerant lookup for image resolution: a dangling link must not 404 its whole product/catalog. */
	public Optional<MediaDtos.Response> findMedia(String id) {
		return mediaRepository.findById(id).map(this::toResponse);
	}

	public List<MediaDtos.Response> listUnused() {
		return mediaRepository.findAll().stream()
				.filter(m -> linkRepository.countByMediaId(m.getId()) == 0)
				.map(this::toResponse)
				.toList();
	}

	/** §13 index — force-delete, bypasses the §10.2 grace window. Still refuses while a product links it (§10.2 intent). */
	public void forceDelete(String id) {
		Media media = mediaRepository.findById(id).orElseThrow(() -> ApiException.notFound("Media introuvable."));
		long linkCount = linkRepository.countByMediaId(id);
		if (linkCount > 0) {
			throw ApiException.conflict(ErrorCode.CONFLICT,
					"Impossible de supprimer: cette image est utilisée par " + linkCount + " produit(s).");
		}
		if (media.getStatus() == MediaStatus.ready) {
			cloudinary.destroy(media.getStorageKey());
		}
		mediaRepository.deleteById(id);
	}

	public MediaDtos.Response toResponse(Media media) {
		long linkCount = linkRepository.countByMediaId(media.getId());
		return MediaDtos.Response.builder()
				.id(media.getId())
				.status(media.getStatus().name())
				.url(media.getUrl())
				.variants(media.getVariants())
				.mimeType(media.getMimeType())
				.width(media.getWidth())
				.height(media.getHeight())
				.bytes(media.getBytes())
				.checksum(media.getChecksum())
				.originalName(media.getOriginalName())
				.uploadedBy(media.getUploadedBy())
				.createdAt(media.getCreatedAt())
				.linkCount(linkCount)
				.build();
	}

	/** §10.1 — reap abandoned presigned uploads every hour. */
	@org.springframework.scheduling.annotation.Scheduled(fixedDelay = 60 * 60 * 1000)
	public void cleanupAbandonedUploads() {
		Instant cutoff = Instant.now().minus(properties.getAbandonedUploadHours(), ChronoUnit.HOURS);
		List<Media> abandoned = mediaRepository.findByStatusAndCreatedAtBefore(MediaStatus.pending, cutoff);
		for (Media media : abandoned) {
			mediaRepository.delete(media);
		}
		if (!abandoned.isEmpty()) {
			log.info("Reaped {} abandoned media uploads", abandoned.size());
		}
	}

	/** §10.2 — hard-delete unlinked media past the undo grace window, once a day. */
	@org.springframework.scheduling.annotation.Scheduled(fixedDelay = 24L * 60 * 60 * 1000)
	public void cleanupOrphanedMedia() {
		Instant cutoff = Instant.now().minus(properties.getOrphanGraceDays(), ChronoUnit.DAYS);
		List<Media> readyMedia = mediaRepository.findByStatusAndCreatedAtBefore(MediaStatus.ready, cutoff);
		int deleted = 0;
		for (Media media : readyMedia) {
			if (linkRepository.countByMediaId(media.getId()) == 0) {
				cloudinary.destroy(media.getStorageKey());
				mediaRepository.delete(media);
				deleted++;
			}
		}
		if (deleted > 0) {
			log.info("Reaped {} orphaned media rows", deleted);
		}
	}

	private void validateMimeAndSize(String mimeType, long bytes) {
		if (!MimeSniffer.ALLOWED.contains(mimeType)) {
			throw ApiException.unprocessable(ErrorCode.INVALID_IMAGE,
					"Type d'image non supporté: " + mimeType);
		}
		if (bytes > properties.getMaxBytes()) {
			throw ApiException.unprocessable(ErrorCode.INVALID_IMAGE,
					"Fichier trop volumineux (maximum " + (properties.getMaxBytes() / (1024 * 1024)) + " Mo).");
		}
	}

	private String mimeTypeForFormat(String format) {
		return switch (format) {
			case "png" -> "image/png";
			case "webp" -> "image/webp";
			default -> "image/jpeg";
		};
	}

	private String sanitizeFilename(String filename) {
		if (filename == null) {
			return "";
		}
		return filename.replaceAll("[\\\\/\\p{Cntrl}]", "_");
	}

	private String sha256(byte[] bytes) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return Base64.getEncoder().encodeToString(digest.digest(bytes));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}
}

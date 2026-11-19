package com.attia.advanced_auth_api.shop.controller;

import com.attia.advanced_auth_api.shop.common.Envelope;
import com.attia.advanced_auth_api.shop.dto.MediaDtos;
import com.attia.advanced_auth_api.shop.service.MediaService;
import com.attia.advanced_auth_api.util.AuthenticatedUser;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** image-upload-api.md §3/§4/§13 — upload Flow A (presigned) and Flow B (one-shot). */
@RestController
@RequestMapping("/api/v1")
public class MediaController {
	private final MediaService mediaService;

	public MediaController(MediaService mediaService) {
		this.mediaService = mediaService;
	}

	@PostMapping("/admin/media/uploads")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<MediaDtos.InitiateUploadResponse> initiate(
			@RequestBody MediaDtos.InitiateUploadRequest request, Authentication authentication) {
		return Envelope.of(mediaService.initiateUpload(request, adminId(authentication)));
	}

	@PostMapping("/admin/media/uploads/{mediaId}/complete")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<MediaDtos.Response> complete(@PathVariable String mediaId, Authentication authentication) {
		return Envelope.of(mediaService.completeUpload(mediaId, adminId(authentication)));
	}

	@PostMapping(value = "/admin/media", consumes = "multipart/form-data")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public Envelope<MediaDtos.Response> oneShot(@RequestPart("file") MultipartFile file, Authentication authentication) {
		return Envelope.of(mediaService.oneShotUpload(file, adminId(authentication)));
	}

	@GetMapping("/admin/media/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<MediaDtos.Response> get(@PathVariable String id) {
		return Envelope.of(mediaService.getMedia(id));
	}

	@GetMapping("/admin/media")
	@PreAuthorize("hasRole('ADMIN')")
	public Envelope<List<MediaDtos.Response>> listUnused(@RequestParam(required = false) Boolean unused) {
		return Envelope.of(mediaService.listUnused());
	}

	@DeleteMapping("/admin/media/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String id) {
		mediaService.forceDelete(id);
	}

	private String adminId(Authentication authentication) {
		return ((AuthenticatedUser) authentication.getPrincipal()).getId();
	}
}

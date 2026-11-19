package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.common.ApiException;
import com.attia.advanced_auth_api.shop.common.ErrorCode;
import com.attia.advanced_auth_api.shop.config.CloudinaryProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Minimal Cloudinary REST client (signed upload, destroy, resource lookup, transformation URLs) —
 * plain {@code java.net.http.HttpClient}, no Cloudinary SDK dependency, so builds stay reliable
 * offline. See image-upload-api.md §3/§8/§9: this class IS the "object storage" of that spec.
 */
@Service
public class CloudinaryClient {
	private static final String API_BASE = "https://api.cloudinary.com/v1_1/";
	private static final String DELIVERY_BASE = "https://res.cloudinary.com/";

	private final CloudinaryProperties properties;
	private final HttpClient http = HttpClient.newHttpClient();
	private final ObjectMapper mapper = new ObjectMapper();

	public CloudinaryClient(CloudinaryProperties properties) {
		this.properties = properties;
	}

	public record Resource(String publicId, String secureUrl, String format, int width, int height, long bytes, String etag) {
	}

	/** §4 — one-shot: bytes already validated by the caller, upload them now. */
	public Resource upload(byte[] bytes, String publicId, String originalFilename) {
		requireConfigured();
		long timestamp = Instant.now().getEpochSecond();
		String fullPublicId = fullPublicId(publicId);
		Map<String, String> toSign = new TreeMap<>();
		toSign.put("public_id", fullPublicId);
		toSign.put("timestamp", String.valueOf(timestamp));
		String signature = sign(toSign);

		Map<String, String> fields = new TreeMap<>(toSign);
		fields.put("api_key", properties.getApiKey());
		fields.put("signature", signature);

		JsonNode json = postMultipart(API_BASE + properties.getCloudName() + "/image/upload", fields, bytes, originalFilename);
		return toResource(json);
	}

	/** §3.1/§3.3 — the params a browser needs to PUT/POST straight to Cloudinary itself. */
	public record SignedUpload(String uploadUrl, String apiKey, long timestamp, String signature, String publicId) {
	}

	public SignedUpload buildSignedUpload(String publicId) {
		requireConfigured();
		long timestamp = Instant.now().getEpochSecond();
		String fullPublicId = fullPublicId(publicId);
		Map<String, String> toSign = new TreeMap<>();
		toSign.put("public_id", fullPublicId);
		toSign.put("timestamp", String.valueOf(timestamp));
		String signature = sign(toSign);
		return new SignedUpload(
				API_BASE + properties.getCloudName() + "/image/upload", properties.getApiKey(), timestamp, signature, fullPublicId);
	}

	/**
	 * §3.3 — verify the direct upload actually landed, and fetch its metadata. {@code publicId}
	 * here is always the already-fully-qualified id (as stored on {@code Media.storageKey}), NOT
	 * the bare media id — unlike {@link #upload} / {@link #buildSignedUpload}, this must not
	 * re-apply the folder prefix.
	 */
	public Resource getResource(String publicId) {
		requireConfigured();
		String url = API_BASE + properties.getCloudName() + "/resources/image/upload/" + publicId;
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.header("Authorization", basicAuth())
				.GET()
				.build();
		HttpResponse<String> response = send(request);
		if (response.statusCode() == 404) {
			throw new ApiException(HttpStatus.CONFLICT, ErrorCode.UPLOAD_NOT_FOUND,
					"Le fichier n'a jamais été reçu par Cloudinary.");
		}
		return toResource(parse(response));
	}

	/** §13 index — force-delete. {@code publicId} is the already-fully-qualified storage key, same caveat as {@link #getResource}. */
	public void destroy(String publicId) {
		requireConfigured();
		long timestamp = Instant.now().getEpochSecond();
		Map<String, String> toSign = new TreeMap<>();
		toSign.put("invalidate", "true");
		toSign.put("public_id", publicId);
		toSign.put("timestamp", String.valueOf(timestamp));
		String signature = sign(toSign);

		Map<String, String> fields = new TreeMap<>(toSign);
		fields.put("api_key", properties.getApiKey());
		fields.put("signature", signature);
		postMultipart(API_BASE + properties.getCloudName() + "/image/destroy", fields, null, null);
	}

	/** §8 — a resized/optimized delivery URL, generated on the fly by Cloudinary, never re-uploaded. */
	public String transformationUrl(String publicId, String format, int maxWidth) {
		return DELIVERY_BASE + properties.getCloudName() + "/image/upload/w_" + maxWidth
				+ ",c_limit,q_auto,f_auto/" + publicId + "." + format;
	}

	public boolean isConfigured() {
		return properties.isConfigured();
	}

	private String fullPublicId(String publicId) {
		String folder = properties.getFolder();
		return folder == null || folder.isBlank() ? publicId : folder + "/" + publicId;
	}

	private Resource toResource(JsonNode json) {
		return new Resource(
				json.path("public_id").asText(),
				json.path("secure_url").asText(),
				json.path("format").asText("jpg"),
				json.path("width").asInt(0),
				json.path("height").asInt(0),
				json.path("bytes").asLong(0),
				json.path("etag").asText(null));
	}

	private String sign(Map<String, String> sortedParams) {
		String toSign = sortedParams.entrySet().stream()
				.map(e -> e.getKey() + "=" + e.getValue())
				.collect(Collectors.joining("&"));
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] hashed = digest.digest((toSign + properties.getApiSecret()).getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : hashed) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-1 unavailable", ex);
		}
	}

	private String basicAuth() {
		String creds = properties.getApiKey() + ":" + properties.getApiSecret();
		return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
	}

	private JsonNode postMultipart(String url, Map<String, String> fields, byte[] fileBytes, String filename) {
		String boundary = "----adelparfum" + System.nanoTime();
		byte[] body = buildMultipartBody(boundary, fields, fileBytes, filename);
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.header("Content-Type", "multipart/form-data; boundary=" + boundary)
				.POST(HttpRequest.BodyPublishers.ofByteArray(body))
				.build();
		return parse(send(request));
	}

	private byte[] buildMultipartBody(String boundary, Map<String, String> fields, byte[] fileBytes, String filename) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			for (Map.Entry<String, String> field : fields.entrySet()) {
				out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
				out.write(("Content-Disposition: form-data; name=\"" + field.getKey() + "\"\r\n\r\n")
						.getBytes(StandardCharsets.UTF_8));
				out.write((field.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
			}
			if (fileBytes != null) {
				String safeName = filename == null || filename.isBlank() ? "upload.jpg" : filename;
				out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
				out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + safeName + "\"\r\n")
						.getBytes(StandardCharsets.UTF_8));
				out.write("Content-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8));
				out.write(fileBytes);
				out.write("\r\n".getBytes(StandardCharsets.UTF_8));
			}
			out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
			return out.toByteArray();
		} catch (java.io.IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private HttpResponse<String> send(HttpRequest request) {
		try {
			return http.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception ex) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, ErrorCode.INTERNAL, "Cloudinary injoignable: " + ex.getMessage());
		}
	}

	private JsonNode parse(HttpResponse<String> response) {
		try {
			JsonNode json = mapper.readTree(response.body());
			if (response.statusCode() >= 400 && response.statusCode() != 404) {
				String message = json.path("error").path("message").asText(response.body());
				throw new ApiException(HttpStatus.BAD_GATEWAY, ErrorCode.INTERNAL, "Cloudinary: " + message);
			}
			return json;
		} catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, ErrorCode.INTERNAL, "Réponse Cloudinary illisible.");
		}
	}

	private void requireConfigured() {
		if (!isConfigured()) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL,
					"Cloudinary n'est pas configuré (CLOUDINARY_CLOUD_NAME/API_KEY/API_SECRET).");
		}
	}
}

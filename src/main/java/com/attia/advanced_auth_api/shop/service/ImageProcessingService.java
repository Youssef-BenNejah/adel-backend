package com.attia.advanced_auth_api.shop.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

/**
 * Pre-upload dimension validation only — image-upload-api.md §7 ("dimensions can only be checked
 * after the bytes exist"). Variant generation itself (§8) is Cloudinary transformation URLs now
 * (see {@link CloudinaryClient#transformationUrl}), so no local resizing happens here anymore.
 */
@Service
public class ImageProcessingService {
	public record Dimensions(int width, int height) {
	}

	public Dimensions readDimensions(byte[] bytes) {
		try {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
			if (image == null) {
				throw new IllegalArgumentException("Unreadable image");
			}
			return new Dimensions(image.getWidth(), image.getHeight());
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}

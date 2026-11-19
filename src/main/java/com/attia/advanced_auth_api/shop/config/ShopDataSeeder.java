package com.attia.advanced_auth_api.shop.config;

import com.attia.advanced_auth_api.document.Role;
import com.attia.advanced_auth_api.document.UserDocument;
import com.attia.advanced_auth_api.repository.UserRepository;
import com.attia.advanced_auth_api.shop.document.Category;
import com.attia.advanced_auth_api.shop.document.CategoryTile;
import com.attia.advanced_auth_api.shop.document.GalleryImage;
import com.attia.advanced_auth_api.shop.document.HeroSlide;
import com.attia.advanced_auth_api.shop.document.Media;
import com.attia.advanced_auth_api.shop.document.MediaStatus;
import com.attia.advanced_auth_api.shop.document.Notes;
import com.attia.advanced_auth_api.shop.document.Pack;
import com.attia.advanced_auth_api.shop.document.Product;
import com.attia.advanced_auth_api.shop.document.ProductImageLink;
import com.attia.advanced_auth_api.shop.document.ProductTag;
import com.attia.advanced_auth_api.shop.document.Promotion;
import com.attia.advanced_auth_api.shop.document.Settings;
import com.attia.advanced_auth_api.shop.document.Size;
import com.attia.advanced_auth_api.shop.repository.CategoryRepository;
import com.attia.advanced_auth_api.shop.repository.CategoryTileRepository;
import com.attia.advanced_auth_api.shop.repository.GalleryImageRepository;
import com.attia.advanced_auth_api.shop.repository.HeroSlideRepository;
import com.attia.advanced_auth_api.shop.repository.MediaRepository;
import com.attia.advanced_auth_api.shop.repository.PackRepository;
import com.attia.advanced_auth_api.shop.repository.ProductImageLinkRepository;
import com.attia.advanced_auth_api.shop.repository.ProductRepository;
import com.attia.advanced_auth_api.shop.repository.SettingsRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the data every faithful migration of the current frontend needs to boot with:
 * the single admin account (backend-api-spec.md §2.9, on top of the existing {@code UserDocument}
 * table), the three catalog categories, the singleton settings row, the one starter pack, a
 * demo product catalog with real photos, and landing-page content — so a fresh clone (new
 * database) shows real data in both the admin and the storefront right after boot.
 */
@Component
public class ShopDataSeeder implements CommandLineRunner {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ShopAdminProperties adminProperties;
	private final CategoryRepository categoryRepository;
	private final SettingsRepository settingsRepository;
	private final PackRepository packRepository;
	private final MediaRepository mediaRepository;
	private final ProductRepository productRepository;
	private final ProductImageLinkRepository productImageLinkRepository;
	private final HeroSlideRepository heroSlideRepository;
	private final GalleryImageRepository galleryImageRepository;
	private final CategoryTileRepository categoryTileRepository;

	public ShopDataSeeder(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			ShopAdminProperties adminProperties,
			CategoryRepository categoryRepository,
			SettingsRepository settingsRepository,
			PackRepository packRepository,
			MediaRepository mediaRepository,
			ProductRepository productRepository,
			ProductImageLinkRepository productImageLinkRepository,
			HeroSlideRepository heroSlideRepository,
			GalleryImageRepository galleryImageRepository,
			CategoryTileRepository categoryTileRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.adminProperties = adminProperties;
		this.categoryRepository = categoryRepository;
		this.settingsRepository = settingsRepository;
		this.packRepository = packRepository;
		this.mediaRepository = mediaRepository;
		this.productRepository = productRepository;
		this.productImageLinkRepository = productImageLinkRepository;
		this.heroSlideRepository = heroSlideRepository;
		this.galleryImageRepository = galleryImageRepository;
		this.categoryTileRepository = categoryTileRepository;
	}

	@Override
	public void run(String... args) {
		seedAdmin();
		seedCategories();
		seedSettings();
		seedPack();
		List<Media> demoMedia = seedDemoMedia();
		seedProducts(demoMedia);
		seedLanding(demoMedia);
	}

	private void seedAdmin() {
		String email = adminProperties.getEmail().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			return;
		}
		userRepository.save(UserDocument.builder()
				.email(email)
				.passwordHash(passwordEncoder.encode(adminProperties.getPassword()))
				.role(Role.ADMIN)
				.enabled(true)
				.build());
	}

	private void seedCategories() {
		if (categoryRepository.count() > 0) {
			return;
		}
		categoryRepository.saveAll(List.of(
				Category.builder().slug("homme").label("Homme").sub("Boisé · Épicé · Ambré").order(0).build(),
				Category.builder().slug("femme").label("Femme").sub("Floral · Fruité · Gourmand").order(1).build(),
				Category.builder().slug("unisexe").label("Unisexe").sub("Frais · Aromatique").order(2).build()));
	}

	private void seedSettings() {
		if (settingsRepository.count() > 0) {
			return;
		}
		settingsRepository.save(Settings.builder().build());
	}

	private void seedPack() {
		if (packRepository.count() > 0) {
			return;
		}
		packRepository.save(Pack.builder()
				.id("pack-" + System.currentTimeMillis() + "-001")
				.name("Pack Découverte")
				.price(69)
				.size(3)
				.scope(Promotion.Scope.all)
				.active(true)
				.order(0)
				.build());
	}

	/**
	 * Royalty-free Unsplash photos of generic, unlabeled perfume/cologne bottles — each one
	 * individually vetted (zoomed in) to confirm no visible brand name, logo, or wordmark, so the
	 * demo catalog never shows a competitor's trademark. Seeded as {@link Media} rows so the demo
	 * catalog and landing page have real pictures without needing a Cloudinary account.
	 */
	private static final List<String> DEMO_IMAGE_URLS = List.of(
			"https://images.unsplash.com/photo-1733660227163-01bc46e0d7d7?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1613521140785-e85e427f8002?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1733660227168-444e3c751a1e?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1594125311687-3b1b3eafa9f4?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1720423514789-15a33e59fc81?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1720366788970-1f42a5e958dd?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1598121496628-9d8b7578e290?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1720423738890-37689a6f6b95?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1680503525530-d21c05862d4d?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1760920250029-36af9369a0bb?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1622618991746-fe6004db3a47?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1608721279136-cd41b752fa41?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1676951334972-2e65e67f4cbe?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1613521076081-2820f9746a2d?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1638295916768-459f6cf440bc?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1716857591457-7d7fa45199c6?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1617661338085-d1ec6a89a6d8?w=1200&q=80&auto=format&fit=crop",
			"https://images.unsplash.com/photo-1610245169249-c7fbf6768884?w=1200&q=80&auto=format&fit=crop");

	/**
	 * Deterministic ids ({@code med-seed-00} …) so re-running on an already-seeded db is a no-op.
	 * Always re-asserts the url (rather than skip-if-exists) so swapping an entry in
	 * {@link #DEMO_IMAGE_URLS} (e.g. to drop a bad pick) takes effect on the next boot even
	 * though the row already exists.
	 */
	private List<Media> seedDemoMedia() {
		List<Media> media = new java.util.ArrayList<>();
		for (int i = 0; i < DEMO_IMAGE_URLS.size(); i++) {
			String id = String.format("med-seed-%02d", i);
			String url = DEMO_IMAGE_URLS.get(i);
			Media existing = mediaRepository.findById(id).orElse(null);
			if (existing != null) {
				existing.setUrl(url);
				media.add(mediaRepository.save(existing));
				continue;
			}
			media.add(mediaRepository.save(Media.builder()
					.id(id)
					.status(MediaStatus.ready)
					.url(url)
					.mimeType("image/jpeg")
					.width(1200)
					.height(1500)
					.storageKey("seed/unsplash-" + i)
					.createdAt(Instant.now())
					.build()));
		}
		return media;
	}

	private record DemoProduct(
			String id, String sku, String name, String category, String family, String description,
			int basePrice, Integer oldPrice, ProductTag tag, String top, String heart, String base) {
	}

	private static final List<DemoProduct> DEMO_PRODUCTS = List.of(
			new DemoProduct("homme-oud-imperial", "H001-C", "Oud Impérial", "homme", "Oriental Boisé",
					"Un oud dense et résineux, relevé de safran — sillage puissant qui tient toute la journée.",
					65, null, ProductTag.Bestseller, "Safran · Cardamome", "Rose de Damas · Oud", "Ambre gris · Santal"),
			new DemoProduct("homme-vetiver-sauvage", "H002-C", "Vétiver Sauvage", "homme", "Boisé Aromatique",
					"Vétiver franc et terreux adouci par le poivre rose — frais le matin, boisé le soir.",
					59, null, ProductTag.Bestseller, "Bergamote · Poivre rose", "Géranium · Patchouli", "Vétiver · Cèdre"),
			new DemoProduct("homme-cuir-noir", "H003-C", "Cuir Noir", "homme", "Ambré Boisé",
					"Cuir fumé et ambré, une signature sombre et élégante.",
					72, null, ProductTag.CreationAdel, "Cardamome · Bigarade", "Cuir · Iris", "Benjoin · Musc blanc"),
			new DemoProduct("homme-santal-royal", "H004-C", "Santal Royal", "homme", "Boisé Épicé",
					"Santal crémeux relevé d'épices chaudes, pour une allure raffinée.",
					68, null, ProductTag.Nouveau, "Cannelle · Mandarine", "Héliotrope · Ylang-ylang", "Bois de santal · Fève tonka"),
			new DemoProduct("homme-tabac-intense", "H005-C", "Tabac Intense", "homme", "Oriental Vanillé",
					"Tabac blond et vanille bourbon — gourmand et capiteux.",
					55, 62, ProductTag.Promo, "Poivre rose · Poire", "Fleur d'oranger · Vanille bourbon", "Fève tonka · Benjoin"),
			new DemoProduct("homme-ambre-nomade", "H006-C", "Ambre Nomade", "homme", "Ambré Boisé",
					"Ambre minéral et bois de cèdre, inspiré des caravanes du désert.",
					63, null, null, "Pamplemousse · Safran", "Patchouli · Géranium", "Ambre gris · Cèdre"),
			new DemoProduct("femme-rose-doree", "F001-C", "Rose Dorée", "femme", "Floral Fruité",
					"Rose de Damas éclatante sur un fond fruité lumineux.",
					58, null, ProductTag.Bestseller, "Framboise · Mandarine", "Rose de Damas · Pivoine", "Musc blanc · Vanille"),
			new DemoProduct("femme-vanilla-powder", "F002-C", "Vanilla Powder", "femme", "Gourmand Oriental",
					"Vanille ronde et poudreuse, addictive et chaleureuse — un best-seller client.",
					60, null, ProductTag.Bestseller, "Poire · Cardamome", "Héliotrope · Fleur d'oranger", "Vanille bourbon · Fève tonka"),
			new DemoProduct("femme-jasmin-sambac", "F003-C", "Jasmin Sambac", "femme", "Floral Musqué",
					"Jasmin sambac charnel sur un musc blanc tout en douceur.",
					64, null, ProductTag.Nouveau, "Bergamote · Mandarine", "Jasmin sambac · Ylang-ylang", "Musc blanc · Bois de santal"),
			new DemoProduct("femme-iris-sublime", "F004-C", "Iris Sublime", "femme", "Chypré Fruité",
					"Iris poudré et chypré fruité, une élégance discrète.",
					66, null, null, "Bigarade · Framboise", "Iris · Géranium", "Patchouli · Musc blanc"),
			new DemoProduct("femme-pivoine-tendre", "F005-C", "Pivoine Tendre", "femme", "Floral Fruité",
					"Pivoine fraîche et fruits rouges, un sillage tendre et lumineux.",
					52, 58, ProductTag.Promo, "Framboise · Poire", "Pivoine · Rose de Damas", "Musc blanc · Vanille"),
			new DemoProduct("femme-orchidee-secrete", "F006-C", "Orchidée Secrète", "femme", "Oriental Vanillé",
					"Orchidée exotique et vanille, une création signature Adel.",
					70, null, ProductTag.CreationAdel, "Cardamome · Bergamote", "Orchidée · Ylang-ylang", "Vanille bourbon · Ambre gris"),
			new DemoProduct("unisexe-black-bomb", "U001-C", "Black Bomb", "unisexe", "Oriental Boisé",
					"Oriental boisé intense et addictif — superbe en toute saison.",
					58, null, ProductTag.Bestseller, "Safran · Poivre rose", "Fève tonka · Patchouli", "Oud · Ambre gris"),
			new DemoProduct("unisexe-palo-santo-zen", "U002-C", "Palo Santo Zen", "unisexe", "Boisé Aromatique",
					"Palo santo fumé et résine, apaisant et minéral.",
					56, null, null, "Cardamome · Pamplemousse", "Encens · Géranium", "Palo santo · Cèdre"),
			new DemoProduct("unisexe-feve-tonka-infini", "U003-C", "Fève Tonka Infini", "unisexe", "Gourmand Oriental",
					"Fève tonka gourmande et musc blanc, un sillage enveloppant.",
					61, null, ProductTag.Nouveau, "Poire · Cannelle", "Héliotrope · Fève tonka", "Musc blanc · Benjoin"));

	/** Per-id existence check (not a blanket collection count) so this still seeds the demo
	 * catalog alongside whatever unrelated products a dev already created by hand. */
	private void seedProducts(List<Media> media) {
		if (media.isEmpty()) {
			return;
		}
		int imageIndex = 0;
		for (DemoProduct demo : DEMO_PRODUCTS) {
			if (productRepository.existsById(demo.id())) {
				imageIndex++;
				continue;
			}
			Product product = Product.builder()
					.id(demo.id())
					.sku(demo.sku())
					.name(demo.name())
					.category(demo.category())
					.family(demo.family())
					.description(demo.description())
					.sizes(List.of(
							Size.builder().ml(30).price(demo.basePrice()).build(),
							Size.builder().ml(50).price(demo.basePrice() + 10).build(),
							Size.builder().ml(100).price(demo.basePrice() + 24).build()))
					.oldPrice(demo.oldPrice())
					.tag(demo.tag())
					.rating("5.0")
					.reviews(12 + (imageIndex * 7) % 180)
					.notes(Notes.builder().top(demo.top()).heart(demo.heart()).base(demo.base()).build())
					.build();
			productRepository.save(product);

			Media first = media.get(imageIndex % media.size());
			Media second = media.get((imageIndex + 1) % media.size());
			productImageLinkRepository.save(ProductImageLink.builder()
					.id(demo.id() + "-img-0")
					.productId(demo.id())
					.mediaId(first.getId())
					.position(0)
					.build());
			productImageLinkRepository.save(ProductImageLink.builder()
					.id(demo.id() + "-img-1")
					.productId(demo.id())
					.mediaId(second.getId())
					.position(1)
					.build());
			imageIndex++;
		}
	}

	private void seedLanding(List<Media> media) {
		if (media.isEmpty()) {
			return;
		}
		seedHeroSlides(media);
		seedGallery(media);
		seedCategoryTiles(media);
	}

	private void seedHeroSlides(List<Media> media) {
		if (heroSlideRepository.count() > 0) {
			return;
		}
		List<HeroSlide> slides = List.of(
				HeroSlide.builder().id("slide-seed-0").mediaId(media.get(0).getId())
						.eyebrow("Collection Signature").title("L'élégance d'un parfum de créateur, sans le prix")
						.active(true).order(0).build(),
				HeroSlide.builder().id("slide-seed-1").mediaId(media.get(2).getId())
						.eyebrow("Nouveauté").title("Découvrez nos dernières créations").active(true).order(1).build(),
				HeroSlide.builder().id("slide-seed-2").mediaId(media.get(4).getId())
						.eyebrow("Pack Découverte").title("3 parfums, un seul prix").active(true).order(2).build());
		heroSlideRepository.saveAll(slides);
	}

	private void seedGallery(List<Media> media) {
		if (galleryImageRepository.count() > 0) {
			return;
		}
		List<GalleryImage> images = new java.util.ArrayList<>();
		int[] picks = {1, 3, 5, 7, 9, 11};
		for (int i = 0; i < picks.length; i++) {
			images.add(GalleryImage.builder()
					.id("gallery-seed-" + i)
					.mediaId(media.get(picks[i] % media.size()).getId())
					.order(i)
					.build());
		}
		galleryImageRepository.saveAll(images);
	}

	/** Fills in only the slugs still missing a picture (e.g. a placeholder row left with
	 * {@code mediaId == null}), rather than skipping outright because the collection isn't empty. */
	private void seedCategoryTiles(List<Media> media) {
		fillCategoryTile("homme", media.get(6).getId());
		fillCategoryTile("femme", media.get(8).getId());
		fillCategoryTile("unisexe", media.get(10).getId());
	}

	private void fillCategoryTile(String slug, String mediaId) {
		CategoryTile tile = categoryTileRepository.findById(slug).orElse(null);
		if (tile != null && tile.getMediaId() != null) {
			return;
		}
		categoryTileRepository.save(CategoryTile.builder().categorySlug(slug).mediaId(mediaId).build());
	}
}

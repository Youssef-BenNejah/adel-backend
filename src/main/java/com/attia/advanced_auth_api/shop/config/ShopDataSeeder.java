package com.attia.advanced_auth_api.shop.config;

import com.attia.advanced_auth_api.document.Role;
import com.attia.advanced_auth_api.document.UserDocument;
import com.attia.advanced_auth_api.repository.UserRepository;
import com.attia.advanced_auth_api.shop.document.Category;
import com.attia.advanced_auth_api.shop.document.Pack;
import com.attia.advanced_auth_api.shop.document.Promotion;
import com.attia.advanced_auth_api.shop.document.Settings;
import com.attia.advanced_auth_api.shop.repository.CategoryRepository;
import com.attia.advanced_auth_api.shop.repository.PackRepository;
import com.attia.advanced_auth_api.shop.repository.SettingsRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the data every faithful migration of the current frontend needs to boot with:
 * the single admin account (backend-api-spec.md §2.9, on top of the existing {@code UserDocument}
 * table), the three catalog categories, the singleton settings row, and the one starter pack.
 */
@Component
public class ShopDataSeeder implements CommandLineRunner {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ShopAdminProperties adminProperties;
	private final CategoryRepository categoryRepository;
	private final SettingsRepository settingsRepository;
	private final PackRepository packRepository;

	public ShopDataSeeder(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			ShopAdminProperties adminProperties,
			CategoryRepository categoryRepository,
			SettingsRepository settingsRepository,
			PackRepository packRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.adminProperties = adminProperties;
		this.categoryRepository = categoryRepository;
		this.settingsRepository = settingsRepository;
		this.packRepository = packRepository;
	}

	@Override
	public void run(String... args) {
		seedAdmin();
		seedCategories();
		seedSettings();
		seedPack();
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
}

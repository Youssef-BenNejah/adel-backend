package com.attia.advanced_auth_api.shop.service;

import com.attia.advanced_auth_api.shop.document.Order;
import com.attia.advanced_auth_api.shop.document.OrderItem;
import com.attia.advanced_auth_api.shop.document.OrderStatus;
import com.attia.advanced_auth_api.shop.document.Product;
import com.attia.advanced_auth_api.shop.document.Promotion;
import com.attia.advanced_auth_api.shop.dto.DashboardDtos;
import com.attia.advanced_auth_api.shop.dto.ProductDtos;
import com.attia.advanced_auth_api.shop.repository.CategoryRepository;
import com.attia.advanced_auth_api.shop.repository.CustomerRepository;
import com.attia.advanced_auth_api.shop.repository.OrderRepository;
import com.attia.advanced_auth_api.shop.repository.ProductRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/** backend-api-spec.md §3.7 / §3.10 / §15. */
@Service
public class DashboardService {
	private static final int RAIL_SIZE = 10;
	private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final CustomerRepository customerRepository;
	private final CategoryRepository categoryRepository;
	private final PromotionService promotionService;
	private final PricingService pricingService;
	private final ProductService productService;

	public DashboardService(
			OrderRepository orderRepository,
			ProductRepository productRepository,
			CustomerRepository customerRepository,
			CategoryRepository categoryRepository,
			PromotionService promotionService,
			PricingService pricingService,
			ProductService productService) {
		this.orderRepository = orderRepository;
		this.productRepository = productRepository;
		this.customerRepository = customerRepository;
		this.categoryRepository = categoryRepository;
		this.promotionService = promotionService;
		this.pricingService = pricingService;
		this.productService = productService;
	}

	public DashboardDtos.Response dashboard() {
		List<Order> orders = orderRepository.findAll();
		int revenue = orders.stream().mapToInt(Order::getTotal).sum();
		long pending = orders.stream().filter(o -> o.getStatus() == OrderStatus.EN_ATTENTE).count();
		int avgOrder = orders.isEmpty() ? 0 : (int) Math.round(revenue / (double) orders.size());

		List<DashboardDtos.DayTotal> last7Days = last7Days(orders);
		List<DashboardDtos.StatusCount> statusCounts = OrderStatus.ORDERED.stream()
				.map(status -> new DashboardDtos.StatusCount(
						status.getLabel(), orders.stream().filter(o -> o.getStatus() == status).count()))
				.toList();
		List<DashboardDtos.TopProduct> topProducts = topProducts(orders);
		List<Order> recentOrders = orders.stream()
				.sorted(Comparator.comparing(Order::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(6)
				.toList();

		return DashboardDtos.Response.builder()
				.revenue(revenue)
				.ordersCount(orders.size())
				.productsCount(productRepository.count())
				.customersCount(customerRepository.count())
				.avgOrder(avgOrder)
				.activePromosCount(promotionService.countActive())
				.categoriesCount(categoryRepository.count())
				.pendingOrdersCount(pending)
				.last7Days(last7Days)
				.statusCounts(statusCounts)
				.topProducts(topProducts)
				.recentOrders(recentOrders)
				.build();
	}

	private List<DashboardDtos.DayTotal> last7Days(List<Order> orders) {
		ZoneId zone = ZoneId.systemDefault();
		List<DashboardDtos.DayTotal> days = new ArrayList<>();
		LocalDate today = LocalDate.now(zone);
		for (int i = 6; i >= 0; i--) {
			LocalDate day = today.minusDays(i);
			int total = orders.stream()
					.filter(o -> o.getDate() != null && o.getDate().atZone(zone).toLocalDate().equals(day))
					.mapToInt(Order::getTotal)
					.sum();
			days.add(new DashboardDtos.DayTotal(day.format(DAY_FORMAT), total));
		}
		return days;
	}

	private List<DashboardDtos.TopProduct> topProducts(List<Order> orders) {
		record Agg(String id, String name, int qty, int revenue) {
		}
		Map<String, int[]> qtyRevenueById = new LinkedHashMap<>();
		Map<String, String> nameById = new LinkedHashMap<>();
		for (Order order : orders) {
			for (OrderItem item : order.getItems()) {
				qtyRevenueById.computeIfAbsent(item.getId(), k -> new int[2]);
				int[] agg = qtyRevenueById.get(item.getId());
				agg[0] += item.getQty();
				agg[1] += item.getQty() * item.getPrice();
				nameById.putIfAbsent(item.getId(), item.getName());
			}
		}
		return qtyRevenueById.entrySet().stream()
				.map(e -> new DashboardDtos.TopProduct(e.getKey(), nameById.get(e.getKey()), e.getValue()[0], e.getValue()[1]))
				.sorted(Comparator.comparingInt(DashboardDtos.TopProduct::qty).reversed())
				.limit(5)
				.toList();
	}

	/** §15.3 / §3.7 — two independent, non-overlapping, catalog-ordered rails, capped at 10 each. */
	public DashboardDtos.HomeRails homeRails() {
		List<Product> products = productRepository.findAll();
		List<Promotion> activePromotions = promotionService.listActive();

		List<Product> promoRail = products.stream()
				.filter(p -> pricingService.isPromo(p, activePromotions))
				.limit(RAIL_SIZE)
				.toList();
		Set<String> promoIds = new LinkedHashSet<>();
		promoRail.forEach(p -> promoIds.add(p.getId()));

		List<Product> bestsellersFirst = new ArrayList<>();
		products.stream()
				.filter(p -> pricingService.isBestseller(p) && !promoIds.contains(p.getId()))
				.forEach(bestsellersFirst::add);
		bestsellersFirst.addAll(products);

		Set<String> seen = new LinkedHashSet<>(promoIds);
		List<Product> bestRail = new ArrayList<>();
		for (Product p : bestsellersFirst) {
			if (bestRail.size() >= RAIL_SIZE) {
				break;
			}
			if (seen.add(p.getId())) {
				bestRail.add(p);
			}
		}

		List<ProductDtos.Response> promoResponses = promoRail.stream()
				.map(p -> productService.enrich(p, activePromotions)).toList();
		List<ProductDtos.Response> bestResponses = bestRail.stream()
				.map(p -> productService.enrich(p, activePromotions)).toList();
		return DashboardDtos.HomeRails.builder().promo(promoResponses).bestsellers(bestResponses).build();
	}
}

package com.attia.advanced_auth_api.shop.dto;

import com.attia.advanced_auth_api.shop.document.Order;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** backend-api-spec.md §15.1 / §3.10. */
public final class DashboardDtos {
	private DashboardDtos() {
	}

	public record DayTotal(String date, int total) {
	}

	public record StatusCount(String status, long count) {
	}

	public record TopProduct(String id, String name, int qty, int revenue) {
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class Response {
		private int revenue;
		private long ordersCount;
		private long productsCount;
		private long customersCount;
		private int avgOrder;
		private long activePromosCount;
		private long categoriesCount;
		private long pendingOrdersCount;
		private List<DayTotal> last7Days;
		private List<StatusCount> statusCounts;
		private List<TopProduct> topProducts;
		private List<Order> recentOrders;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	public static class HomeRails {
		private List<ProductDtos.Response> promo;
		private List<ProductDtos.Response> bestsellers;
	}
}

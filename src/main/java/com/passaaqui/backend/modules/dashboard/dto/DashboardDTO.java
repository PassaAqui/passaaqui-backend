package com.passaaqui.backend.modules.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.passaaqui.backend.modules.order.dto.ShopkeeperOrderDTO;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDTO(
    @JsonProperty("orders_today") long ordersToday,
    @JsonProperty("revenue_today") BigDecimal revenueToday,
    @JsonProperty("active_products") long activeProducts,
    @JsonProperty("pending_orders") long pendingOrders,
    @JsonProperty("weekly_sales") List<WeeklySalesDTO> weeklySales,
    @JsonProperty("recent_orders") List<ShopkeeperOrderDTO> recentOrders
) {}
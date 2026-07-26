package com.passaaqui.backend.modules.dashboard.service;

import com.passaaqui.backend.modules.dashboard.dto.DashboardDTO;
import com.passaaqui.backend.modules.dashboard.dto.WeeklySalesDTO;
import com.passaaqui.backend.modules.order.dto.ShopkeeperOrderDTO;
import com.passaaqui.backend.modules.order.service.OrderService;
import com.passaaqui.backend.modules.product.repository.ProductRepository;
import com.passaaqui.backend.modules.shopkeeper.repository.ShopkeeperRepository;
import com.passaaqui.backend.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final ShopkeeperRepository shopkeeperRepository;

    public DashboardDTO getDashboard(Integer shopkeeperId) {
        shopkeeperRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper not found"));

        long ordersToday = orderService.countOrdersToday(shopkeeperId);
        BigDecimal revenueToday = orderService.revenueToday(shopkeeperId);
        long activeProducts = productRepository.countByShopkeeperIdAndActiveTrue(shopkeeperId);
        long pendingOrders = orderService.countPendingOrders(shopkeeperId);

        List<BigDecimal> weeklyTotals = orderService.weeklySales(shopkeeperId);
        List<WeeklySalesDTO> weeklySales = new ArrayList<>();
        String[] dayNames = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"};
        for (int i = 0; i < weeklyTotals.size(); i++) {
            weeklySales.add(new WeeklySalesDTO(dayNames[i], weeklyTotals.get(i)));
        }

        List<ShopkeeperOrderDTO> recentOrders = orderService.getRecentOrders(shopkeeperId, 5);

        return new DashboardDTO(
            ordersToday, revenueToday, activeProducts, pendingOrders,
            weeklySales, recentOrders
        );
    }
}
package com.lezh1n.goodminton_shop_api.services.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.response.DailyRevenueResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.DashboardSummaryResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreRevenueResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.Store;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.OrderRepository;
import com.lezh1n.goodminton_shop_api.repositories.StoreRepository;
import com.lezh1n.goodminton_shop_api.security.CurrentAccountProvider;
import com.lezh1n.goodminton_shop_api.services.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int DEFAULT_DAYS = 30;

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final CurrentAccountProvider currentAccountProvider;

    @Override
    public DashboardSummaryResponse getSystemSummary(LocalDate from, LocalDate to) {
        DateRange r = resolveRange(from, to);
        OrderStatus status = OrderStatus.COMPLETED;

        BigDecimal revenue = orderRepository.sumRevenue(status, r.start, r.end);
        long completed = orderRepository.countByStatusAndOrderDateBetween(status, r.start, r.end);
        long online = orderRepository.countByStatusAndOrderTypeAndOrderDateBetween(
                status, OrderType.ONLINE, r.start, r.end);
        long inStore = orderRepository.countByStatusAndOrderTypeAndOrderDateBetween(
                status, OrderType.IN_STORE, r.start, r.end);

        return summary(r, revenue, completed, online, inStore);
    }

    @Override
    public DashboardSummaryResponse getMyStoreSummary(LocalDate from, LocalDate to) {
        Store store = currentStore();
        DateRange r = resolveRange(from, to);
        OrderStatus status = OrderStatus.COMPLETED;

        BigDecimal revenue = orderRepository.sumRevenueByStore(store.getId(), status, r.start, r.end);
        long completed = orderRepository.countByStore_IdAndStatusAndOrderDateBetween(
                store.getId(), status, r.start, r.end);
        long online = orderRepository.countByStore_IdAndStatusAndOrderTypeAndOrderDateBetween(
                store.getId(), status, OrderType.ONLINE, r.start, r.end);
        long inStore = orderRepository.countByStore_IdAndStatusAndOrderTypeAndOrderDateBetween(
                store.getId(), status, OrderType.IN_STORE, r.start, r.end);

        return summary(r, revenue, completed, online, inStore);
    }

    @Override
    public List<DailyRevenueResponse> getRevenueByDate(LocalDate from, LocalDate to, Integer storeId) {
        DateRange r = resolveRange(from, to);
        List<Object[]> rows = orderRepository.findDailyRevenueRaw(
                OrderStatus.COMPLETED.name(), r.start, r.end, storeId);

        return rows.stream().map(row -> DailyRevenueResponse.builder()
                .date(toLocalDate(row[0]))
                .revenue((BigDecimal) row[1])
                .orderCount(((Number) row[2]).longValue())
                .build())
                .toList();
    }

    @Override
    public List<DailyRevenueResponse> getMyStoreRevenueByDate(LocalDate from, LocalDate to) {
        Store store = currentStore();
        return getRevenueByDate(from, to, store.getId());
    }

    @Override
    public List<StoreRevenueResponse> getRevenueByStore(LocalDate from, LocalDate to) {
        DateRange r = resolveRange(from, to);
        List<Object[]> rows = orderRepository.findRevenueByStoreRaw(OrderStatus.COMPLETED, r.start, r.end);

        return rows.stream().map(row -> StoreRevenueResponse.builder()
                .storeId((Integer) row[0])
                .storeName((String) row[1])
                .revenue((BigDecimal) row[2])
                .orderCount(((Number) row[3]).longValue())
                .build())
                .toList();
    }

    // ---------- Helpers ----------

    private DashboardSummaryResponse summary(DateRange r, BigDecimal revenue, long completed,
            long online, long inStore) {
        return DashboardSummaryResponse.builder()
                .fromDate(r.start.toLocalDate())
                .toDate(r.end.toLocalDate())
                .totalRevenue(revenue == null ? BigDecimal.ZERO : revenue)
                .completedOrders(completed)
                .onlineOrders(online)
                .inStoreOrders(inStore)
                .build();
    }

    private DateRange resolveRange(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveTo = to != null ? to : today;
        LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(DEFAULT_DAYS);
        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new AppException(ErrorCode.SYSTEM_INTERNAL_ERROR);
        }
        return new DateRange(effectiveFrom.atStartOfDay(), effectiveTo.atTime(LocalTime.MAX));
    }

    private Store currentStore() {
        Account admin = currentAccountProvider.getCurrentAccount();
        return storeRepository.findByAdmin_Id(admin.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
    }

    // Native query returns java.sql.Date; JPQL CAST may return LocalDate. Handle both.
    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) {
            return ld;
        }
        if (value instanceof Date d) {
            return d.toLocalDate();
        }
        throw new IllegalStateException("Unsupported date type: " + value.getClass());
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }
}

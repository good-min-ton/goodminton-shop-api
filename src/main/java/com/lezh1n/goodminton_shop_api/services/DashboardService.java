package com.lezh1n.goodminton_shop_api.services;

import java.time.LocalDate;
import java.util.List;

import com.lezh1n.goodminton_shop_api.dtos.response.DailyRevenueResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.DashboardSummaryResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreRevenueResponse;

public interface DashboardService {

    DashboardSummaryResponse getSystemSummary(LocalDate from, LocalDate to);

    DashboardSummaryResponse getMyStoreSummary(LocalDate from, LocalDate to);

    /** storeId == null → all stores (super admin only). */
    List<DailyRevenueResponse> getRevenueByDate(LocalDate from, LocalDate to, Integer storeId);

    /** Resolves current store admin's store internally. */
    List<DailyRevenueResponse> getMyStoreRevenueByDate(LocalDate from, LocalDate to);

    List<StoreRevenueResponse> getRevenueByStore(LocalDate from, LocalDate to);
}

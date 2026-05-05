package com.lezh1n.goodminton_shop_api.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.DailyRevenueResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.DashboardSummaryResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreRevenueResponse;
import com.lezh1n.goodminton_shop_api.services.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // ---------- Super admin ----------

    @GetMapping("/summary")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<DashboardSummaryResponse> getSystemSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.<DashboardSummaryResponse>builder()
                .result(dashboardService.getSystemSummary(from, to))
                .build();
    }

    @GetMapping("/revenue-by-date")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<DailyRevenueResponse>> getRevenueByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer storeId) {
        return ApiResponse.<List<DailyRevenueResponse>>builder()
                .result(dashboardService.getRevenueByDate(from, to, storeId))
                .build();
    }

    @GetMapping("/revenue-by-store")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<StoreRevenueResponse>> getRevenueByStore(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.<List<StoreRevenueResponse>>builder()
                .result(dashboardService.getRevenueByStore(from, to))
                .build();
    }

    // ---------- Store admin ----------

    @GetMapping("/my-store/summary")
    @PreAuthorize("hasRole('STORE_ADMIN')")
    public ApiResponse<DashboardSummaryResponse> getMyStoreSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.<DashboardSummaryResponse>builder()
                .result(dashboardService.getMyStoreSummary(from, to))
                .build();
    }

    @GetMapping("/my-store/revenue-by-date")
    @PreAuthorize("hasRole('STORE_ADMIN')")
    public ApiResponse<List<DailyRevenueResponse>> getMyStoreRevenueByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.<List<DailyRevenueResponse>>builder()
                .result(dashboardService.getMyStoreRevenueByDate(from, to))
                .build();
    }
}

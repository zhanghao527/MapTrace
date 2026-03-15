package com.maptrace.controller;

import com.maptrace.common.Result;
import com.maptrace.model.vo.DashboardStatsVO;
import com.maptrace.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.success(dashboardService.getStats());
    }
}

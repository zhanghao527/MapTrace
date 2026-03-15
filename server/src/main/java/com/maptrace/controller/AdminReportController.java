package com.maptrace.controller;

import com.maptrace.common.Result;
import com.maptrace.model.dto.*;
import com.maptrace.model.vo.*;
import com.maptrace.service.AdminLogService;
import com.maptrace.service.AppealService;
import com.maptrace.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;
    private final AppealService appealService;
    private final AdminLogService adminLogService;

    @GetMapping("/list")
    public Result<AdminReportPageVO> list(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "targetType", required = false) String targetType,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(reportService.getAdminReports(userId, status, targetType, page, size));
    }

    @GetMapping("/detail/{id}")
    public Result<AdminReportDetailVO> detail(
            @RequestAttribute("userId") Long userId,
            @PathVariable("id") Long id) {
        return Result.success(reportService.getAdminReportDetail(userId, id));
    }

    @PostMapping("/resolve")
    public Result<Void> resolve(
            @RequestAttribute("userId") Long userId,
            @RequestBody ResolveReportRequest request) {
        reportService.resolveReport(userId, request);
        return Result.success();
    }

    @PostMapping("/reject")
    public Result<Void> reject(
            @RequestAttribute("userId") Long userId,
            @RequestBody RejectReportRequest request) {
        reportService.rejectReport(userId, request);
        return Result.success();
    }

    @PostMapping("/batch-resolve")
    public Result<Void> batchResolve(
            @RequestAttribute("userId") Long userId,
            @RequestBody BatchReportActionRequest request) {
        reportService.batchResolve(userId, request);
        return Result.success();
    }

    @PostMapping("/batch-reject")
    public Result<Void> batchReject(
            @RequestAttribute("userId") Long userId,
            @RequestBody BatchReportActionRequest request) {
        reportService.batchReject(userId, request);
        return Result.success();
    }

    @GetMapping("/pending-count")
    public Result<PendingReportCountVO> pendingCount(
            @RequestAttribute("userId") Long userId) {
        PendingReportCountVO r = reportService.getPendingCount(userId);
        r.setAppealCount(appealService.getPendingCount());
        return Result.success(r);
    }

    @GetMapping("/aggregated")
    public Result<List<AggregatedReportVO>> aggregated(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(reportService.getAggregatedReports(userId, page, size));
    }

    @PostMapping("/punish")
    public Result<Void> punishUser(
            @RequestAttribute("userId") Long userId,
            @RequestBody PunishUserRequest request) {
        reportService.punishUser(userId, request);
        return Result.success();
    }

    @GetMapping("/user-violations")
    public Result<UserViolationPageVO> userViolations(
            @RequestAttribute("userId") Long userId,
            @RequestParam("targetUserId") Long targetUserId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(reportService.getUserViolations(userId, targetUserId, page, size));
    }

    @GetMapping("/appeals")
    public Result<AppealPageVO> appeals(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(appealService.getAdminAppeals(userId, status, page, size));
    }

    @GetMapping("/appeal/{id}")
    public Result<AppealVO> appealDetail(
            @RequestAttribute("userId") Long userId,
            @PathVariable("id") Long id) {
        return Result.success(appealService.getAppealDetail(userId, id));
    }

    @PostMapping("/appeal/resolve")
    public Result<Void> resolveAppeal(
            @RequestAttribute("userId") Long userId,
            @RequestBody HandleAppealRequest request) {
        appealService.resolveAppeal(userId, request);
        return Result.success();
    }

    @PostMapping("/appeal/reject")
    public Result<Void> rejectAppeal(
            @RequestAttribute("userId") Long userId,
            @RequestBody HandleAppealRequest request) {
        appealService.rejectAppeal(userId, request);
        return Result.success();
    }

    @GetMapping("/logs")
    public Result<AdminLogPageVO> logs(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return Result.success(adminLogService.getLogs(userId, page, size));
    }
}

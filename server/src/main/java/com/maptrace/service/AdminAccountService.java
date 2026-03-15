package com.maptrace.service;

import com.maptrace.model.dto.*;
import com.maptrace.model.vo.*;
import java.util.List;

public interface AdminAccountService {
    AdminLoginVO login(AdminLoginRequest request, String ip, String userAgent);
    AdminAccountVO getInfo(Long adminId);
    void changePassword(Long adminId, AdminChangePasswordRequest request, String ip, String userAgent);
    List<AdminAccountVO> listAccounts(Long adminId);
    AdminAccountVO createAccount(Long adminId, CreateAdminAccountRequest request);
    void updateAccount(Long adminId, Long targetId, CreateAdminAccountRequest request);
    void resetPassword(Long adminId, Long targetId);
    void toggleAccount(Long adminId, Long targetId);
}

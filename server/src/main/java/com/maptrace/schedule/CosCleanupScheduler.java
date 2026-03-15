package com.maptrace.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.maptrace.mapper.CosDeleteRecordMapper;
import com.maptrace.model.entity.CosDeleteRecord;
import com.maptrace.service.CosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * COS 文件延迟删除定时任务
 * 每天凌晨 3 点执行，清理超过 30 天的已标记删除文件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CosCleanupScheduler {

    private final CosDeleteRecordMapper cosDeleteRecordMapper;
    private final CosService cosService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredFiles() {
        log.info("开始执行 COS 文件清理任务");
        
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CosDeleteRecord> wrapper = new LambdaQueryWrapper<CosDeleteRecord>()
                .eq(CosDeleteRecord::getIsDeleted, 0)
                .le(CosDeleteRecord::getScheduledDeleteTime, now);
        
        List<CosDeleteRecord> records = cosDeleteRecordMapper.selectList(wrapper);
        log.info("找到 {} 个待删除的 COS 文件", records.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (CosDeleteRecord record : records) {
            try {
                cosService.deleteByUrl(record.getFileUrl());
                record.setIsDeleted(1);
                cosDeleteRecordMapper.updateById(record);
                successCount++;
            } catch (Exception e) {
                log.error("删除 COS 文件失败: {}", record.getFileUrl(), e);
                failCount++;
            }
        }
        
        log.info("COS 文件清理任务完成，成功: {}, 失败: {}", successCount, failCount);
    }
}

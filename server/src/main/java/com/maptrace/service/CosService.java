package com.maptrace.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.maptrace.config.CosConfig;
import com.maptrace.monitor.BusinessMetricsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.maptrace.common.BusinessException;
import com.maptrace.common.ErrorCode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CosService {

    private final COSClient cosClient;
    private final CosConfig cosConfig;
    private final BusinessMetricsCollector metricsCollector;
    private final com.maptrace.mapper.CosDeleteRecordMapper cosDeleteRecordMapper;

    /**
     * 标记文件为待删除（30天后物理删除）
     * 用于支持申诉后的内容恢复
     */
    public void scheduleDelete(String fileUrl, String contentType, Long contentId, String deleteReason) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        com.maptrace.model.entity.CosDeleteRecord record = new com.maptrace.model.entity.CosDeleteRecord();
        record.setFileUrl(fileUrl);
        record.setContentType(contentType);
        record.setContentId(contentId);
        record.setDeleteReason(deleteReason);
        record.setScheduledDeleteTime(java.time.LocalDateTime.now().plusDays(30));
        record.setIsDeleted(0);

        cosDeleteRecordMapper.insert(record);
        log.info("COS 文件已标记为延迟删除: {}, 计划删除时间: {}", fileUrl, record.getScheduledDeleteTime());
    }

    /**
     * 立即物理删除文件（仅用于定时任务清理过期文件）
     */
    public void deleteByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        try {
            String host = cosConfig.getBucket() + ".cos." + cosConfig.getRegion() + ".myqcloud.com/";
            int idx = fileUrl.indexOf(host);
            if (idx < 0) {
                log.warn("COS URL 格式不匹配，跳过删除: {}", fileUrl);
                return;
            }
            String key = fileUrl.substring(idx + host.length());
            cosClient.deleteObject(cosConfig.getBucket(), key);
            log.info("COS 删除成功: {}", key);
            metricsCollector.recordCosOperation("delete", "success");
        } catch (Exception e) {
            log.error("COS 删除失败: {}", fileUrl, e);
            metricsCollector.recordCosOperation("delete", "error");
        }
    }

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".heic", ".heif");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/heic", "image/heif");
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    /**
     * 上传文件到 COS，返回访问 URL
     */
    public String upload(MultipartFile file) {
        // 文件校验
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PHOTO_SIZE_EXCEEDED, "文件大小不能超过20MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件类型，仅允许 JPG/PNG/GIF/WebP/HEIC");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件扩展名，仅允许 jpg/png/gif/webp/heic");
        }

        java.time.Instant start = java.time.Instant.now();
        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String key = "photos/" + datePath + "/" + UUID.randomUUID() + ext;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucket(), key, file.getInputStream(), metadata);
            cosClient.putObject(putRequest);

            String url = "https://" + cosConfig.getBucket() + ".cos." + cosConfig.getRegion() + ".myqcloud.com/" + key;
            log.info("COS 上传成功: {}", url);

            metricsCollector.recordCosOperation("upload", "success");
            metricsCollector.recordCosOperationDuration("upload",
                    java.time.Duration.between(start, java.time.Instant.now()));

            return url;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("COS 上传失败", e);
            metricsCollector.recordCosOperation("upload", "error");
            throw new BusinessException(ErrorCode.PHOTO_UPLOAD_FAILED, "图片上传失败，请稍后重试");
        }
    }
}


package com.timemap.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 照片上传请求 DTO
 * 添加输入校验注解
 */
@Data
public class PhotoUploadRequest {

    @NotNull(message = "图片文件不能为空")
    private MultipartFile file;

    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度必须在 -180 到 180 之间")
    @DecimalMax(value = "180.0", message = "经度必须在 -180 到 180 之间")
    private Double longitude;

    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度必须在 -90 到 90 之间")
    @DecimalMax(value = "90.0", message = "纬度必须在 -90 到 90 之间")
    private Double latitude;

    @NotBlank(message = "拍摄日期不能为空")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日期格式必须为 yyyy-MM-dd")
    private String photoDate;

    @Size(max = 200, message = "地点名称不能超过200个字符")
    private String locationName;

    @Size(max = 100, message = "行政区划不能超过100个字符")
    private String district;

    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
}

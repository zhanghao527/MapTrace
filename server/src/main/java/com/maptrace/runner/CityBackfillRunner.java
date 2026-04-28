package com.maptrace.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.maptrace.model.entity.Photo;
import com.maptrace.mapper.PhotoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 启动时自动回填 city 字段。
 * 找出所有 district 非空但 city 为空的照片，按经纬度调腾讯地图逆地理编码获取城市名，批量更新。
 * 只在有需要回填的数据时才执行，跑完一次后不会再触发。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CityBackfillRunner implements ApplicationRunner {

    private final PhotoMapper photoMapper;

    @Value("${tencent-map.key}")
    private String mapKey;

    @Override
    public void run(ApplicationArguments args) {
        // 查出所有需要回填的照片（district 非空，city 为空）
        List<Photo> photos = photoMapper.selectList(
                new LambdaQueryWrapper<Photo>()
                        .isNotNull(Photo::getDistrict)
                        .ne(Photo::getDistrict, "")
                        .and(w -> w.isNull(Photo::getCity).or().eq(Photo::getCity, ""))
                        .eq(Photo::getDeleted, 0)
        );

        if (photos.isEmpty()) {
            log.info("[CityBackfill] 无需回填，所有照片已有 city 字段");
            return;
        }

        log.info("[CityBackfill] 发现 {} 张照片需要回填 city", photos.size());

        // 按 district 分组，每组取一张照片的经纬度去查城市（同一个区县的城市一定相同）
        Map<String, Photo> districtSamples = photos.stream()
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                .collect(Collectors.toMap(
                        Photo::getDistrict,
                        p -> p,
                        (a, b) -> a  // 同 district 取第一个
                ));

        RestTemplate restTemplate = new RestTemplate();
        int updated = 0;

        for (Map.Entry<String, Photo> entry : districtSamples.entrySet()) {
            String district = entry.getKey();
            Photo sample = entry.getValue();

            try {
                String city = fetchCity(restTemplate, sample.getLatitude(), sample.getLongitude());
                if (city != null && !city.isEmpty()) {
                    // 批量更新该 district 下所有照片的 city
                    int rows = photoMapper.update(null,
                            new LambdaUpdateWrapper<Photo>()
                                    .eq(Photo::getDistrict, district)
                                    .and(w -> w.isNull(Photo::getCity).or().eq(Photo::getCity, ""))
                                    .set(Photo::getCity, city)
                    );
                    updated += rows;
                    log.info("[CityBackfill] {} → {} (更新 {} 张)", district, city, rows);
                } else {
                    log.warn("[CityBackfill] {} 未能获取城市名", district);
                }
                // 避免触发 API 频率限制
                Thread.sleep(200);
            } catch (Exception e) {
                log.error("[CityBackfill] 回填 {} 失败: {}", district, e.getMessage());
            }
        }

        log.info("[CityBackfill] 回填完成，共更新 {} 张照片", updated);
    }

    @SuppressWarnings("unchecked")
    private String fetchCity(RestTemplate restTemplate, double lat, double lng) {
        String url = String.format(
                "https://apis.map.qq.com/ws/geocoder/v1/?location=%s,%s&key=%s",
                lat, lng, mapKey
        );
        try {
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
            if (resp == null) return null;
            int status = (int) resp.getOrDefault("status", -1);
            if (status != 0) return null;
            Map<String, Object> result = (Map<String, Object>) resp.get("result");
            if (result == null) return null;
            Map<String, Object> component = (Map<String, Object>) result.get("address_component");
            if (component == null) return null;
            return (String) component.get("city");
        } catch (Exception e) {
            log.error("[CityBackfill] 腾讯地图 API 调用失败: {}", e.getMessage());
            return null;
        }
    }
}

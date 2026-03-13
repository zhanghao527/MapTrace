package com.timemap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.timemap.mapper.SubscribeMessageMapper;
import com.timemap.mapper.UserMapper;
import com.timemap.model.entity.SubscribeMessage;
import com.timemap.model.entity.User;
import com.timemap.service.WxSubscribeMessageService;
import com.timemap.util.WxApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxSubscribeMessageServiceImpl implements WxSubscribeMessageService {

    private final SubscribeMessageMapper subscribeMessageMapper;
    private final UserMapper userMapper;
    private final WxApiUtil wxApiUtil;
    private final RestTemplate restTemplate;

    private static final String SEND_URL =
            "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token={accessToken}";

    @Override
    @Transactional
    public void recordSubscription(Long userId, String templateId) {
        SubscribeMessage record = subscribeMessageMapper.selectOne(
                new LambdaQueryWrapper<SubscribeMessage>()
                        .eq(SubscribeMessage::getUserId, userId)
                        .eq(SubscribeMessage::getTemplateId, templateId));

        if (record != null) {
            record.setRemainingCount(record.getRemainingCount() + 1);
            subscribeMessageMapper.updateById(record);
        } else {
            record = new SubscribeMessage();
            record.setUserId(userId);
            record.setTemplateId(templateId);
            record.setRemainingCount(1);
            subscribeMessageMapper.insert(record);
        }
    }

    @Override
    @Transactional
    public boolean trySend(Long userId, String templateId, String page, Map<String, String> data) {
        // 查询剩余额度
        SubscribeMessage record = subscribeMessageMapper.selectOne(
                new LambdaQueryWrapper<SubscribeMessage>()
                        .eq(SubscribeMessage::getUserId, userId)
                        .eq(SubscribeMessage::getTemplateId, templateId));

        if (record == null || record.getRemainingCount() <= 0) {
            log.debug("用户 {} 模板 {} 无订阅额度，跳过推送", userId, templateId);
            return false;
        }

        // 获取用户 openid
        User user = userMapper.selectById(userId);
        if (user == null || user.getOpenid() == null) {
            log.warn("用户 {} 不存在或无 openid，跳过推送", userId);
            return false;
        }

        // 构建请求体
        Map<String, Object> body = new HashMap<>();
        body.put("touser", user.getOpenid());
        body.put("template_id", templateId);
        if (page != null && !page.isEmpty()) {
            body.put("page", page);
        }

        // 转换 data 格式: {"key": "value"} -> {"key": {"value": "value"}}
        Map<String, Object> templateData = new HashMap<>();
        data.forEach((key, value) -> {
            Map<String, String> item = new HashMap<>();
            item.put("value", value);
            templateData.put(key, item);
        });
        body.put("data", templateData);

        try {
            String accessToken = wxApiUtil.getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    SEND_URL, httpEntity, Map.class, accessToken);

            if (response != null && Integer.valueOf(0).equals(response.get("errcode"))) {
                // 发送成功，扣减额度
                record.setRemainingCount(record.getRemainingCount() - 1);
                subscribeMessageMapper.updateById(record);
                log.info("订阅消息发送成功: userId={}, templateId={}", userId, templateId);
                return true;
            } else {
                log.warn("订阅消息发送失败: userId={}, response={}", userId, response);
                // 如果是 43101（用户拒绝）则清零额度
                if (response != null && Integer.valueOf(43101).equals(response.get("errcode"))) {
                    record.setRemainingCount(0);
                    subscribeMessageMapper.updateById(record);
                }
                return false;
            }
        } catch (Exception e) {
            log.error("订阅消息发送异常: userId={}, templateId={}", userId, templateId, e);
            return false;
        }
    }
}

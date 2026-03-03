package com.timemap.util;

import com.timemap.config.WxConfig;
import com.timemap.model.dto.WxSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class WxApiUtil {

    private final WxConfig wxConfig;
    private final RestTemplate restTemplate;

    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    public WxSessionResponse code2Session(String code) {
        WxSessionResponse response = restTemplate.getForObject(
                CODE2SESSION_URL,
                WxSessionResponse.class,
                wxConfig.getAppId(),
                wxConfig.getAppSecret(),
                code
        );

        if (response == null || !response.isSuccess()) {
            String errMsg = response != null ? response.getErrmsg() : "微信接口无响应";
            throw new RuntimeException("微信登录失败: " + errMsg);
        }

        return response;
    }
}

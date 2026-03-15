package com.maptrace.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();

        // 微信接口返回 text/plain，需要让 Jackson 也能处理
        restTemplate.getMessageConverters().forEach(converter -> {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.setSupportedMediaTypes(Arrays.asList(
                        MediaType.APPLICATION_JSON,
                        MediaType.TEXT_PLAIN,
                        new MediaType("application", "*+json")
                ));
            }
        });

        return restTemplate;
    }
}

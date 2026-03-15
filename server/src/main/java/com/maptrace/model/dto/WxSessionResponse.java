package com.maptrace.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WxSessionResponse {

    private String openid;

    @JsonProperty("session_key")
    private String sessionKey;

    private Integer errcode;

    private String errmsg;

    public boolean isSuccess() {
        return errcode == null || errcode == 0;
    }
}

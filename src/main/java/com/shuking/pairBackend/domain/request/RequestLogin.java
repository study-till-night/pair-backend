package com.shuking.pairBackend.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "登录请求体") //  定义接口使用到的model的信息
public class RequestLogin implements Serializable {

    @Schema(description = "用户名",example = "shu-king")
    private String username;

    @Schema(description = "密码",example = "t128129..")
    private String password;
}

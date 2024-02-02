package com.shuking.pairBackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(name = "队伍更改DTO")
public class TeamUpdateDto {
    @Schema(description = "队伍id")
    private String teamId;

    @Schema(description = "队伍名称",example = "team111")
    private String name;

    @Schema(description = "队伍简介",example = "this is a great team")
    private String description;

    @Schema(description = "最大人数",example = "10")
    private Integer maxNum;

    @Schema(description = "过期时间",example = "2023-11-12 00:00:00")
    private Date expireTime;

    @Schema(description = "队伍类型",example = "0")
    private Integer status;

    @Schema(description = "队伍密码",example = "668800")
    private String password;
}

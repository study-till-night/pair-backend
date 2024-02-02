package com.shuking.pairBackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 前端调用队伍新增时DTO对象
 */
@Data
@Schema(description = "队伍新增DTO")
public class TeamAddDto implements Serializable {

    @Schema(description = "队伍名称",example = "team111")
    private String name;

    @Schema(description = "队伍简介",example = "this is a great team")
    private String description;

    @Schema(description = "最大人数",example = "10")
    private Integer maxNum;

    @Schema(description = "过期时间",example = "2023-11-12 00:00:00")
    private Date expireTime;

   /* @Schema(description = "创建用户的id")
    private String  userId;*/

    @Schema(description = "队伍类型",example = "0")
    private Integer status;

    @Schema(description = "队伍密码",example = "668800")
    private String password;
}

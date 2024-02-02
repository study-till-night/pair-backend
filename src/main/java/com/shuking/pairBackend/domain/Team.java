package com.shuking.pairBackend.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍
 * @TableName team
 */
@TableName(value ="team")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Team implements Serializable {
    /**
     * id
     */
    @TableId(value = "team_id",type = IdType.ASSIGN_UUID)
    private String  teamId;

    /**
     * 队伍名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 最大人数
     */
    @TableField(value = "max_num")
    private Integer maxNum;

    /**
     * 过期时间
     */
    @TableField(value = "expire_time")
    private Date expireTime;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private String  userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 成员数量
     */
    @TableField(value = "join_num")
    private Integer joinNum;

    /**
     * 
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
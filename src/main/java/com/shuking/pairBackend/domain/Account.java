package com.shuking.pairBackend.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 账户表
 *
 * @TableName account
 */
@TableName(value = "account")
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Account implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @TableId(value = "user_id", type = IdType.ASSIGN_UUID)
    private String userId;
    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 用户昵称
     */
    @TableField(value = "nick_name")
    private String nickName;
    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;
    /**
     * 简介
     */
    @TableField(value = "description")
    private String description;
    /**
     * 性别
     */
    @TableField(value = "gender")
    private Integer gender;
    /**
     * 用户标签
     */
    @TableField(value = "tags")
    private String tags;

    /**
     * 用户角色
     */
    @TableField(value = "role")
    private Integer role;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;
    /**
     * 手机号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 账户状态是否正常
     */
    @TableField(value = "is_valid")
    private Integer isValid;
    /**
     *
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;
    /**
     *
     */
    @TableField(value = "update_time")
    private Date updateTime;
    /**
     * java实体类属性不能以下划线命名 否则会出现映射问题
     */
    @TableField(value = "create_time")
    private Date createTime;
}
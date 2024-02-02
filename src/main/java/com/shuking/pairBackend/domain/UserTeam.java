package com.shuking.pairBackend.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户队伍关系
 * @TableName user_team
 */
@TableName(value ="user_team")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTeam implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String  id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private String  userId;

    /**
     * 队伍id
     */
    @TableField(value = "team_id")
    private String teamId;

    /**
     * 加入时间
     */
    @TableField(value = "join_time")
    private Date joinTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

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

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
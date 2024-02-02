package com.shuking.pairBackend.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 标签表
 *
 * @TableName tag
 */
@TableName(value = "tag")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @TableId(value = "tag_id")
    private String tag_id;
    /**
     * 标签名
     */
    @TableField(value = "tag_name")
    private String tag_name;
    /**
     * 提出的用户
     */
    @TableField(value = "user_id")
    private String user_id;
    /**
     * 父标签id
     */
    @TableField(value = "parent_id")
    private String parent_id;
    /**
     * 是否为父标签 0不是 1是
     */
    @TableField(value = "is_parent")
    private Integer is_parent;
    /**
     *
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer is_deleted;
    /**
     *
     */
    @TableField(value = "update_time")
    private Date update_time;
    /**
     *
     */
    @TableField(value = "create_time")
    private Date create_time;
}
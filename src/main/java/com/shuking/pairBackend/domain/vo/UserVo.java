package com.shuking.pairBackend.domain.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserVo {
    private String userId;

    private String username;

    private String nickName;

    private String description;

    private Integer gender;

    private String tags;

    private String email;

    private String phone;

    private Integer isValid;

    private Date createTime;
}

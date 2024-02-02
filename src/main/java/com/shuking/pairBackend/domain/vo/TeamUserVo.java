package com.shuking.pairBackend.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeamUserVo {
    private String teamId;

    private String name;

    private String description;

    private Integer maxNum;

    private Date expireTime;

    private String userId;

    private Integer status;
    /**
     * 队长
     */
    private UserVo createUser;
    /**
     * 当前队伍成员数
     */
    private Integer joinNum;
    /**
     * 当前用户是否已加入该队伍
     */
    private Boolean hasJoin;

    private Date createTime;

    /**
     * 当前队伍的成员
     */
    private List<UserVo> memberList;
}

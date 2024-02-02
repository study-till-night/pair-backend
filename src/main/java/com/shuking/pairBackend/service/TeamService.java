package com.shuking.pairBackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shuking.pairBackend.domain.Account;
import com.shuking.pairBackend.domain.Team;
import com.shuking.pairBackend.domain.dto.TeamJoinDto;
import com.shuking.pairBackend.domain.dto.TeamQueryDto;
import com.shuking.pairBackend.domain.dto.TeamUpdateDto;
import com.shuking.pairBackend.domain.vo.PageResult;
import com.shuking.pairBackend.domain.vo.TeamUserVo;

/**
 * @author HP
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2023-11-13 09:55:30
 */
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     *
     * @param team      队伍
     * @param loginUser 当前登录用户
     * @return 队伍id
     */
    String addTeam(Team team, Account loginUser);

    /**
     * 搜索队伍
     *
     * @param teamQuery 队伍查询dto
     * @return 队伍列表
     */
    PageResult<TeamUserVo> listTeams(TeamQueryDto teamQuery, Account loginUser);

    /**
     * 更新队伍
     *
     * @param teamUpdateDto 队伍更新dto
     * @return 是否成功
     */
    boolean updateTeam(TeamUpdateDto teamUpdateDto, Account loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoin 队伍加入dto
     * @return 是否成功
     */
    boolean joinTeam(TeamJoinDto teamJoin, Account loginUser);

    /**
     * 退出队伍
     *
     * @param teamId 队伍id
     * @return 是否成功
     */
    boolean quitTeam(String teamId, Account loginUser);

    /**
     * 删除（解散）队伍
     *
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(String id, Account loginUser);
}

package com.shuking.pairBackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuking.pairBackend.common.ErrorCode;
import com.shuking.pairBackend.constant.enums.TeamStatusEnum;
import com.shuking.pairBackend.constant.redisKey.TeamRedisKey;
import com.shuking.pairBackend.domain.Account;
import com.shuking.pairBackend.domain.Team;
import com.shuking.pairBackend.domain.UserTeam;
import com.shuking.pairBackend.domain.dto.TeamJoinDto;
import com.shuking.pairBackend.domain.dto.TeamQueryDto;
import com.shuking.pairBackend.domain.dto.TeamUpdateDto;
import com.shuking.pairBackend.domain.vo.PageResult;
import com.shuking.pairBackend.domain.vo.TeamUserVo;
import com.shuking.pairBackend.domain.vo.UserVo;
import com.shuking.pairBackend.exception.BusinessException;
import com.shuking.pairBackend.mapper.TeamMapper;
import com.shuking.pairBackend.service.AccountService;
import com.shuking.pairBackend.service.TeamService;
import com.shuking.pairBackend.service.UserTeamService;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author HP
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-11-13 09:55:30
 */
@Service
@Log4j2
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private AccountService accountService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建队伍
     *
     * @param team 队伍
     * @return 队伍id
     */
    @Override
    @Transactional  //  一部分代码需要事务不建议在方法上直接加该注解
    public String addTeam(Team team, Account loginUser) {
        if (team == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String userId = loginUser.getUserId();
        /*if (!team.getUserId().equals(userId))
            throw new BusinessException(ErrorCode.NO_AUTH);*/

        //  判断合法性
        isValidTeam(team);

        // 7. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍 使用synchronized关键字包裹
        synchronized (this) {
            QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            long hasTeamNum = this.count(queryWrapper);
            if (hasTeamNum >= 5) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
            }

            // 8. 插入队伍信息到队伍表
            team.setTeamId(null);
            team.setUserId(userId);
            boolean result = this.save(team);
            String teamId = team.getTeamId();
            if (!result || teamId == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
            }

            // 9. 插入用户  => 队伍关系到关系表
            UserTeam userTeam = new UserTeam();
            userTeam.setUserId(userId);
            userTeam.setTeamId(teamId);
            userTeam.setJoinTime(new Date());
            result = userTeamService.save(userTeam);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
            }
            return teamId;
        }
    }

    /**
     * 搜索队伍
     *
     * @param teamQuery 队伍查询dto
     * @return 队伍列表
     */
    @Override
    public PageResult<TeamUserVo> listTeams(TeamQueryDto teamQuery, Account loginUser) {
        if (teamQuery == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

        //  判断成员数上限
        Integer maxNum = teamQuery.getMaxNum();
        if (maxNum != null)
            queryWrapper.le("max_num", maxNum);

        //  判断队伍状态
        Integer status = teamQuery.getStatus();
        if (TeamStatusEnum.getEnumByValue(status) != null)
            queryWrapper.eq("status", status);

        //  剔除已过期队伍
        queryWrapper.and(qw -> qw.isNull("expire_time").or().gt("expire_time", new Date()));

        //  添加搜索条件
        String searchText = teamQuery.getSearchText();
        if (!StringUtils.isBlank(searchText))
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));


        //  获取分页结果
        int pageSize = teamQuery.getPageSize();
        int pageNum = teamQuery.getPageNum();

        //  todo 若是默认查询 可通过定时任务在redis中存入热点队伍列表  每次查询请求从redis中随机选取n条返回
        Page<Team> teamPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Team> teamList = teamPage.getRecords();

        //  转换为VO
        ArrayList<TeamUserVo> teamVoList = new ArrayList<>();
        for (Team team : teamList) {
            TeamUserVo tempTeamVo = new TeamUserVo();
            String userId = team.getUserId();
            String teamId = team.getTeamId();

            BeanUtils.copyProperties(team, tempTeamVo);
            //  关联创建用户
            Account createUser = accountService.getById(userId);
            tempTeamVo.setCreateUser(accountService.getSafetyUser(createUser));

            //  select () from team_user tu join account a on tu.user_id = a.user_id ordered by tu.join_time    按加入时间排序
            //  关联队友    获取队员uid集合
            List<String> memberIdList = userTeamService.list(new QueryWrapper<UserTeam>()
                    .eq("team_id", teamId)).stream().map(UserTeam::getUserId).collect(Collectors.toList());
            //  获取队员对象集合
            List<UserVo> memberVoList = new ArrayList<>();
            //  使用mysql in() 传入的数组长度不得为空
            if (!memberIdList.isEmpty())
                memberVoList = accountService.listByIds(memberIdList)
                        .stream().map(accountService::getSafetyUser).collect(Collectors.toList());
            tempTeamVo.setMemberList(memberVoList);

            //  用户是否已经加入当前队伍
            tempTeamVo.setHasJoin(userTeamService.count(new QueryWrapper<UserTeam>()
                    .eq("team_id", teamId).eq("user_id", loginUser.getUserId())) != 0);
            teamVoList.add(tempTeamVo);
        }

        //  封装为page对象
        PageResult<TeamUserVo> result = new PageResult<>();
        result.setPageSize(teamPage.getSize());
        result.setPageNum(teamPage.getCurrent());
        result.setTotal(teamPage.getTotal());
        result.setRecords(teamVoList);
        return result;
    }

    /**
     * 更新队伍
     *
     * @param teamUpdateDto 队伍更新dto
     * @return 是否成功
     */
    @Override
    public boolean updateTeam(TeamUpdateDto teamUpdateDto, Account loginUser) {
        if (teamUpdateDto == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String teamId = Optional.ofNullable(teamUpdateDto.getTeamId()).orElse("");
        Team team = this.getById(teamId);

        //  判断队伍是否存在 / 是否过期
        hasTeam(team);

        //  更新的队伍不属于当前用户
        if (!team.getUserId().equals(loginUser.getUserId()))
            throw new BusinessException(ErrorCode.NO_AUTH);

        BeanUtils.copyProperties(teamUpdateDto, team);
        isValidTeam(team);

        /*//  判断状态是否合规
        TeamStatusEnum teamStatus = TeamStatusEnum.getEnumByValue(teamUpdateDto.getStatus());
        String password = teamUpdateDto.getPassword();
        if (teamStatus.equals(TeamStatusEnum.SECRET) && StringUtils.isBlank(password))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "私密队伍必须设置密码");
        if (!teamStatus.equals(TeamStatusEnum.SECRET) && StringUtils.isNotBlank(password))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非私密队伍必须不得设置密码");

        //  判断过期时间合法性
        expireTime = teamUpdateDto.getExpireTime();
        if (expireTime != null && expireTime.before(new Date()))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期时间不得早于当前时间");*/
        return this.updateById(team);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinDto 队伍加入dto
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean joinTeam(TeamJoinDto teamJoinDto, Account loginUser) {
        String userId = loginUser.getUserId();

        String teamId = Optional.ofNullable(teamJoinDto.getTeamId()).orElse("");
        // todo 不断传入空值或无效值可能会造成缓存穿透 造成 mysql压力
        Team joinedTeam = this.getById(teamId);
        //  判断队伍是否存在 / 是否过期
        hasTeam(joinedTeam);

        //  todo  获取分布式锁  不同队伍加不同锁 ?? 是否可行
        RLock lock = redissonClient.getLock(TeamRedisKey.JOIN_LOCK_KEY + teamId.substring(0, 7));

        //  todo  锁力度过大 对于加入不同队伍的请求应当并行处理
        //  不断访问锁 直到占有
        while (true) {
            try {
                if (lock.tryLock(30, -1, TimeUnit.MILLISECONDS)) {
                    //  队伍人数必须未满
                    long memberNum = userTeamService.count(new QueryWrapper<UserTeam>().eq("team_id", teamId));
                    if (memberNum >= joinedTeam.getMaxNum())
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "加入的队伍人数已满");

                    //  私有队伍不得加入
                    TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(joinedTeam.getStatus());
                    if (statusEnum.equals(TeamStatusEnum.PRIVATE))
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不得加入私有队伍");

                    //  加密的队伍需要密码
                    if (statusEnum.equals(TeamStatusEnum.SECRET)) {
                        String joinPassword = teamJoinDto.getPassword();
                        if (StringUtils.isBlank(joinPassword))
                            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍需要密码");

                        String teamPassword = joinedTeam.getPassword();
                        if (!teamPassword.equals(joinPassword))
                            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
                    }

                    //  用户最多加入5个队伍
                    //  todo  个人加入队伍数量也得加锁
                    QueryWrapper<UserTeam> recordQueryWrapper = new QueryWrapper<>();
                    if (userTeamService.count(recordQueryWrapper.eq("user_id", userId)) >= 5)
                        throw new BusinessException(ErrorCode.FORBIDDEN, "只能同时加入5个队伍");

                    //  用户不得多次加入
                    if (userTeamService.count(recordQueryWrapper.eq("team_id", teamId)) > 0)
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "已加入当前队伍");

                    //  新增入队记录
                    UserTeam teamJoinRecord = new UserTeam();
                    teamJoinRecord.setUserId(userId);
                    teamJoinRecord.setTeamId(teamId);
                    teamJoinRecord.setJoinTime(new Date());

                    boolean result = userTeamService.save(teamJoinRecord)
                            //  队伍成员数+1
                            && this.update(new UpdateWrapper<Team>().eq("team_id", teamId).set("join_num", joinedTeam.getJoinNum() + 1));
                    if (!result)
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入失败 --系统出错");
                    return true;
                }
            } catch (InterruptedException e) {
                log.error("redis-lock-error on team-join userId--{}",userId);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入失败 --redis出错");
            }finally {
                lock.unlock();
            }
        }
    }

    /**
     * 退出队伍
     *
     * @param teamId 队伍id
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean quitTeam(String teamId, Account loginUser) {
        if (StringUtils.isEmpty(teamId))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String userId = loginUser.getUserId();
        Team quitedTeam = this.getById(teamId);

        //  队伍是否存在
        hasTeam(quitedTeam);

        //  判断是否在队伍中
       isMemberOf(teamId,userId);

        RLock lock = redissonClient.getLock(TeamRedisKey.QUIT_LOCK_KEY + teamId.substring(0, 7));

        while (true) {
            try {
                if (lock.tryLock(30, -1, TimeUnit.MILLISECONDS)) {
                    //  我是否是队长
                    if (quitedTeam.getUserId().equals(userId)) {
                        QueryWrapper<UserTeam> teamQueryWrapper = new QueryWrapper<>();
                        //  取出当前队伍中的前两名成员
                        teamQueryWrapper.eq("team_id", teamId).last("order by join_time limit 2");
                        List<UserTeam> headTwoMember = userTeamService.list(teamQueryWrapper);
                        //    当队伍至少2人时  得到顺位队长的uid
                        if (headTwoMember.size() > 1) {
                            String nextLeaderUid = headTwoMember.get(1).getUserId();
                            //  设置队长为下一成员
                            boolean updateResult = this.update(new UpdateWrapper<Team>().eq("team_id", teamId).set("user_id", nextLeaderUid));
                            if (!updateResult)
                                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出失败 --更新队长出错");
                        }
                        //  如果只剩一人 队伍不复存在
                        else {
                            boolean removeTeamResult = this.removeById(teamId);
                            if (!removeTeamResult)
                                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出失败 --队伍删除出错");
                        }
                    }
                    //  退出队伍
                    boolean removeResult = userTeamService.remove(new QueryWrapper<UserTeam>().eq("user_id", userId).eq("team_id", teamId));
                    if (!removeResult)
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出失败 --删除记录出错");

                    //  队伍成员数-1
                    boolean result = this.update(new UpdateWrapper<Team>().eq("team_id", teamId).set("join_num", quitedTeam.getJoinNum() - 1));
                    if (!result)
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出失败 --更新队伍人数出错");

                    return true;
                }
            } catch (InterruptedException e) {
                log.error("redis-lock-error on team-quit userId--{}",userId);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入失败 --redis出错");
            }finally {
                lock.unlock();
            }
        }

    }

    /**
     * 解散队伍
     *
     * @param teamId 要解散的队伍id
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean deleteTeam(String teamId, Account loginUser) {
        if (StringUtils.isBlank(teamId))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String userId = loginUser.getUserId();
        Team deletedTeam = this.getById(teamId);

        //  队伍是否存在
        hasTeam(deletedTeam);

        //  只有队长才能解散队伍
        if (!userId.equals(deletedTeam.getUserId()))
            throw new BusinessException(ErrorCode.NO_AUTH, "无权解散队伍");

        //  判断是否在队伍中
        isMemberOf(teamId, userId);

        //  删除入队记录
        boolean removeRecordRes = userTeamService.remove(new QueryWrapper<UserTeam>().eq("team_id", teamId));
        if (!removeRecordRes)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散失败 --删除记录失败");

        //  删除该队伍
        boolean removeTeamRes = this.removeById(teamId);
        if (!removeTeamRes)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散失败 --删除队伍失败");

        //  队伍成员数-1
        boolean updateRes = this.update(new UpdateWrapper<Team>().eq("team_id", teamId).set("join_num", 0));
        if (!updateRes)
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散失败 --更新队伍人数出错");
        return true;
    }

    /**
     * 判断队伍参数是否合规
     *
     * @param team 待检测对象
     */
    private void isValidTeam(Team team) {
        //   1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //  判断队伍当前人数是否已经大于修改后的最大人数
        // todo 解决并发造成的人数不一致
        long memberNum = userTeamService.count(new QueryWrapper<UserTeam>().eq("team_id", team.getTeamId()));
        if (memberNum > maxNum)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数上限不得小于当前成员人数");

        //   2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }

        //   3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }

        //   4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }

        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();

        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        } else {
            if (StringUtils.isNotBlank(password))
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "非加密队伍无法设置密码");
        }
        // 6. 超时时间 是否早于 当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间早于当前时间");
        }
    }

    /**
     * 判断队伍是否存在 或者过期
     */
    private void hasTeam(Team team) {
        if (team == null)
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date()))
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已过期");
    }

    /**
     * 判断是否在队伍中
     *
     * @param teamId 队伍id
     * @param userId 当前用户id
     */
    private void isMemberOf(String teamId, String userId) {
        //  判断是否在队伍中
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<UserTeam>().eq("user_id", userId).eq("team_id", teamId);
        if (userTeamService.count(queryWrapper) == 0)
            throw new BusinessException(ErrorCode.FORBIDDEN, "不在当前队伍中");
    }
}
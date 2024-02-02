package com.shuking.pairBackend.controller;

import com.google.gson.Gson;
import com.shuking.pairBackend.common.BaseResult;
import com.shuking.pairBackend.common.ErrorCode;
import com.shuking.pairBackend.domain.Account;
import com.shuking.pairBackend.domain.Team;
import com.shuking.pairBackend.domain.dto.TeamAddDto;
import com.shuking.pairBackend.domain.dto.TeamJoinDto;
import com.shuking.pairBackend.domain.dto.TeamQueryDto;
import com.shuking.pairBackend.domain.dto.TeamUpdateDto;
import com.shuking.pairBackend.domain.vo.PageResult;
import com.shuking.pairBackend.domain.vo.TeamUserVo;
import com.shuking.pairBackend.exception.BusinessException;
import com.shuking.pairBackend.service.AccountService;
import com.shuking.pairBackend.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/team")
@Tag(name = "队伍接口", description = "队伍创建、加入、解散等行为")
@CrossOrigin(value = "http://127.0.0.1:5173", allowCredentials = "true")
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private AccountService accountService;

    /**
     * 创建队伍
     *
     * @param teamAddDto 队伍创建dto
     * @return 队伍id
     */
    @PostMapping("/add")
    @Operation(summary = "创建队伍")
    public BaseResult<String> addTeam(@RequestBody @Parameter(description = "队伍新增DTO") TeamAddDto teamAddDto, HttpServletRequest request) {
        if (teamAddDto == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        Account currentUser = accountService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddDto, team);
        String teamId = teamService.addTeam(team, currentUser);
        return BaseResult.success("创建成功", teamId);
    }

    /**
     * 分页搜索队伍
     *
     * @param teamQuery dto
     * @return 队伍分页列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页搜索队伍")
    public BaseResult<PageResult<TeamUserVo>> PageTeam(@RequestBody @Parameter(description = "队伍查询DTO") TeamQueryDto teamQuery, HttpServletRequest request) {
        if (teamQuery == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        Account currentUser = accountService.getLoginUser(request);
        PageResult<TeamUserVo> teamPageList = teamService.listTeams(teamQuery,currentUser);
        return BaseResult.success(teamPageList);
    }

    /**
     * 更改队伍
     *
     * @param teamUpdate dto
     * @return 是否成功
     */
    @PostMapping("/update")
    @Operation(summary = "更改队伍")
    public BaseResult<Boolean> updateTeam(@RequestBody @Parameter(description = "队伍更新DTO") TeamUpdateDto teamUpdate, HttpServletRequest request) {
        if (teamUpdate == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        Account currentUser = accountService.getLoginUser(request);

        boolean isSuccessful = teamService.updateTeam(teamUpdate, currentUser);
        return isSuccessful ? BaseResult.success("修改成功") : BaseResult.error("修改失败", ErrorCode.SYSTEM_ERROR.getCode(), "系统异常");
    }

    /**
     * 加入队伍
     *
     * @param teamJoin DTO
     * @return 是否成功
     */
    @PostMapping("/join")
    @Operation(summary = "加入队伍")
    public BaseResult<Boolean> joinTeam(@RequestBody @Parameter(description = "队伍加入DTO") TeamJoinDto teamJoin, HttpServletRequest request) {
        if (teamJoin == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        Account currentUser = accountService.getLoginUser(request);

        boolean isSuccessful = teamService.joinTeam(teamJoin, currentUser);
        return isSuccessful ? BaseResult.success("加入成功") : BaseResult.error("加入失败", ErrorCode.SYSTEM_ERROR.getCode(), "系统异常");
    }

    /**
     * 退出队伍
     *
     * @param teamIdJson 要退出的队伍id
     * @return 是否成功
     */
    @PostMapping("/quit")
    @Operation(summary = "退出队伍")
    public BaseResult<Boolean> quitTeam(@RequestBody @Parameter(description = "要退出的队伍id") String teamIdJson, HttpServletRequest request) {
        String teamId = isJsonTeamIdValid(teamIdJson);

        Account currentUser = accountService.getLoginUser(request);
        boolean isSuccessful = teamService.quitTeam(teamId, currentUser);
        return isSuccessful ? BaseResult.success("退出成功") : BaseResult.error("退出失败", ErrorCode.SYSTEM_ERROR.getCode(), "系统异常");
    }

    /**
     * 退出队伍
     *
     * @param teamIdJson 要退出的队伍id
     * @return 是否成功
     */
    @PostMapping("/delete")
    @Operation(summary = "解散队伍")
    public BaseResult<Boolean> deleteTeam(@RequestBody @Parameter(description = "要解散的队伍id") String teamIdJson, HttpServletRequest request) {
        String teamId = isJsonTeamIdValid(teamIdJson);

        Account currentUser = accountService.getLoginUser(request);
        boolean isSuccessful = teamService.deleteTeam(teamId, currentUser);
        return isSuccessful ? BaseResult.success("解散成功") : BaseResult.error("解散失败", ErrorCode.SYSTEM_ERROR.getCode(), "系统异常");
    }

    /**
     * 判断json id合法性 若合法则返回反序列化后id
     *
     * @param teamIdJson 序列化id
     */
    private String isJsonTeamIdValid(String teamIdJson) {
        if (teamIdJson == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        //  反序列化
        String teamId = new Gson().fromJson(teamIdJson, String.class);
        if (StringUtils.isBlank(teamId))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        return teamId;
    }
}
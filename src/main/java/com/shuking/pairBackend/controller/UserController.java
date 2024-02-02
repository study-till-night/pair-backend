package com.shuking.pairBackend.controller;

import com.shuking.pairBackend.common.BaseResult;
import com.shuking.pairBackend.common.ErrorCode;
import com.shuking.pairBackend.domain.Account;
import com.shuking.pairBackend.domain.request.RequestLogin;
import com.shuking.pairBackend.domain.request.RequestRegister;
import com.shuking.pairBackend.domain.vo.PageResult;
import com.shuking.pairBackend.domain.vo.UserVo;
import com.shuking.pairBackend.exception.BusinessException;
import com.shuking.pairBackend.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
//  前端携带cookie 允许访问的地址必须为自己的源地址 不能为* 且需设置   allowCredentials    为true
@CrossOrigin(value = "http://127.0.0.1:5173", allowCredentials = "true")
@Tag(name = "账户操作", description = "用户注册、登录、下线等一系列账户行为")  //定义该controller名称及介绍
@Log4j2
public class UserController {

    @Resource
    private AccountService accountService;


    /**
     * 用户注册
     *
     * @param registerBody 注册请求头
     * @return 注册结果 成功返回用户id
     */
    @Operation(summary = "用户注册接口", description = "传入的是封装好的RequestRegister对象")    //  定义该接口名称及介绍
    @PostMapping("/register")
    public BaseResult<String> doRegister(@RequestBody @Parameter(description = "注册请求体") RequestRegister registerBody) {
        if (registerBody == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String username = registerBody.getUsername();
        String password = registerBody.getPassword();
        String checkPassword = registerBody.getCheckPassword();

        if (StringUtils.isAnyBlank(username, password, checkPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String uid = accountService.register(username, password, checkPassword);
        return BaseResult.success("注册成功", uid);
    }

    /**
     * 用户登录
     *
     * @param loginBody 登录请求体
     * @return 登录后用户脱敏信息
     */
    @Operation(summary = "用户登录接口", description = "传入的是封装好的loginBody对象")    //  定义该接口名称及介绍
    @PostMapping("/login")
    public BaseResult<UserVo> doLogin(@RequestBody @Parameter(description = "登录请求体") RequestLogin loginBody, HttpServletRequest httpServletRequest) {
        if (loginBody == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        String username = loginBody.getUsername();
        String password = loginBody.getPassword();

        if (StringUtils.isAnyBlank(username, password))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

        UserVo account = accountService.doLogin(username, password, httpServletRequest);
        return BaseResult.success("登录成功", account);
    }

    /**
     * 用户退出
     *
     * @param request http
     * @return 成功
     */
    @Operation(summary = "用户退出")
    @PostMapping("/logout")
    public BaseResult<String> doLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        accountService.userLogout(request);
        return BaseResult.success("退出成功");
    }

    /**
     * 获取当前用户
     *
     * @param request http
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户")
    @GetMapping("/current")
    public BaseResult<UserVo> getCurrentUser(HttpServletRequest request) {
        Account currentUser = accountService.getLoginUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        String userId = currentUser.getUserId();
        // TODO 校验用户是否合法
        Account user = accountService.getById(userId);
        UserVo currentUserVo = accountService.getSafetyUser(user);
        return BaseResult.success(currentUserVo);
    }

    /**
     * 更新用户
     *
     * @param user    传入用户
     * @param request http
     * @return 结果
     */
    @Operation(summary = "更新用户")
    @PostMapping("/update")
    public BaseResult<Integer> updateUser(@RequestBody @Parameter(description = "修改的用户对象") Account user, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Account loginUser = accountService.getLoginUser(request);
        int result = accountService.updateUser(user, loginUser);
        return BaseResult.success("更新成功", result);
    }

    /**
     * 根据标签搜索用户
     * tagList[] 前端传递数组时使用get 会在url中体现为[] 所以在接收时添加[]即可解决
     * 但tomcat高版本对url路径符号检测严格 不允许保留字符出现 故采用post请求 以body形式传入
     * string数组类型经测验不能解析
     *
     * @param tagNameList 标签列表
     * @return 匹配的用户
     */
    @Operation(summary = "根据标签搜索用户")
    @PostMapping("/search-by-tags")
    public BaseResult<List<UserVo>> searchByTags(@RequestBody @Parameter(name = "tagNameList", description = "搜索标签") List<String> tagNameList) {
        if (tagNameList == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        List<UserVo> matchedUsers = accountService.searchUsersByTags(tagNameList);
        return BaseResult.success(matchedUsers);
    }

    /**
     * 首页推荐
     *
     * @return 用户列表
     */
    @GetMapping("/recommend")
    @Operation(summary = "首页用户推荐")
    public BaseResult<PageResult<UserVo>> recommend(@Parameter(description = "页面大小") Integer pageSize,
                                                    @Parameter(description = "页面序号") Integer pageNum, HttpServletRequest request) {
        pageSize = Optional.ofNullable(pageSize).orElse(10);
        pageNum = Optional.ofNullable(pageNum).orElse(1);

        PageResult<UserVo> tempPage = accountService.recommend(pageSize, pageNum, request);

        return BaseResult.success(tempPage);
        /*return BaseResult.success().add("recommendUserList", resPage.getRecords())
                .add("pageSize", resPage.getSize())
                .add("pageNum", resPage.getCurrent())
                .add("total", resPage.getTotal());*/
    }

    /**
     * 用户匹配
     *
     * @param num 匹配的数量
     * @return  匹配的用户列表
     */
    @Operation(summary = "用户匹配")
    @GetMapping("/match")
    public BaseResult<List<UserVo>> matchUser(@Parameter(description = "匹配的用户数量") Integer num, HttpServletRequest request) {
        num = Optional.ofNullable(num).orElse(10);
        if (num <= 0 || num > 20)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "匹配用户数量须在1-20之间");
        Account currentUser = accountService.getLoginUser(request);

        List<UserVo> matchedUserList = accountService.matchUser(num, currentUser);
        return BaseResult.success(matchedUserList);
    }
}
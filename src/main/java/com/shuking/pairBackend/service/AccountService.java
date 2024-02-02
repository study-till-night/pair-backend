package com.shuking.pairBackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shuking.pairBackend.domain.Account;
import com.shuking.pairBackend.domain.vo.PageResult;
import com.shuking.pairBackend.domain.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author HP
 * @description 针对表【account(账户表)】的数据库操作Service
 * @createDate 2023-10-31 19:38:48
 */
public interface AccountService extends IService<Account> {
    /**
     * 用户注册
     *
     * @param username      用户 名
     * @param password      密码
     * @param checkPassword 二次验证的密码
     * @return true 成功 false 失败
     */
    String register(String username, String password, String checkPassword);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    UserVo doLogin(String username, String password, HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request 请求对象
     */
    void userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request http
     * @return 用户信息
     */
    Account getLoginUser(HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser 原始对象
     * @return 脱敏后对象
     */
    UserVo getSafetyUser(Account originUser);

    /**
     * 根据标签列表查询用户
     *
     * @param tags 标签列表
     * @return 用户列表
     */
    List<UserVo> searchUsersByTags(List<String> tags);

    /**
     * 更新用户
     *
     * @param account      传入对象
     * @param loginAccount 当前登录对象
     * @return 更新结果
     */
    int updateUser(Account account, Account loginAccount);

    /**
     * 首页推荐
     *
     * @param pageSize 页面大小
     * @param pageNum  当前页序号
     * @param request  http
     * @return page对象
     */
    PageResult<UserVo> recommend(Integer pageSize, Integer pageNum, HttpServletRequest request);

    /**
     * 用户匹配
     *
     * @param num 匹配的数量
     * @return  匹配的用户列表
     */
    List<UserVo> matchUser(Integer num, Account currentUser);
}
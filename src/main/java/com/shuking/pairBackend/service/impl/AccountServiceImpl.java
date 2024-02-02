package com.shuking.pairBackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shuking.pairBackend.common.ErrorCode;
import com.shuking.pairBackend.constant.UserConstant;
import com.shuking.pairBackend.constant.redisKey.AccountRedisKey;
import com.shuking.pairBackend.domain.Account;
import com.shuking.pairBackend.domain.vo.PageResult;
import com.shuking.pairBackend.domain.vo.UserVo;
import com.shuking.pairBackend.exception.BusinessException;
import com.shuking.pairBackend.mapper.AccountMapper;
import com.shuking.pairBackend.service.AccountService;
import com.shuking.pairBackend.utils.Algorithms;
import com.shuking.pairBackend.utils.RegexUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author HP
 * @description 针对表【account(账户表)】的数据库操作Service实现
 * @createDate 2023-10-31 19:38:48
 */
@Service
@Log4j2
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account>
        implements AccountService {
    //    加密噪音
    private static final String SALT = "shu-king";

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 用户注册
     *
     * @param username      用户 名
     * @param password      密码
     * @param checkPassword 二次验证的密码
     * @return true 成功 false 失败
     */
    @Override
    public String register(String username, String password, String checkPassword) {
        //        检验字符串是否为全空格字符串或空字符串
        if (StringUtils.isAllBlank(username, password, checkPassword))
            throw new BusinessException("字符串为空", ErrorCode.PARAMS_ERROR.getCode(), "");

        if (username.length() > 10)
            throw new BusinessException("用户名长度过长", ErrorCode.PARAMS_ERROR.getCode(), "");

        if (password.length() < 8 || checkPassword.length() < 8)
            throw new BusinessException("密码长度过短", ErrorCode.PARAMS_ERROR.getCode(), "");

        if (RegexUtils.checkValidString(username))
            throw new BusinessException("用户名包含特殊字符", ErrorCode.PARAMS_ERROR.getCode(), "");

        if (!password.equals(checkPassword))
            throw new BusinessException("密码不一致", ErrorCode.PARAMS_ERROR.getCode(), "");

        //        账户名不能重复
        if (this.count(new QueryWrapper<Account>().eq("username", username)) != 0)
            throw new BusinessException("用户名重复", ErrorCode.PARAMS_ERROR.getCode(), "");

        //  密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes(StandardCharsets.UTF_8));

        // 插入数据
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(encryptPassword);
        this.save(account);
        //        用户信息脱敏
        account.setPassword("");
        return account.getUserId();
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    @Override
    public UserVo doLogin(String username, String password, HttpServletRequest request) {
        //        检验字符串是否为全空格字符串或空字符串
        if (StringUtils.isAllBlank(username, password))
            throw new BusinessException("字符串为空", ErrorCode.PARAMS_ERROR.getCode(), "");

        if (username.length() > 10)
            throw new BusinessException("用户名长度过长", ErrorCode.PARAMS_ERROR.getCode(), "");

        if (password.length() < 8)
            throw new BusinessException("密码长度过短", ErrorCode.PARAMS_ERROR.getCode(), "");

        if (RegexUtils.checkValidString(username))
            throw new BusinessException("用户名包含特殊字符", ErrorCode.PARAMS_ERROR.getCode(), "");

        //        判断逻辑
        Account currentUser = this.getOne(new QueryWrapper<Account>()
                .eq("username", username).eq("password", DigestUtils.md5DigestAsHex((SALT + password).getBytes(StandardCharsets.UTF_8))));
        if (currentUser == null) {
            log.info("用户名或密码错误");
            throw new BusinessException("用户名或者密码错误", ErrorCode.PARAMS_ERROR.getCode(), "");
        }
        //  后续可判断短时间内登录次数 以对账户进行限流

        //        用户信息脱敏
        UserVo safetyUser = getSafetyUser(currentUser);
        //  将用户信息存入会话
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, currentUser);
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request 请求对象
     */
    @Override
    public void userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
    }

    /**
     * 用户脱敏
     *
     * @param originUser 原始对象
     * @return 脱敏后对象
     */
    @Override
    public UserVo getSafetyUser(Account originUser) {
        if (originUser == null)
            return null;
        UserVo safeTyUser = new UserVo();
        BeanUtils.copyProperties(originUser, safeTyUser);
        return safeTyUser;
    }

    /**
     * 根据标签列表查询用户
     *
     * @param tags 标签列表
     * @return 用户列表
     */
    @Override
    public List<UserVo> searchUsersByTags(List<String> tags) {
        //        参数非法 抛出异常
        if (CollectionUtils.isEmpty(tags))
            throw new BusinessException(ErrorCode.PARAMS_ERROR);

       /* 在sql层面进行过滤

        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        //       同时满足
        for (String tag : tags)
            queryWrapper.like("tags", tag);
        List<Account> accountList = accountMapper.selectList(queryWrapper);
        */

        //        在java层面进行过滤
        List<Account> accountListAll = accountMapper.selectList(null);
        return accountListAll.stream().filter(user -> {
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags))
                return false;
            //      进行序列化
            Set<String> tagsSet = new Gson().fromJson(userTags, new TypeToken<Set<String>>() {
            }.getType());
            //      java8 新特性避免null
            tagsSet = Optional.ofNullable(tagsSet).orElse(new HashSet<>());

            //      遍历传入的标签序列 有一个不在集合中就丢弃
            for (String tag : tags) {
                if (!tagsSet.contains(tag))
                    return false;
            }
            return true;
            //  逐条脱敏 返回列表
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 更新用户
     *
     * @param account      传入对象
     * @param loginAccount 当前登录对象
     * @return 更新结果
     */
    @Override
    public int updateUser(Account account, Account loginAccount) {
        if (account == null || loginAccount == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        String userId = account.getUserId();
        //  无权限更新其他用户
        if (!userId.equals(loginAccount.getUserId()))
            throw new BusinessException(ErrorCode.NO_AUTH);
        //  更新的用户不存在
        if (accountMapper.selectById(userId) == null)
            throw new BusinessException(ErrorCode.NULL_ERROR);
        return accountMapper.updateById(account);
    }

    /**
     * 首页推荐
     *
     * @param pageSize 页面大小
     * @param pageNum  当前页序号
     * @param request  http
     * @return page对象
     */
    @Override
    public PageResult<UserVo> recommend(Integer pageSize, Integer pageNum, HttpServletRequest request) {
        String recommendKey = AccountRedisKey.RECOMMEND_KEY + this.getLoginUser(request).getUserId();
        ValueOperations<String, Object> userOps = redisTemplate.opsForValue();
        Page<Account> tempPage = (Page<Account>) userOps.get(recommendKey);

        //  返回封装的分页对象
        PageResult<UserVo> result = new PageResult<>();

        //  缓存中不存在 设置新值
        if (tempPage == null) {
            tempPage = this.page(new Page<>(pageNum, pageSize), null);
            try {
                userOps.set(recommendKey, tempPage, 30000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("redis error on setting user-recommend");
            }
        }
        //  设置分页对象数值
        result.setPageSize(tempPage.getSize());
        result.setPageNum(tempPage.getCurrent());
        result.setTotal(tempPage.getTotal());
        result.setRecords(tempPage.getRecords().stream().map(this::getSafetyUser).collect(Collectors.toList()));
        return result;
    }

    /**
     * 用户匹配
     *
     * @param num 匹配的数量
     * @return 匹配的用户列表
     */
    @Override
    public List<UserVo> matchUser(Integer num, Account currentUser) {
        String userTagStr = currentUser.getTags();
        List<String> currentUserTagList = new Gson().fromJson(userTagStr, new TypeToken<List<String>>() {
        }.getType());

        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        //  只取需要的字段 & 去除无tag的用户
        queryWrapper.select("user_id", "tags").isNotNull("tags");
        //  随机选2k个用户作为候选目标
        List<Account> candidateList = this.list(queryWrapper.last("limit 2000"));
        //  todo    只维护前num个元素 节约空间
        //  存储计算结果
        List<ImmutablePair<Account, Integer>> scorePairList = new ArrayList<>();
        //  遍历候选人计算编辑距离
        for (Account account : candidateList) {
            String tempTagStr = account.getTags();
            //  无标签直接继续
            if (StringUtils.isBlank(tempTagStr) || account.getUserId().equals(currentUser.getUserId()))
                continue;

            List<String> tempTagList = new Gson().fromJson(tempTagStr, new TypeToken<List<String>>() {
            }.getType());

            int dis = Algorithms.minStrDistance(currentUserTagList, tempTagList);
            scorePairList.add(new ImmutablePair<>(account, dis));
        }
        //  进行排序并取前num个元素
        scorePairList = scorePairList
                .stream()
                .sorted(Comparator.comparingInt(Pair::getValue))
                .limit(num)
                .collect(Collectors.toList());
        //  得到最终匹配的用户id列表
        List<String> matchedIdList = scorePairList.stream()
                .map(pair -> pair.getKey().getUserId())
                .collect(Collectors.toList());
        //  最终匹配的用户
        ArrayList<UserVo> matchedUserVoList = new ArrayList<>();
        //  将用户信息以id为键值 对象为value转为map
        Map<String, List<UserVo>> userIdUserListMap = this.listByIds(matchedIdList).stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(UserVo::getUserId));
        //  按原本顺序还原
        for (String userId : matchedIdList) {
            matchedUserVoList.add(userIdUserListMap.get(userId).get(0));
        }
        return matchedUserVoList;
    }

    /**
     * 获取当前登录用户
     *
     * @param request http
     * @return 用户信息
     */
    @Override
    public Account getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Account currentUser = (Account) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return currentUser;
    }
}
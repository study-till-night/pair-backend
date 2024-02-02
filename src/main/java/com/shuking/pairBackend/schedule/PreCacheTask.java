package com.shuking.pairBackend.schedule;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuking.pairBackend.constant.redisKey.AccountRedisKey;
import com.shuking.pairBackend.domain.Account;
import com.shuking.pairBackend.service.AccountService;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class PreCacheTask {

    @Resource
    private AccountService accountService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // todo 核心用户  可以动态生成
    private final List<String> coreUserList = Arrays.asList("user1", "user2");

    //  每隔一天执行  用户推荐预热
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    public void recommendPreCache() {
        //  获取分布式锁
        RLock lock = redissonClient.getLock(AccountRedisKey.RECOMMEND_LOCK_KEY);

        try {
            //  参数1-- 其他未获取锁的线程的等待时间 0表示不等待 没抢到锁立即退出
            //  参数2-- 过期时长 -1表示开启看门狗机制 每隔10s监听一次 若任务还没结束则续费到30s
            //  解决锁过期但任务未执行完被其他线程占用的情况  默认30s是防止redisson宕机使得锁长时间存在
            //  参数3-- 时间单位
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                coreUserList.forEach(userId -> {
                    //  推荐列表的key
                    String recommendKey = AccountRedisKey.RECOMMEND_KEY + userId;
                    ValueOperations<String, Object> userOps = redisTemplate.opsForValue();

                    Page<Account> newRes = accountService.page(new Page<>(1, 20), null);
                    try {
                        userOps.set(recommendKey, newRes, 1000 * 60 * 60 * 24, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis error on setting user-recommend ---pre-cache");
                    }
                });
            }
        } catch (InterruptedException e) {
            log.error("redisson error on setting user-recommend--lock ---pre-cache");
        } finally {
            //  如果锁属于当前线程   则释放
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }
}

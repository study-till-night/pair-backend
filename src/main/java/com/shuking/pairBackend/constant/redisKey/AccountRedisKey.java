package com.shuking.pairBackend.constant.redisKey;

public interface AccountRedisKey {
    // 定时任务推荐分布式锁
    String RECOMMEND_LOCK_KEY = "pair:preCache:recommend:lock";

    // 用户推荐
    String RECOMMEND_KEY = "pair:preCache:recommend:";
}

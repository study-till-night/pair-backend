package com.shuking.pairBackend.constant.redisKey;

public interface TeamRedisKey {
    // 加入队伍分布式锁
    String JOIN_LOCK_KEY = "pair:join_team_lock:teamId:";

    // 退出队伍分布式锁
    String QUIT_LOCK_KEY = "pair:quit_team_lock:teamId:";

    // 热点队伍列表
    String INDEX_LIST_KEY="pair:index_team_list";
}

package com.shuking.pairBackend;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.shuking.pairBackend.mapper")
@EnableTransactionManagement
@EnableRedisHttpSession //  分布式session
@EnableKnife4j
@EnableScheduling   //  定时任务
public class PairProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PairProjectApplication.class, args);
    }

}

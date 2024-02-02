package com.shuking.pairBackend.service;

import com.shuking.pairBackend.PairProjectApplication;
import com.shuking.pairBackend.domain.vo.UserVo;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@Log4j2
@SpringBootTest(classes = PairProjectApplication.class)
class AccountServiceTest {

    @Resource
    private AccountService accountService;

    @Test
    void testSearchUsersByTags() {
        List<UserVo> accounts = accountService.searchUsersByTags(Arrays.asList("java", "c++"));
        log.info(accounts);
        Assertions.assertTrue(accounts.size() != 0,"error");
//        Assert.notNull(accounts,"error");
    }
}
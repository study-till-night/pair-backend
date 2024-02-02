package com.shuking.pairBackend.mapper;

import com.shuking.pairBackend.domain.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
* @author HP
* @description 针对表【account(账户表)】的数据库操作Mapper
* @createDate 2023-10-31 19:38:48
* @Entity com.shuking.pair_project.domain.Account
*/
@Repository
public interface AccountMapper extends BaseMapper<Account> {

}





package com.shuking.pairBackend.mapper;

import com.shuking.pairBackend.domain.Team;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
* @author HP
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2023-11-13 09:55:30
* @Entity com.shuking.pairBackend.domain.Team
*/
@Repository
public interface TeamMapper extends BaseMapper<Team> {

}





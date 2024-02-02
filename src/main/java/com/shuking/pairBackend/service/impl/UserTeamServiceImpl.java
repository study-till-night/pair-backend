package com.shuking.pairBackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuking.pairBackend.domain.UserTeam;
import com.shuking.pairBackend.service.UserTeamService;
import com.shuking.pairBackend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author HP
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-11-13 09:55:30
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}





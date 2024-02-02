package com.shuking.pairBackend.mapper;

import com.shuking.pairBackend.domain.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
* @author HP
* @description 针对表【tag(标签表)】的数据库操作Mapper
* @createDate 2023-10-31 19:38:48
* @Entity com.shuking.pair_project.domain.Tag
*/
@Repository
public interface TagMapper extends BaseMapper<Tag> {

}





package com.shuking.pairBackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuking.pairBackend.domain.Tag;
import com.shuking.pairBackend.service.TagService;
import com.shuking.pairBackend.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author HP
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2023-10-31 19:38:48
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}





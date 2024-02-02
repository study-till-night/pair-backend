package com.shuking.pairBackend.controller;

import com.shuking.pairBackend.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag")
public class TagController {
    @Resource
    private TagService tagService;


}

package com.example.esapi.controller;

import com.example.esapi.service.ContentService;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author 吴政杰
 */
@RestController
public class ContentController {
    @Autowired
    private ContentService contentService;

    @GetMapping("/parse/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword)throws Exception{
        return contentService.parseContent(keyword);
    }

    @GetMapping("/parse/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> parse(@PathVariable("keyword") String keyword,
                                          @PathVariable("pageNo") int pageNo,
                                          @PathVariable("pageSize") int pageSize)throws Exception{
        return contentService.searchRequest(keyword,pageNo,pageSize);
    }
}

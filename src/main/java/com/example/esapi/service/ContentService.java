package com.example.esapi.service;

import com.alibaba.fastjson.JSON;
import com.example.esapi.pojo.Content;
import com.example.esapi.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 吴政杰
 */
@Service
public class ContentService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public Boolean parseContent(String keyword) throws Exception{
        List<Content> list = new HtmlParseUtil().parseJD(keyword);
        BulkRequest bulkRequest = new BulkRequest();
        for (Content content : list) {
            bulkRequest.add(new IndexRequest("jd").source(JSON.toJSONString(content), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    public List<Map<String,Object>> searchRequest(String keyword,int pageNo,int pageSize) throws IOException {
        if (pageNo<=1){
            pageNo=1;
        }
        SearchRequest jd = new SearchRequest("jd");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.from(pageNo).size(pageSize);
        TermQueryBuilder title = QueryBuilders.termQuery("name", keyword);
        builder.query(title);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        //不需要多个字段高亮
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        builder.highlighter(highlightBuilder);

        jd.source(builder);
        SearchResponse search = restHighLevelClient.search(jd, RequestOptions.DEFAULT);
        List<Map<String,Object>> list=new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField name = highlightFields.get("name");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();//原来的结果
            //解析高亮的字段，将将原来的字段替换为高亮的字段
            if (name!=null){
                Text[] fragments = name.fragments();
                String new_name="";
                for (Text fragment : fragments) {
                    new_name+=fragment;
                }
                sourceAsMap.put("name",new_name);//高亮字段替换原来内容
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}

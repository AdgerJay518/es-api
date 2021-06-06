package com.example.esapi;

import com.alibaba.fastjson.JSON;
import com.example.esapi.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class EsApiApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //索引的创建
    @Test
    void testCreatIndex() throws IOException {
        //创建索引
        CreateIndexRequest index = new CreateIndexRequest("jd");
        //执行请求
        CreateIndexResponse create =
                restHighLevelClient.indices().create(index, RequestOptions.DEFAULT);
        System.out.println(create);
    }

    //获取索引
    @Test
    void testExistIndex() throws IOException {
        GetIndexRequest index = new GetIndexRequest("esapi_index");
        boolean exists =
                restHighLevelClient.indices().exists(index, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    //删除索引
    @Test
    void testDeleteIndex() throws IOException{
        DeleteIndexRequest delete = new DeleteIndexRequest("esapi_index");
        AcknowledgedResponse delete1 = restHighLevelClient.indices().delete(delete, RequestOptions.DEFAULT);
        System.out.println(delete1);
    }

    //添加文档
    @Test
    void testAddDocument() throws IOException {
        User user = new User("wzj", 22);
        IndexRequest request = new IndexRequest("esapi_index");
        //定义规则
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        request.timeout("1s");
        //将我们的数据放入json请求
        request.source(JSON.toJSONString(user), XContentType.JSON);
        //客户端发送请求，获取响应的结果
        IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(index.status().toString());
        System.out.println(index);
    }

    //获取文档,判断是否存在
    @Test
    void testDocumentIsExist() throws IOException{
        GetRequest index = new GetRequest("esapi_index","1");
        boolean exists = restHighLevelClient.exists(index, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    //获取文档信息
    @Test
    void testGetDocument() throws IOException{
        GetRequest index = new GetRequest("esapi_index","1");
        GetResponse documentFields = restHighLevelClient.get(index, RequestOptions.DEFAULT);
        System.out.println(documentFields);//文档的内容
        System.out.println(documentFields.getSourceAsString());//全部内容
    }

    //更新文档
    @Test
    void testUpdateDocument() throws IOException{
        UpdateRequest request = new UpdateRequest("esapi_index", "1");
        User user = new User("jonyon", 18);
        request.doc(JSON.toJSONString(user),XContentType.JSON);
        UpdateResponse update = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(update);
        System.out.println(update.status());
    }

    //删除文档记录
    @Test
    void testDeleteDocument() throws IOException{
        DeleteRequest request = new DeleteRequest("esapi_index", "1");
        DeleteResponse delete = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }

    //批量插入数据
    @Test
    void testBulkRequest() throws IOException{
        BulkRequest bulkRequest = new BulkRequest();
        List<User> list=new ArrayList<>();
        list.add(new User("wzj",25));
        list.add(new User("阿杰",26));
        list.add(new User("Adger",23));
        //批处理请求
        for (int i=0;i<list.size();i++){
            //批量更新和批量删除就在这里更改对应的请求即可
            bulkRequest.add(
                            new IndexRequest("esapi_index")
                                    .id(""+(i+1))//如果不用id会生成随机id
                                    .source(JSON.toJSONString(list.get(i)),XContentType.JSON)
                    );
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulk.hasFailures());
    }

    //查询
    @Test
    void testSearch() throws IOException{
        SearchRequest searchRequest = new SearchRequest("esapi_index");
        //构建搜索的条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //查询条件可以使用QueryBuilders工具来实现
        //termQuery是精确匹配;matchAllQuery()是匹配所有
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "wzj");
        builder.query(termQueryBuilder);
        //构建分页
        builder.from(0).size(3);
        searchRequest.source(builder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(search.getHits()));
        System.out.println("-------------------------------");
        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
    }
}

package com.example.esapi.utils;

import com.example.esapi.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 吴政杰
 */
@Component
public class HtmlParseUtil {
//    public static void main(String[] args) throws IOException {
//        new HtmlParseUtil().parseJD("java").forEach(System.out::println);
//    }
    public List<Content> parseJD(String keyword) throws IOException {
        String url="https://search.jd.com/Search?keyword="+keyword;
        //Jsoup返回的Document就是浏览器Document对象
        Document document = Jsoup.parse(new URL(url), 20000);
        //所有在js中可以使用的方法这里都能用
        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");
        List<Content> list=new ArrayList<>();
        for (Element li:elements){
            String img = li.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = li.getElementsByClass("p-price").eq(0).text();
            String name = li.getElementsByClass("p-name").eq(0).text();
            Content content = new Content();
            content.setImg(img);
            content.setName(name);
            content.setPrice(price);
            list.add(content);
        }
        return list;
    }
}

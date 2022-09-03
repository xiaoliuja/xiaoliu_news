package com.xiaoliu.news.Service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaoliu.news.Dao.InformationDao;
import com.xiaoliu.news.Model.Information;
import com.xiaoliu.news.Service.InfromationService;
import com.xiaoliu.news.Utils.http.HttpClientUtil;
import com.xiaoliu.news.Utils.http.HttpResult;
import com.xiaoliu.news.Utils.reids.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.*;

@Service
public class InformationServiceImpl implements InfromationService {

    private static final String URL = "https://news.baidu.com/widget?id=LocalNews&ajax=json";

    private static final Logger LOGGER = LoggerFactory.getLogger(InformationServiceImpl.class);

    @Autowired
    private InformationDao informationDao;

    @Autowired
    private HttpClientUtil httpClientUtil;


    @Autowired
    private RedisUtil redisUtil;



    @Override
    public void insertNews() {
        LOGGER.info(" .........爬取内容开始");
        try {
            //请求参数拿到结果
            HttpResult httpResult = httpClientUtil.doPost(URL);
            //判断是否成功拿到
            if(httpResult.getCode() == 200){
                //将结果转换为json对象
                JSONObject object = JSON.parseObject(httpResult.getBody());
                //拿到具体的内容为 jsonArray
                JSONArray jsonArray = object.getJSONObject("data").getJSONObject("LocalNews")
                        .getJSONObject("data").getJSONObject("rows").
                        getJSONArray("first");
                //遍历json数组中每个位置存储的对象
                for (int i = 0; i < jsonArray.size(); i++) {
                    //依次循环拿到json数组每个位置的对象
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    //将拿到的对象的值赋值给一个新的information对象，再将值插入到数据库当中去
                    Information information = new Information();
                    //新闻来源  我们这里爬取的是百度的新闻 赋值为 "baidu"
                    information.setInfoSrc("baidu");

                    //爬取内容的信息赋值
                    information.setInfoText(jsonObject.getString("title"));
                    information.setInfoImage(jsonObject.getString("imgUrl"));
                    information.setInfoUrl(jsonObject.getString("url"));
                    information.setTime(jsonObject.getString("time"));

                    //将title加密 用来判断数据库中的信息是否会重复 因为每个加密的都是不一样的加密信息
                    String a = DigestUtils.md5DigestAsHex(jsonObject.getString("title").getBytes());
                    information.setSingleTag(a);

                    //设置创建时间、更新时间
                    information.setCreateTime(new Date());
                    information.setUpdateTime(new Date());

                    //设置 是否哦可用 是否删除   在写sql中每个表都应有这两个字段 习惯
                    information.setIsEnabled(1);
                    information.setIsDeleted(0);

                    Information i1 = new Information();
                    i1.setSingleTag(a);
                    if (informationDao.count(i1) == 0l ){
                          informationDao.insert(information);
                    }
                }

            }

        } catch (Exception e) {
            LOGGER.error("出现异常：{}", e);
        }

        LOGGER.info(" ...... 爬取内容结束  .........");

    }

    @Override
    public List<Information> findByWord(String word) {
        return informationDao.findByWord(word);
    }


    //将查询的关键字存储到redis里面去     使用 zincrby
    @Override
    public Double record(String word) {
            return redisUtil.zincrby("NEWS_SEARCH", word, 1);
    }


    //热度排行榜逻辑  使用redis里的 zrevrange     用List<Map<String,Object>>去接受  ，将每个map对象（一个关键字的信息在redis的sort-set结构里面存储这 value和score两个参数）存储在list集合里面 将list传给前端页面遍历出来数据
    @Override
    public List<Map<String, Object>> findRank() {
        Set<ZSetOperations.TypedTuple<Object>> search = redisUtil.zrevrangeByScoreWithScores("NEWS_SEARCH", 0, 1000D);
        List<Map<String,Object>> list  = new ArrayList<>();
        /**
         *        因为redis内部会将关键字以关键字搜索的次数降序排列，所以进行遍历的时候拿到的关键字是降序排列的，因为我们只需定义一个变量
         *        rank,当每遍历一次就把它加1，第一次遍历就是第一名，把rank加1，正好为1，加到该数据的map里面，这样前端遍历的时候就可以拿到该排名rank。
         */

        int rank = 0;
        for (ZSetOperations.TypedTuple<Object> objectTypedTuple : search) {
            //拿到redis里面存储的 value和score
            String value = (String) objectTypedTuple.getValue();
            int score = objectTypedTuple.getScore().intValue();
            rank += 1;
            //以键值对的形式存储到 map中去
            Map<String,Object> map = new HashMap<>();
            map.put("value",value);
            map.put("score", score);
            map.put("rank", rank);
            //将每个map对象也就为一条信息 存储到 list里面
            list.add(map);
        }
        return list;
    }
}

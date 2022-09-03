package com.xiaoliu.news.Service;

import com.xiaoliu.news.Model.Information;

import java.util.List;
import java.util.Map;

public interface InfromationService {


    void   insertNews();


    List<Information> findByWord(String word);


    Double record(String word);

    List<Map<String,Object>> findRank();
}

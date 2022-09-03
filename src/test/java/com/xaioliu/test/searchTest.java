package com.xaioliu.test;

import com.xiaoliu.news.Dao.InformationDao;
import com.xiaoliu.news.MainApplication;
import com.xiaoliu.news.Model.Information;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MainApplication.class)
public class searchTest {

    @Autowired
    private InformationDao informationDao;


    @Test
    public void count(){
        Information information = new Information();
        information.setId(10l);
        long count = informationDao.count(information);
        System.out.println(count);
    }



}

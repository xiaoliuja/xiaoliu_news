package com.xiaoliu.news.Job;


import com.xiaoliu.news.Service.InfromationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



@Component
public class Timer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Timer.class);


    @Autowired
    private InfromationService infromationService;


    @Scheduled(cron = "0 1/5 * * * ?")
    public void run(){

        LOGGER.info("=== 开始爬取定任务 ===");
        infromationService.insertNews();
        LOGGER.info(" ==== 结束爬取任务 ====");

    }

}

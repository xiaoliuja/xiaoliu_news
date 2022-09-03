package com.xiaoliu.news.Controller;

import com.xiaoliu.news.Model.Information;
import com.xiaoliu.news.Service.InfromationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.lang.model.element.NestingKind;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/page")
public class InformationController {

    @Autowired
    private InfromationService searchService;

    @RequestMapping("/searchPage")
    public String searchPage(){
        return "search";
    }



    //根据输入框中的信息模糊查询出对应的news信息
    @RequestMapping("/search")
    public String searchByWord(String infoText, Model model){
        if (StringUtils.isNotEmpty(infoText)) {
            //将查询关键字的信息写入redis里面 存储在sort-set结构体里面
            searchService.record(infoText);
            //查询信息
            List<Information> list = searchService.findByWord(infoText);
            model.addAttribute("list", list);
            model.addAttribute("infoText", infoText);
            return "search";
        }

            model.addAttribute("message", "查询的关键字为空请重新输入");
            return "search";

    }



    //关键字热度排行榜
    @RequestMapping("/rank")
    public String wordRank(Model model){
        //得到reids内部存储的关键字的信息
        List<Map<String, Object>> rankList = searchService.findRank();
        //传给前端   前端在遍历这个list的时候会去依次遍历每个map对象里面的键值对，根据 c.key 把该对象里面的value取出来。
        model.addAttribute("list", rankList);

        return "rank";
    }

}

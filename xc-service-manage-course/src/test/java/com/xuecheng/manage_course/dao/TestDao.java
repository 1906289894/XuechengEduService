package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.CourseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDao {
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CourseService courseService;

    @Autowired
    GridFsTemplate gridFsTemplate;



    @Test
    public void testCourseBaseRepository(){
        Optional<CourseBase> optional = courseBaseRepository.findById("402885816240d276016240f7e5000002");
        if(optional.isPresent()){
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }

    }


    @Test
    public void testcourse(){
        CourseMarket courseMarket = new CourseMarket();
        courseMarket.setPrice_old(100f);
        courseMarket.setPrice(200f);
        courseMarket.setEndTime(new Date());
        courseMarket.setStartTime(new Date());
        courseMarket.setQq("1906289894");
        courseMarket.setValid("204001");
        courseMarket.setCharge("200400");
        ResponseResult responseResult = courseService.updateCourseMarket("402885816240d276016241019be70004", courseMarket);
        System.out.println(responseResult);
    }


}

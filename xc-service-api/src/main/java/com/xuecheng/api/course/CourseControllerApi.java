package com.xuecheng.api.course;

import com.xuecheng.framework.domain.cms.ext.CourseView;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.web.bind.annotation.PathVariable;


public interface CourseControllerApi {

    //课程计划查询
    public TeachplanNode findTeachplanList(String courseId);

    //添加课程计划
    public ResponseResult addTeachplan(Teachplan teachplan);

    //查询课程列表
    public QueryResponseResult<CourseInfo> findCourseList(int page, int size, CourseListRequest courseListRequest);

    //添加课程
    public ResponseResult addCourse(CourseBase courseBase);

    //获取课程基础信息
    public CourseBase getCourseBaseById(String courseId) throws RuntimeException;

    //修改课程信息
    public ResponseResult updateCourseBase(String id,CourseBase courseBase);

    //获取课程营销信息
    public CourseMarket getCourseMarketById(String courseId);

    //更新课程营销信息
    public ResponseResult updataCourseMarket(String id,CourseMarket courseMarket);

    //添加课程图片
    public ResponseResult addCoursePic(String courseId,String pic);

    //获取课程寄出信息
    public CoursePic findCoursePic(String courseId);

    //删除课程图片
    public ResponseResult deleteCoursePic(String courseId);

    //课程视图查询
    public CourseView courseview(String id);

    //课程预览
    public CoursePublishResult preview(String id);

    //课程发布
    public CoursePublishResult publish(@PathVariable String id);

    //保存媒体信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia);

    //我的课程
    public QueryResponseResult<CourseInfo> mfindCourseList(int page, int size, CourseListRequest courseListRequest);
}

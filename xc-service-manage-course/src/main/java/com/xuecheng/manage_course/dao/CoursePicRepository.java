package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CoursePicRepository extends JpaRepository<CoursePic,String> {
    long deleteByCourseid(String courseid);
}

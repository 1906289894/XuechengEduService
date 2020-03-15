package com.xuecheng.manage_cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)

public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Test
    public void teatFindPage(){
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase",
                ExampleMatcher.GenericPropertyMatchers.contains());
//页面别名模糊查询，需要自定义字符串的匹配器实现模糊查询
//ExampleMatcher.GenericPropertyMatchers.contains() 包含
//ExampleMatcher.GenericPropertyMatchers.startsWith()//开头匹配
//条件值
        CmsPage cmsPage = new CmsPage();
cmsPage.setPageAliase("课程");
// cmsPage.setPageAliase("分类导航");
//创建条件实例
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        Pageable pageable = new PageRequest(0, 10);
        Page<CmsPage> all = cmsPageRepository.findAll( example,pageable);
        System.out.println(all);
    }
}


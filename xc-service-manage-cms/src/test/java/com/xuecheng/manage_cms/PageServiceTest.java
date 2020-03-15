package com.xuecheng.manage_cms;

import com.xuecheng.manage_cms.service.PageService;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PageServiceTest {
    @Autowired
    PageService pageService;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Test
    public void testGetPageHtml(){
        
        String pageHtml = pageService.getPageHtml("5e4e1c02ac5f5676b8c3747e");
        System.out.println(pageHtml);
    }

    //保存模板
    @Test
    public void testGridFs() throws FileNotFoundException {
    //要存储的文件
        File file = new File("d:/index_banner.html");
    //定义输入流
        FileInputStream inputStram = new FileInputStream(file);
        //向GridFS存储文件
        ObjectId objectId  = gridFsTemplate.store(inputStram, "轮播图测试文件01", "");
        //得到文件ID
        String fileId = objectId.toString();
        System.out.println(file);
    }

    @Test
    public void testStore2() throws FileNotFoundException{

        File file = new File("c:\\course.ftl");
        FileInputStream inputStream = new FileInputStream(file);
        //保存模板文件内容
        ObjectId gridFsFile = gridFsTemplate.store(inputStream, "课程详情模板文件", "");
        //得到文件ID
        String fileId = gridFsFile.toString();
        System.out.println(fileId);

    }
}

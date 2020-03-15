package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    @Test
    public void testUpload(){
        try {
            ClientGlobal.initByProperties("config/fc.properties");

            TrackerClient tc = new TrackerClient();

            TrackerServer ts = tc.getConnection();

            if (ts == null) {
                System.out.println("null");
                return;
            }

            StorageServer ss = tc.getStoreStorage(ts);
            if (ss == null) {
                System.out.println("null");
            }

            StorageClient1 storageClient1 = new StorageClient1(ts, ss);
            NameValuePair[] meta_list = null; //new NameValuePair[0];
            String item = "D:\\b.jpg";
            String fileid;
            fileid = storageClient1.upload_file1(item, "jpg", meta_list);
            System.out.println("Upload local file " + item + " ok, fileid=" + fileid);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }



}

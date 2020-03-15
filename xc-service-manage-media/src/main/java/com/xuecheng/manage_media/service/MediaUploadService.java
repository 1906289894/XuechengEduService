package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //上传文件根目录
    @Value("${xc-service-manage-media.upload-location}")
    String uploadPath;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    /**
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    //得到文件路径
    private String getFilePath(String fileMd5,String fileExt){
        String filePath = uploadPath+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+
                "/"+fileMd5+"/"+fileMd5+"."+fileExt;

        return filePath;
    }


    //得到文件所在目录路径
    private String getFileFolderPath(String fileMd5){
        String fileFolderPath = uploadPath+fileMd5.substring(0,1)+"/"
                +fileMd5.substring(1,2)+"/"+fileMd5+"/";
        return fileFolderPath;
    }

    //得到块文件所在目录
    private String getChunkFileFolderPath(String fileMd5){
        String fileChunkFolderPath = getFileFolderPath(fileMd5)+"chunk/";
        return fileChunkFolderPath;
    }

    //创建文件目录
    private boolean createFileFold(String fileMd5){
        //创建上传文件目录
        String fileFolderPath = getFileFolderPath(fileMd5);
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            //创建文件夹
            boolean mkdirs = fileFolder.mkdirs();
            return mkdirs;
        }
        return true;
    }

    //文件上传前的注册，检查文件是否存在
    public ResponseResult register(String fileMd5,String fileName,Long fileSize
                                    ,String mimetype,String fileExt){
        //文件所在目录
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //文件路径
        String filePath = getFilePath(fileMd5,fileExt);
        File file = new File(filePath);

        //查询数据库文件是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        //文件存在
        if (file.exists()&&optional.isPresent()) {
            //文件存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }

        //文件不存在时做些准备工作，检查文件所在目录是否存在，如果不存在则创建
        boolean fileFold = createFileFold(fileMd5);
        if (!fileFold) {
            //上传文件目录创建失败
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }


    //检查块文件
    public CheckChunkResult checkchunk(String fileMd5,Integer chunk,Integer chunkSize){
        //得到块文件所在路径
        String chunkfileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //块文件的文件名称以1,2,3..序号命名，没有扩展名
        File chunkFile = new File(chunkfileFolderPath+chunk);
        if (chunkFile.exists()) {
            //块文件存在
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,true);
        }else {
            //块文件不存在
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_FAIL,false);
        }
    }

    //块文件上传
    public ResponseResult uploadchunk(MultipartFile file,String fileMd5,Integer chunk){
        //检查分块目录，如果不存在则要自动创建
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //得到块文件存放路径
        String chunkFulePath = chunkFileFolderPath+chunk;

        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        if (file == null) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_ISNULL);
        }


        //上传的块文件
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(chunkFulePath));
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            ExceptionCast.cast(MediaCode.CHUNK_FILE_UPLOAD_FAIL);
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //创建块目录文件
    private boolean createChunkFileFolder(String fileMd5){
        //创建上传文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            //创建文件夹
            boolean mkdirs = chunkFileFolder.mkdirs();
            return mkdirs;
        }
        return true;
    }

    //合并块文件
    public ResponseResult mergechunks(String fileMd5,String fileName,Long fileSize,
                                      String mimetype,String fileExt){
        //创建块文件的路径
        String chunkfileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkfileFolder = new File(chunkfileFolderPath);

        //获取块文件
        List<File> chunkFiles = getChunkFiles(chunkfileFolder);

        //创建一个合并文件
        File mergeFile = new File(getFilePath(fileMd5,fileExt));

        //合并文件
        mergeFile = this.mergeFile(chunkFiles,mergeFile);
        if (mergeFile == null){
            //合并失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        //校验文件的MD5值是否和前端传入的md5一样
        boolean checkFileMd5 = this.checkFileMd5(mergeFile,fileMd5);
        if (!checkFileMd5) {
            //校验失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        //将文件的信息写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        //文件路径保存相对路径
        String filePath1 = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);

        //向MQ发送消息
        sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //检验文件
    private boolean checkFileMd5(File mergeFile, String fileMd5) {
        try{

            //创建文件输入流
            FileInputStream fileInputStream = new FileInputStream(mergeFile);

            //得到文件的md5
            String md5Hex = DigestUtils.md5Hex(fileInputStream);

            //和传入的md5比较
            if (fileMd5.equalsIgnoreCase(md5Hex)){
                return true;
            }

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

    //合并文件
    private File mergeFile(List<File> chunkFileList, File mergeFile){



            try {
                //如果合并文件存在则删除
                if (mergeFile.exists()) {
                    mergeFile.delete();
                }else {
                    //创建一个新文件
                    mergeFile.createNewFile();
                }

                //创建一个写对象
                RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
                byte[] b = new byte[1024];
                for(File chunkFile:chunkFileList){
                    RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                    int len = -1;
                    while ((len = raf_read.read(b))!=-1){
                        raf_write.write(b,0,len);
                    }
                    raf_read.close();
                }
                raf_write.close();
                return mergeFile;

            } catch (IOException e) {
                e.printStackTrace();
            }
        return null;
    }
    //获取所有块文件
    private List<File> getChunkFiles(File chunkfileFolder){
        //获取路径下的所有块文件
        File[] chunkFiles = chunkfileFolder.listFiles();
        //将文件数组转成list,并排序
        List<File> chunkFileList = new ArrayList<File>();
        chunkFileList.addAll(Arrays.asList(chunkFiles));

        //排序
        Collections.sort(chunkFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }

        });
        return chunkFileList;
    }

    //向mq发送视频处理消息
    public ResponseResult sendProcessVideoMsg(String mediaId){
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        MediaFile mediaFile = optional.get();
        String fileId = mediaFile.getFileId();
        //发送视频处理
        Map<String,String> msgMap = new HashMap<>();
        msgMap.put("mediaId",fileId);
        //发送消息
        String jsonString = JSON.toJSONString(msgMap);
        try {//EX_MEDIA_PROCRSSTASK
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonString);

        }catch (AmqpException e){
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }
}

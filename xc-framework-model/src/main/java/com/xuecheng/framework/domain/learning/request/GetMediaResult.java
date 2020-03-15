package com.xuecheng.framework.domain.learning.request;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;

public class GetMediaResult extends ResponseResult {

    //媒资管理播放地址
    private String fileUrl;
    public GetMediaResult(ResultCode resultCode ,String fileUrl){
        super(resultCode);
        this.fileUrl  = fileUrl;
    }
}

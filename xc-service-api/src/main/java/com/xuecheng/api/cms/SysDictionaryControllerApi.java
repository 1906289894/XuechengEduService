package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.system.SysDictionary;

public interface SysDictionaryControllerApi {
    //数据字典
    public SysDictionary getByType(String type);
}

package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysdictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysDictionaryService {
    @Autowired
    SysdictionaryRepository sysdictionaryRepository;

    public SysDictionary findDictByType(String type){
        return sysdictionaryRepository.findByDType(type);
    }
}

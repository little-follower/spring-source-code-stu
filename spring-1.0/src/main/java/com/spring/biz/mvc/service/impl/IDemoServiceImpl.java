package com.spring.biz.mvc.service.impl;

import com.spring.biz.mvc.service.IDemoService;
import com.spring.mvcframework.annotation.MyService;

/**
 * @author: chengqing Zhang
 * @description: 实现IDemoServiceImpl，重写接口方法
 * @date:Create：in 2022/9/20 11:06
 */
@MyService
public class IDemoServiceImpl implements IDemoService {
    @Override
    public String get(String name) {
        return "my name is "+name;
    }
}

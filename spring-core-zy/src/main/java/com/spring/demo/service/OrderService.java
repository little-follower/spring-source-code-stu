package com.spring.demo.service;

import com.spring.framework.annotion.Component;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/9/26 13:57
 * @modified By：
 */
@Component("orderService")
public class OrderService {

    public void myOrderService() {
        System.out.println("my order service~");
    }
}

package com.spring.demo.service;

import com.spring.framework.annotion.*;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/9/23 16:31
 */
@Component("userService")
@Scope
public class UserService {

    @Autowired
    private OrderService orderService;



    public void test () {
        orderService.myOrderService();
        System.out.println("我是userService test 方法~");
    }



}

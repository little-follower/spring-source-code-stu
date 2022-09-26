package com.spring.demo;

import com.spring.demo.service.UserService;
import com.spring.framework.CustomApplicationContext;
import com.spring.demo.config.AppConfig;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/9/23 16:37
 * @modified By：
 */
public class ApplicationContextTest {
    public static void main(String[] args) throws ClassNotFoundException {
        CustomApplicationContext applicationContext = new CustomApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        System.out.println(userService);
        userService.test();
    }
}

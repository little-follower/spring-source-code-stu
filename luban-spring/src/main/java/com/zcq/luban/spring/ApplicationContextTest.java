package com.zcq.luban.spring;

import com.zcq.luban.spring.config.AppConfig;
import com.zcq.luban.spring.serivce.UserService;
import com.zcq.luban.spring.serivce.dto.User;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/11/4 9:35
 * @modified By：
 */
public class ApplicationContextTest {
    public static void main(String[] args) {

        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
        User user = (User) applicationContext.getBean("user");
        System.out.println(user);
        System.out.println(userService);
        System.out.println(userService);
        System.out.println(userService);
        userService.tes();

    }
}

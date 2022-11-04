package com.zcq.luban.spring.serivce;

import com.zcq.luban.spring.annotion.Autowired;
import com.zcq.luban.spring.annotion.Component;
import com.zcq.luban.spring.serivce.dto.User;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/11/3 17:35
 * @modified By：
 */
@Component("userService")
public class UserService {

    @Autowired
    private User user;

    public void tes() {
        System.out.println("下面打印应一下user：");
        System.out.println(user);
    }


}

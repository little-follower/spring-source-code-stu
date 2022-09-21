package com.spring.biz.mvc.action;

import com.spring.biz.mvc.service.IDemoService;
import com.spring.mvcframework.annotation.MyAutowired;
import com.spring.mvcframework.annotation.MyController;
import com.spring.mvcframework.annotation.MyRequestMapping;
import com.spring.mvcframework.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/9/20 11:04
 * @modified By：
 */
@MyController
@MyRequestMapping("/demo")
public class DemoAction {
    @MyAutowired
    private IDemoService iDemoService;

    @MyRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response, @MyRequestParam("name") String name) {
        String result = iDemoService.get(name);
        try{
            response.getWriter().write(result);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}

package com.spring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author: chengqing Zhang
 * @description: 自定义 获取参数注解
 * @date:Create：in 2022/9/20 10:53
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {
    String value();
}

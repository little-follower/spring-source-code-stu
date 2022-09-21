package com.spring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author: chengqing Zhang
 * @description: 自定义mapper url映射注解
 * @date:Create：in 2022/9/20 10:52
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
    String value();
}

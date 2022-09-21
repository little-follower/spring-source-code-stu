package com.spring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author: chengqing Zhang
 * @description: 自定义注入bean注解
 * @date:Create：in 2022/9/20 10:49
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {
    String value() default "";
}

package com.spring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author: chengqing Zhang
 * @description: 自定义service注解
 * @date:Create：in 2022/9/20 10:51
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyService {
    String value() default "";
}

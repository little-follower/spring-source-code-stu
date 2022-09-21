package com.spring.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author: chengqing Zhang
 * @description: 自定义 mvc controller
 * @date:Create：in 2022/9/20 10:48
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface MyController {
    String value() default "";
}

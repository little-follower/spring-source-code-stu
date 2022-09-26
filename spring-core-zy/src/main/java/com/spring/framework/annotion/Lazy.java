package com.spring.framework.annotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/9/23 17:34
 * @modified By：
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Lazy {
}

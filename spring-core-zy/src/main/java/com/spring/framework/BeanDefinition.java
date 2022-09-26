package com.spring.framework;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/9/23 17:40
 */
public class BeanDefinition {

    private Boolean lazy;
    private String scope;
    private Class beanClass;


    public Boolean getLazy() {
        return lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }
}

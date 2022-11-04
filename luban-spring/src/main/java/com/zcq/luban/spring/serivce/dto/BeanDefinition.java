package com.zcq.luban.spring.serivce.dto;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/11/4 10:55
 * @modified By：
 */
public class BeanDefinition {
    private Class clazz;
    private String scope;
    private boolean lazy;
    private String beanName;

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}

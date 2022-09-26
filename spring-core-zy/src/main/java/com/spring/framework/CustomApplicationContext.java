package com.spring.framework;

import com.spring.framework.annotion.*;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/9/23 16:32
 * @modified By：
 */
public class CustomApplicationContext {
    private Class config;

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();


    public CustomApplicationContext(Class config) throws ClassNotFoundException {
        this.config = config;
        // spring 的启动
        // 1. 解析configClass得到包路径
        // 获取配置文件中的扫描的bean的包路径
        ComponentScan componentScan = (ComponentScan) config.getAnnotation(ComponentScan.class);
        String packageScan = componentScan.value();//com.spring.demo.service


        //扫描类->解析类上的注解--->BeanDefinition->beanDefinitionMap
        ClassLoader classLoader = this.getClass().getClassLoader();
        packageScan = packageScan.replace(".", "/"); // com/spring/demo
        URL resource = classLoader.getResource(packageScan);
        File file = new File(resource.getFile());//目录
        for (File f : file.listFiles()) {
            String absolutePath = f.getAbsolutePath();
            String fullPath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class")).replace("\\", ".");
            Class clazz = classLoader.loadClass(fullPath);
            boolean annotationPresent = clazz.isAnnotationPresent(Component.class);
            if (annotationPresent) {
                Component component = (Component) clazz.getAnnotation(Component.class);
                String beanName = component.value();
                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setBeanClass(clazz);
                if (clazz.isAnnotationPresent(Lazy.class)) {
                    beanDefinition.setLazy(true);
                } else {
                    beanDefinition.setLazy(false);
                }
                if (clazz.isAnnotationPresent(Scope.class)) {
                    Scope scope = (Scope) clazz.getAnnotation(Scope.class);
                    String scopeValue = scope.value();
                    if ("".equals(scopeValue)) {
                        beanDefinition.setScope("singleton");
                    } else {
                        beanDefinition.setScope(scopeValue);
                    }
                }
                beanDefinitionMap.put(beanName, beanDefinition);
            }
        }

        // 生成非懒加载的单例bean（对象）：bean的生命周期（实例化----填充属性--->?---->单例池）
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            String scope = beanDefinition.getScope();
            //Boolean lazy = beanDefinition.getLazy();
            if ("singleton".equals(scope)) {
                Object object = createBean(beanDefinition);
                singletonObjects.put(beanName, object);
            }
        }

    }

    /**
     * 实例化bean
     *
     * @param beanDefinition
     * @return
     */
    private Object createBean(BeanDefinition beanDefinition) {
        Object object = null;
        Class beanClass = beanDefinition.getBeanClass();
        try {
            object = beanClass.getDeclaredConstructor().newInstance();
            //实现@autowired属性注入功能
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    String fileBeanName =  field.getName();
                    Object bean = getBean(fileBeanName);
                    field.setAccessible(true);
                    field.set(object,bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    public Object getBean(String beanName) {
        //这边先冲beanDefinitionMap判断bean
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        String scope = beanDefinition.getScope();
        if ("singleton".equals(scope)) {
            Object bean = singletonObjects.get(beanName);
            //这个是为了防止属性注入时的类还没来得及初始化，然后就创建一下
            if(null == bean) {
             bean =    createBean(beanDefinition);
            }
            return bean;
        } else {
            return createBean(beanDefinition);
        }
    }
}

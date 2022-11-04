package com.zcq.luban.spring;

import com.zcq.luban.spring.annotion.*;
import com.zcq.luban.spring.serivce.dto.BeanDefinition;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: chengqing Zhang
 * @description: 实现对的applicationContext 创建bean、获取bean的功能
 * @date:Create：in 2022/11/3 17:34
 */
public class ApplicationContext {

    private Class appConfig;
    private List<String> filePathList = new ArrayList<>();
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private Map<String, Object> singletonObejcts = new ConcurrentHashMap<>();

    public ApplicationContext(Class appConfig) {
        this.appConfig = appConfig;
        // 扫描所有的class文件
        List<Class> classList = doScan(appConfig);
        // 将class文件转为bean定义
        setBeanDefinition(classList);
        Set<String> beanNames = beanDefinitionMap.keySet();
        // 根据bean定义实现对单例对象实现实例化并存入单例池中
        beanNames.forEach(beanName -> {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            doCreateBean(beanDefinition);
        });
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        if (!beanDefinition.isLazy() && (null == beanDefinition.getScope() || beanDefinition.getScope().equals("singleton"))) {
            Class clazz = beanDefinition.getClazz();
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                singletonObejcts.put(beanDefinition.getBeanName(), instance);
                Field[] declaredFields = clazz.getDeclaredFields();
                List<Field> fieldList = Arrays.stream(declaredFields).filter(f -> f.isAnnotationPresent(Autowired.class)).collect(Collectors.toList());
                fieldList.forEach(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(instance, getBean(field.getName()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                return instance;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void setBeanDefinition(List<Class> classes) {
        classes.forEach(clazz -> {
            boolean annotationPresent = clazz.isAnnotationPresent(Component.class);
            if (annotationPresent) {
                BeanDefinition beanDefinition = new BeanDefinition();
                Component component = (Component) clazz.getAnnotation(Component.class);
                beanDefinition.setClazz(clazz);
                beanDefinition.setBeanName(component.value());
                boolean isScope = clazz.isAnnotationPresent(Scope.class);
                boolean isLazy = clazz.isAnnotationPresent(Lazy.class);
                if (isScope) {
                    Scope scope = (Scope) clazz.getAnnotation(Scope.class);
                    beanDefinition.setScope(scope.value());
                }
                if (isLazy) {
                    beanDefinition.setLazy(true);
                }
                beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
            }
        });
    }

    private List<Class> doScan(Class appConfig) {
        List<Class> scanAllClass = new ArrayList<>();
        ComponentScan componentScan = (ComponentScan) appConfig.getAnnotation(ComponentScan.class);
        String path = componentScan.value();
        path = path.replace(".", "/");
        //抛出一个疑问什么是ClassLoad 说是类加载器
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource(path);
        String currentFilePath = resource.getFile();
        //System.out.println("打印当前classes路径下的文件的路径"+currentFilePath);
        File file = new File(currentFilePath);
        afterClassPackName(file);
        //System.out.println("=----=" + classPackageNameList);
        for (String classPackage : filePathList) {
            try {
                Class clazz = Class.forName(classPackage);
                scanAllClass.add(clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return scanAllClass;
    }

    public Object getBean(String beanName) {
        Object o = singletonObejcts.get(beanName);
        if (null == o) {
            doCreateBean(beanDefinitionMap.get(beanName));
        }
        return singletonObejcts.get(beanName);
    }

    private void afterClassPackName(File file) {
        File[] files = file.listFiles();
        if (null != files && files.length > 0) {
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    afterClassPackName(file1);
                } else {
                    // System.out.println("=====>" + file1.getAbsolutePath());
                    String absolutePath = file1.getAbsolutePath();
                    absolutePath = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                    absolutePath = absolutePath.replace("\\", ".");
                    filePathList.add(absolutePath);
                }
            }
        }
    }
}

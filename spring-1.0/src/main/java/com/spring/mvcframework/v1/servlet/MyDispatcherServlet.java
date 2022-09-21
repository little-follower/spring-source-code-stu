package com.spring.mvcframework.v1.servlet;

import com.spring.mvcframework.annotation.MyAutowired;
import com.spring.mvcframework.annotation.MyController;
import com.spring.mvcframework.annotation.MyRequestMapping;
import com.spring.mvcframework.annotation.MyService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/9/20 11:03
 */
public class MyDispatcherServlet extends HttpServlet {
    //定义一个map用于存储包扫描的所有的类的全路径，作为key存入到容器中
    private Map<String, Object> mapping = new HashMap<>();
    //定义一个map用于存放bean
    private Map<String, Object> newMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500 exception"+ Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }


    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = request.getRequestURI();
        System.out.println("url ======>" + url);
        String contextPath = request.getContextPath();
        System.out.println("contextPath ====>" + contextPath);
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if (!this.newMapping.containsKey(url)) {
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().write("<h1>404 Not Found<h1>");
            return;
        }
        Method method = (Method) this.newMapping.get(url);
        Map<String, String[]> params = request.getParameterMap();
        method.invoke(this.newMapping.get(method.getDeclaringClass().getName()), new Object[]{request, response, params.get("name")[0]});
    }

    /**
     * init方法是servlet的容器初始化时执行，servlet的生命周期中只执行一次
     *
     * @param config servlet配置类，用于获取servlet应用的上下文环境
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try {
            Properties configContext = new Properties();
            //获取servletr容器配置文件的初始化参数
            String contextConfigLocation = config.getInitParameter("contextConfigLocation");
            //使用当前的类加载器获取所在目录的资源文件的输入流
            is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            //将输入流转为属性对象
            configContext.load(is);
            //获取需要包扫描的全路径
            String scanPackage = configContext.getProperty("scanPackage");
            //扫描父包路径的所有的全限定名
            doScanner(scanPackage);
            // 将url&method、service，实例化并存储在到ioc
            for (String className : mapping.keySet()) {
                if (!className.contains(".")) {
                    continue;
                }
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)) {
                    newMapping.put(className, clazz.newInstance());
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                        MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                        baseUrl = requestMapping.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                            continue;
                        }
                        MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                        String url = baseUrl + requestMapping.value();
                        newMapping.put(url, method);
                    }
                } else if (clazz.isAnnotationPresent(MyService.class)) {
                    MyService myService = clazz.getAnnotation(MyService.class);
                    String beanName = myService.value();
                    if ("".equals(beanName)) {
                        //获取全限定名
                        beanName = clazz.getName();
                        //clazz.getSimpleName();
                    }
                    Object instance = clazz.newInstance();
                    newMapping.put(beanName, instance);
                    for (Class<?> anInterface : clazz.getInterfaces()) {
                        newMapping.put(anInterface.getName(), instance);
                    }
                } else {
                    System.out.println("不需要的类不需要注入ioc中");
                    continue;
                }
            }

            //再判断controller层所引用的autowired的注解的service，实现属性的真正意义上的set方法实例化注入
            for (Object value : newMapping.values()) {
                if (null == value) {
                    continue;
                }
                Class clazz = value.getClass();
                if (clazz.isAnnotationPresent(MyController.class)) {
                    Field[] declaredFields = clazz.getDeclaredFields();
                    for (Field declaredField : declaredFields) {
                        if (!declaredField.isAnnotationPresent(MyAutowired.class)) {
                            continue;
                        }
                        String beanName = declaredField.getAnnotation(MyAutowired.class).value();
                        if ("".equals(beanName)) {
                            beanName = declaredField.getType().getName();
                        }
                        declaredField.setAccessible(true);
                        try {
                            declaredField.set(newMapping.get(clazz.getName()), newMapping.get(beanName));
                        } catch (Exception e) {
                            System.out.println("MyAutowired引用注入失败~");
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("初始化容器失败~");
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("My MVC Framework is init ");
    }

    /**
     * 解析Application.properties文件的包路径下
     * 所有的类的全限定名，并作为key加入到全局的map中
     *
     * @param scanPackage 所有的类的所在包的父包路径
     */
    private void doScanner(String scanPackage) {
        //将包名替换为目录，并返回URL资源
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        System.out.println("将包路径转为目录层级，父目录 ====>" + classDir.getName());
        for (File listFile : classDir.listFiles()) {
            String fileName = listFile.getName();
            if (listFile.isDirectory()) {
                String currentPackage = scanPackage + "." + fileName;
                doScanner(currentPackage);
            } else {
                if (!fileName.endsWith(".class")) {
                    continue;
                }
                //将类的全路径 com.xx.xx.class; 替换为com.xx.xx
                String currentCopyReference = scanPackage + "." + fileName.replace(".class", "");
                mapping.put(currentCopyReference, null);
            }
            newMapping.putAll(mapping);
        }
    }


    public static void main(String[] args) {
        String url = "/demo/query";
        String s = url.replaceAll("/", "");
        System.out.println(s);

        String servlet = "com.spring.mvcframework.v1.servlet.MyDispatcherServlet";
    }
}

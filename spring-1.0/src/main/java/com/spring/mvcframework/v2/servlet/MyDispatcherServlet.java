package com.spring.mvcframework.v2.servlet;

import com.spring.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author: chengqing Zhang
 * @description:
 * @date:Create：in 2022/9/22 9:42
 */
public class MyDispatcherServlet extends HttpServlet {

    // 资源文件转为内存存储
    Properties properties = new Properties();

    // 存储扫描到的包
    List<String> className = new ArrayList<>();

    // 用于存放url和对应的method
    Map<String, Method> handledMapping = new HashMap<>();

    //存放beanName和bean实例
    Map<String, Object> ioc = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException {

        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        if (!this.handledMapping.containsKey(url)) {
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().write("404 Not found");
            return;
        }

        Method method = this.handledMapping.get(url);
        Map<String, String[]> params = request.getParameterMap();
        System.out.println("request parameterMap ====" + params);

        Class<?>[] parameterTypes = method.getParameterTypes();
        System.out.println(" method parameterTypes first name" + parameterTypes[0].getName());

        Map<String, String[]> parameterMap = request.getParameterMap();

        Object[] paramValues = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                paramValues[i] = request;
                continue;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = response;
                continue;
            } else if (parameterType == String.class) {
                //提取方法中加了注解的参数
                Annotation[][] annotations = method.getParameterAnnotations();
                for (int i1 = 0; i1 < annotations.length; i1++) {
                    for (Annotation annotation : annotations[i1]) {
                        if (annotation instanceof MyRequestParam) {
                            String paramName = ((MyRequestParam) annotation).value();
                            if (!"".equals(paramName.trim())) {
                                String value = Arrays.toString(parameterMap.get(paramName))
                                        .replaceAll("\\[|\\]", "")
                                        .replaceAll("\\s", ",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }
                String beanName = toLowUpCaseFirst(method.getDeclaringClass().getSimpleName());
                method.invoke(ioc.get(beanName), paramValues);
            }

        }

    }


    @Override
    public void init(ServletConfig config) throws ServletException {

        // 加载属性配置文件到内存中
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 扫描包
        doScanner(properties.getProperty("scanPackage"));

        //利用反射技术初始化对象存入ico容器中
        doInstance();

        // 实现依赖注入
        doAutowired();

        // 初始化HandlerMapping
        handledMapping();

    }

    private void handledMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> aClass = entry.getValue().getClass();
            if (!aClass.isAnnotationPresent(MyController.class)) {
                continue;
            }
            String baseUrl = "";
            if (aClass.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping myRequestMapping = aClass.getAnnotation(MyRequestMapping.class);
                baseUrl = myRequestMapping.value();
            }
            Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
                String methodUrl = myRequestMapping.value();
                baseUrl = ("/" + baseUrl + "/" + methodUrl).replaceAll("/+", "/");
                handledMapping.put(baseUrl, method);
            }
        }
    }


    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            try {
                Field[] fields = entry.getValue().getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(MyAutowired.class)) {
                        MyAutowired myAutowired = field.getAnnotation(MyAutowired.class);
                        String myAutowiredValue = myAutowired.value();
                        String beanName = myAutowiredValue;
                        if ("".equals(myAutowiredValue.trim())) {
                            beanName = field.getType().getName();
                        }
                        field.setAccessible(true);
                        Object bean = ioc.get(beanName);
                        field.set(entry.getValue(), bean);
                    }
                }
            } catch (Exception e) {
                System.out.println("autowired failed ~");
                e.printStackTrace();
            }
        }
    }

    private void doInstance() {
        for (String clazz : className) {
            if (!clazz.contains(".")) {
                continue;
            }
            try {
                Class<?> aClass = Class.forName(clazz);
                if (aClass.isAnnotationPresent(MyController.class)) {
                    Object instance = aClass.newInstance();
                    ioc.put(toLowUpCaseFirst(aClass.getSimpleName()), instance);
                } else if (aClass.isAnnotationPresent(MyService.class)) {
                    MyService myService = aClass.getAnnotation(MyService.class);
                    String myServiceValue = myService.value();
                    String beanName = toLowUpCaseFirst(aClass.getSimpleName());
                    Object instance = aClass.newInstance();
                    if (!"".equals(myServiceValue)) {
                        beanName = myServiceValue;
                    }
                    ioc.put(beanName, instance);
                    //3 根据类型注入实现类，投机取巧的方式
                    for (Class<?> anInterface : aClass.getInterfaces()) {
                        if (ioc.containsKey(anInterface.getName())) {
                            throw new Exception("The beanName is exist！");
                        }
                        ioc.put(anInterface.getName(), instance);
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据父路径扫描包下的类
     *
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File resource = new File(url.getFile());
        for (File file : resource.listFiles()) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + fileName);
            } else {
                if (!fileName.endsWith(".class")) {
                    continue;
                }
                String beanNameReference = (scanPackage + "." + fileName).replace(".class", "");
                className.add(beanNameReference);
            }
        }
    }

    private void doLoadConfig(String initParameter) {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(initParameter)) {
            properties.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toLowUpCaseFirst(String beanName) {
        char[] chars = beanName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public static void main(String[] args) {
        Class<MyDispatcherServlet> myDispatcherServletClass = MyDispatcherServlet.class;
        System.out.println(myDispatcherServletClass.getSimpleName());
        Field[] declaredFields = myDispatcherServletClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            System.out.println("===filed of type===" + declaredField.getType());
            System.out.println("===filed of type simple name===" + declaredField.getType().getSimpleName());
            System.out.println("===filed of type name ====" + declaredField.getType().getName() + "\n");
        }

    }

}

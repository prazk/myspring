package org.prazk.spring.context.impl;

import org.prazk.spring.annotation.Autowired;
import org.prazk.spring.annotation.Component;
import org.prazk.spring.annotation.ComponentScan;
import org.prazk.spring.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class AnnotationConfigApplicationContext implements ApplicationContext {
    /**
     * 存放 bean的容器
     */
    private final Map<Class<?>, Object> beanFactory = new HashMap<>();

    /**
     * 构造方法：需要读取一个扫描路径配置类
     */
    public <T> AnnotationConfigApplicationContext(Class<T> clazz) {
        ComponentScan[] annotations = clazz.getDeclaredAnnotationsByType(ComponentScan.class);
        if (annotations.length == 0)
            throw new RuntimeException("无法找到@ComponentScan注解");
        Arrays.stream(annotations).forEach(A -> {
            String path = A.value().replaceAll("\\.", "/");
            try {
                // Finds all the resources with the given name
                Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(path);
                while (dirs.hasMoreElements()) {
                    URL url = dirs.nextElement();
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                    File folder = new File(filePath);
                    if (!folder.isDirectory()) {
                        throw new RuntimeException("扫描路径错误");
                    }

                    loadBean(folder, folder.getAbsolutePath().length() - A.value().length());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 依赖注入
        dependencyInjection();
    }

    /**
     * 根据类型获取 bean
     */
    @Override
    public <T> T getBean(Class<T> clazz) {
        return (T) beanFactory.get(clazz);
    }

    /**
     * 根据包扫描路径加载 bean到 IOC容器
     * 扫描路径的包及其子包中的所有类，一旦类上存在注解 @Component，就通过反射实例化一个 bean，并将其加入 IoC 容器
     * 如果有多个同类型的 bean，则抛出异常
     */
    private void loadBean(File folder, int absPathLength) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null || files.length == 0) {
                return;
            }
            for (File file : files) {
                loadBean(file, absPathLength);
            }
        } else {
            String filename = folder.getAbsolutePath();
            if (!filename.contains(".class"))
                return;
            String fullPackageName = filename.substring(absPathLength)
                    .replaceAll("\\\\", "\\.")
                    .replaceAll(".class", "");

            try {
                Class<?> clazz = Class.forName(fullPackageName);
                if (beanFactory.containsKey(clazz))
                    throw new RuntimeException("存在多个相同类型的bean");
                // 一旦类上存在注解 @Component，就通过反射实例化一个 bean，并将其加入 IoC 容器
                if (clazz.getAnnotation(Component.class) != null) {
                    Object bean = clazz.getConstructor().newInstance();
                    beanFactory.put(clazz, bean);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 依赖注入：
     * 注意到必须是组件才能注入依赖，因此可以根据容器中的 bean获取类，一旦类的属性上存在注解 @Autowired，就注入对应的依赖
     */
    private void dependencyInjection() {
        for (Map.Entry<Class<?>, Object> entry : beanFactory.entrySet()) {
            Object obj = entry.getValue(); // IOC 容器中的bean
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotation(Autowired.class) != null) {
                    field.setAccessible(true);
                    try {
                        Object value = beanFactory.get(field.getType());
                        field.set(obj, value); // obj.setField(value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * 测试用方法
     */
    public Map<Class<?>, Object> getBeanFactory() {
        return beanFactory;
    }

}

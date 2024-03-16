package org.prazk.spring.test;

import org.junit.jupiter.api.Test;
import org.prazk.spring.test.pojo.UserService;
import org.prazk.spring.test.config.ScanConfiguration;
import org.prazk.spring.context.impl.AnnotationConfigApplicationContext;

import java.util.Map;

public class BeanTest {
    @Test
    public void test1() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ScanConfiguration.class);
        for (Map.Entry<Class<?>, Object> entry : context.getBeanFactory().entrySet()) {
            System.out.println(entry.getValue().toString());
        }
        UserService userService = context.getBean(UserService.class);
        System.out.println(userService.getUser());
    }
}
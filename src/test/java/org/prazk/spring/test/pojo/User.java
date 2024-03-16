package org.prazk.spring.test.pojo;


import org.prazk.spring.annotation.Component;

@Component
public class User {
    private int age = 20;
    private String name;

    @Override
    public String toString() {
        return "User{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}

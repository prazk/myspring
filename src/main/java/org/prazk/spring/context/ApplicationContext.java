package org.prazk.spring.context;

public interface ApplicationContext {
    /**
     * 根据类型获取bean
     */
    <T> T getBean(Class<T> clazz);
}

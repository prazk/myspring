package org.prazk.spring.test.pojo;


import org.prazk.spring.annotation.Autowired;
import org.prazk.spring.annotation.Component;

@Component
public class UserService {
    @Autowired
    private User user;

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "UserService{" +
                "user=" + user +
                '}';
    }
}

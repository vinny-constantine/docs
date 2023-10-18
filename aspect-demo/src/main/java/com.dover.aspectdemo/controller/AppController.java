package com.dover.aspectdemo.controller;

import com.dover.aspectdemo.aspect.FooAspect;
import com.dover.aspectdemo.config.AppProps;
import com.dover.aspectdemo.config.UserProps;
import com.dover.aspectdemo.service.FooService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dover
 * @since 2021/9/1
 */
@RestController
@RequestMapping("/")
public class AppController {

    private static List<String> userList;
    @Resource
    private FooService1 fooService1;

    @Value("${userList}")
    private void setUserList(List<String> userList) {
        AppController.userList = userList;
    }

    @Resource
    private UserProps userProps;

    @Autowired(required = false)
    private AppProps appProps;

    @GetMapping("/hi")
    public void hello() {
//        System.out.println("hi");
//        System.out.println(userProps);
//        System.out.println(appProps.getName());
        fooService1.run(new UserProps());
    }
}

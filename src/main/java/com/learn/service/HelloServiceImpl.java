package com.learn.service;

/**
 * @author: lifs
 * @create: 2018-04-01 20:40
 **/
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "hello " + name;
    }
}

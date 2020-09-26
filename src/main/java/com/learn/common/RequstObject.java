package com.learn.common;

import java.io.Serializable;

/**
 * @author: lifs
 * @create: 2018-06-27 21:52
 **/
public class RequstObject implements Serializable {

    private static final long serialVersionUID = 8848119682625833820L;

    // 服务提供者接口
    private Class<?> calzz;

    // 服务的方法名称
    private String methodName;

    // 参数类型
    private Class<?>[] paramTypes;

    // 参数
    private Object[] args;

    public RequstObject(Class<?> calzz, String methodName, Class<?>[] paramTypes, Object[] args) {
        this.calzz = calzz;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.args = args;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Class<?> getCalzz() {
        return calzz;
    }

    public void setCalzz(Class<?> calzz) {
        this.calzz = calzz;
    }
}

package com.learn.common;

import java.io.Serializable;

/**
 * @author: lifs
 * @create: 2018-07-01 11:14
 **/
public class ResponseObject implements Serializable {

    private static final long serialVersionUID = 9221265787509458134L;

    private Long requstId;

    private Object result;

    public Long getRequstId() {
        return requstId;
    }

    public void setRequstId(Long requstId) {
        this.requstId = requstId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}

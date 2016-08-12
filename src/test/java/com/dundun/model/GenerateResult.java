package com.dundun.model;

/**
 * Created by dunxuliu on 2015/12/7.
 */
public class GenerateResult<T> {
    T errorCode;

    public T getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(T errorCode) {
        this.errorCode = errorCode;
    }
}

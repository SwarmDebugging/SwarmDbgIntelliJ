package com.swarm;


import com.sun.jdi.Method;

public class invokingMethod {
    private int id;
    private Method method;

    public invokingMethod(){}

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}

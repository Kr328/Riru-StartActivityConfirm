package com.github.kr328.sac;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvokeRecord {
    public Object caller;
    public Method method;
    public Object[] args;

    public MethodInvokeRecord(Object caller, Method method, Object[] args) {
        this.caller = caller;
        this.method = method;
        this.args = args;
    }

    public Object invoke() throws InvocationTargetException, IllegalAccessException {
        return method.invoke(caller, args);
    }
}

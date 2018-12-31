package com.github.kr328.sac;

import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@SuppressWarnings("unchecked")
public class InterfaceProxy {
    public static <Interface extends IInterface> IBinder createInterfaceProxyBinder(Interface original, String interfaceName, InterfaceCallback<Interface> callback) {
        IBinder originalBinder = original.asBinder();

        InvocationHandler interfaceInvocationHandler = (Object thiz, Method method, Object[] args) -> callback.onCalled(original, (Interface) thiz, method, args);

        InvocationHandler binderInvocationHandler = (Object thiz, Method method, Object[] args) -> {
            try {
                if (method.getName().equals("queryLocalInterface")) {
                    //Log.i(Global.TAG ,"queryLocalInterface " + args[0] + " == " + interfaceName);
                    if (interfaceName.equals(args[0]))
                        return Proxy.newProxyInstance(original.getClass().getClassLoader(), new Class[]{Class.forName(interfaceName)}, interfaceInvocationHandler);
                }
            } catch (Exception ignored) {
                Log.w(Global.TAG, "Proxy " + original.getClass().getName() + " failure.");
            }

            return method.invoke(originalBinder, args);
        };

        return (IBinder) Proxy.newProxyInstance(original.getClass().getClassLoader(), new Class[]{IBinder.class}, binderInvocationHandler);
    }

    public static <Interface> Interface createInterfaceProxy(Interface original, Class<?>[] proxy, InterfaceCallback<Interface> callback) {
        return (Interface) Proxy.newProxyInstance(original.getClass().getClassLoader(), proxy, (replaced, method, args) -> callback.onCalled(original, (Interface) replaced, method, args));
    }

    public interface InterfaceCallback<Interface> {
        Object onCalled(Interface original, Interface replaced, Method method, Object[] args) throws Throwable;
    }
}

package com.github.kr328.sac;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ApplicationInjector {
    public static int inject() {
        Log.i(Global.TAG, "Current in Application");

        ActivityThread currentActivityThread = ActivityThread.currentActivityThread();
        if (currentActivityThread == null)
            return 0;

        Instrumentation instrumentation = currentActivityThread.getInstrumentation();
        if (instrumentation == null)
            return 0;

        IBinder binder = ServiceManager.getService(StartActivityConfirmService.NAME);
        if (binder == null) {
            Log.w(Global.TAG, "Service not started.");
            return 1;
        }

        try {
            install();
        } catch (Exception e) {
            Log.e(Global.TAG, "Failure proxy IActivityManager ", e);
        }

        return 1;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static void install() throws Exception {
        Field singletonField = ActivityManager.class.getDeclaredField("IActivityManagerSingleton");
        singletonField.setAccessible(true);
        Singleton singleton = (Singleton) singletonField.get(null);
        IActivityManager activityManager = (IActivityManager) singleton.get();
        singletonField.set(null, new Singleton<IActivityManager>() {
            @Override
            protected IActivityManager create() {
                return InterfaceProxy.createInterfaceProxy(activityManager, new Class[]{IActivityManager.class}, ApplicationInjector::onActivityCalled);
            }
        });
    }

    private static Object onActivityCalled(IActivityManager original, IActivityManager replaced, Method method, Object[] args) throws Exception {
        if (!"startActivity".equals(method.getName()))
            return method.invoke(original, args);

        if (args.length < 7)
            return method.invoke(original, args);

        if (!(args[2] instanceof Intent))
            return method.invoke(original, args);

        if (!(args[4] instanceof IBinder))
            return method.invoke(original, args);

        if (args[5] != null && !(args[5] instanceof String))
            return method.invoke(original, args);

        return ActivityRequest.request((Intent) args[2], (IBinder) args[4], (String) args[5], (int) args[6]
                , new MethodInvokeRecord(original, method, args));
    }
}

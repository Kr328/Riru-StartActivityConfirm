package com.github.kr328.sac;

import android.app.ActivityThread;
import android.os.ServiceManager;
import android.util.Log;

public class SystemServerInjector {
    public static int inject() {
        Log.i(Global.TAG, "system_server");

        ActivityThread activityThread = ActivityThread.currentActivityThread();
        if (activityThread == null)
            return 0;

        new Thread(SystemServerInjector::start).start();

        return 1;
    }

    private static void start() {
        ServiceManager.addService(StartActivityConfirmService.NAME, new StartActivityConfirmService());
    }
}

package android.app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.util.ArrayMap;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

public final class ActivityThread {
    final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap<>();
    final ArrayMap<IBinder, Service> mServices = new ArrayMap<>();
    final ArrayList<Application> mAllApplications = new ArrayList<>();

    public static final class ActivityClientRecord {
        public IBinder token;
        Intent intent;
        String referrer;
        Activity activity;
        Window window;
        Activity parent;
        ActivityInfo activityInfo;
    }

    public static ActivityThread currentActivityThread() {
        throw new RuntimeException("Stub");
    }

    public Instrumentation getInstrumentation() {
        throw new RuntimeException("Stub");
    }

    public Application getApplication() {
        throw new RuntimeException("Stub");
    }

    public void handleSendResult(IBinder token, List<ResultInfo> results, String reason) {
        throw new RuntimeException("Stub");
    }

    public ContextImpl getSystemUiContext() {throw new RuntimeException("Stub"); }
    public ContextImpl getSystemContext() {throw new RuntimeException("Stub"); }
}

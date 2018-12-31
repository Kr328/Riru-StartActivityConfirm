package com.github.kr328.sac;

import android.app.Activity;
import android.app.ActivityThread;
import android.content.Intent;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ActivityResult {
    private static ArrayMap activities;
    private static Field activityField;
    private static Method dispatchResultField;

    public static void send(IBinder token, String toWho, int requestCode, int resultCode, Intent data, String reason) throws Exception {
        init();

        ActivityThread.ActivityClientRecord record = (ActivityThread.ActivityClientRecord) activities.get(token);
        Activity activity = (Activity) activityField.get(record);

        activity.runOnUiThread(() -> {
            try {
                dispatchResultField.invoke(activity, toWho, requestCode, resultCode, data, reason);
            } catch (Exception e) {
                Log.w(Global.TAG, "Dispatch result failure", e);
            }
        });
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static void init() throws Exception {
        if (activityField != null)
            return;

        ActivityThread activityThread = ActivityThread.currentActivityThread();

        Field mActivitiesField = activityThread.getClass().getDeclaredField("mActivities");
        mActivitiesField.setAccessible(true);
        activities = (ArrayMap) mActivitiesField.get(activityThread);

        activityField = android.app.ActivityThread.ActivityClientRecord.class.getDeclaredField("activity");
        activityField.setAccessible(true);

        dispatchResultField = Activity.class.getDeclaredMethod("dispatchActivityResult", String.class, int.class, int.class, Intent.class, String.class);
        dispatchResultField.setAccessible(true);
    }
}

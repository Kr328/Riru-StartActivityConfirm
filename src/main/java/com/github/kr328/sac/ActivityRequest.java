package com.github.kr328.sac;

import android.app.Activity;
import android.app.ActivityThread;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.SparseArray;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

public class ActivityRequest {
    private static final Random random = new Random();
    private static IStartActivityConfirmService service;
    private static SparseArray<Record> recordSparseArray = new SparseArray<>();
    private static int isDefaultLauncher = 0;

    private static class Record {
        public Intent intent;
        public IBinder token;
        public String toWho;
        public int requestCode;
        public MethodInvokeRecord methodInvoke;
    }

    private synchronized static IStartActivityConfirmService getService() {
        if (service == null)
            service = IStartActivityConfirmService.Stub.
                    asInterface(ServiceManager.getService(StartActivityConfirmService.NAME));
        return service;
    }

    private static final IConfirmCallback.Stub CALLBACK = new IConfirmCallback.Stub() {
        @Override
        public void onResult(int requestId, boolean allow) throws RemoteException {
            Record record = recordSparseArray.get(requestId);
            if (record == null) {
                Log.w(Global.TAG, "RequestId " + requestId + " not found");
                return;
            }
            recordSparseArray.remove(requestId);

            if (allow) {
                try {
                    record.methodInvoke.invoke();
                } catch (Exception e) {
                    Log.w(Global.TAG, "Start activity failure");
                    allow = false;
                }
            }

            if (!allow && record.requestCode > 0) {
                try {
                    ActivityResult.send(record.token, record.toWho, record.requestCode
                            , Activity.RESULT_CANCELED, null, "Canceled");
                } catch (Exception e) {
                    Log.w(Global.TAG, "Dispatch result failure", e);
                }
            }
        }
    };

    public static int request(Intent intent, IBinder token, String toWho, int requestCode, MethodInvokeRecord originalInvoke) throws Exception {
        if (intent == null)
            return (int) originalInvoke.invoke();

        String currentPackage = getContext().getPackageName();

        if (currentPackage.equals(intent.getPackage()))
            return (int) originalInvoke.invoke();

        ComponentName componentName = intent.getComponent();

        if (componentName != null && currentPackage.equals(componentName.getPackageName()))
            return (int) originalInvoke.invoke();

        if ( intent.hasCategory(Intent.CATEGORY_LAUNCHER) ) {
            if ( isDefaultLauncher == 0 ) {
                ResolveInfo info = getContext().getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).
                        addCategory(Intent.CATEGORY_LAUNCHER) ,PackageManager.MATCH_DEFAULT_ONLY);
                if ( info.activityInfo != null && info.activityInfo.packageName.equals(currentPackage) )
                    isDefaultLauncher = 1;
                else
                    isDefaultLauncher = -1;
            }
            if ( isDefaultLauncher == 1 )
                return (int) originalInvoke.invoke();
        }

        if (Intent.ACTION_GET_CONTENT.equals(intent.getAction()))
            return (int) originalInvoke.invoke();

        if ( Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null ) {
            switch (intent.getData().getScheme()) {
                case "http" :
                case "https" :
                case "content" :
                case "file" :
                    return (int) originalInvoke.invoke();
            }
        }

        ActivityInfo activityInfo = resolveActivityInfo(intent);
        if ( activityInfo == null )
            throw new ActivityNotFoundException("");

        Log.i(Global.TAG ,"Process " + intent);

        long result = 0;
        int requestId = random.nextInt();
        try {
            result = getService().startConfirm(requestId, currentPackage, activityInfo.packageName, CALLBACK);
        } catch (Exception e) {
            Log.w(Global.TAG, "Blocking " + currentPackage + " failure", e);
            return (int) result;
        }

        int remoteCode = (int) ((result) >> 32);
        int resultCode = (int) (result);

        switch (remoteCode) {
            case StartActivityConfirmService.REMOTE_RESULT_CAPTURE:
                Record record = new Record();
                record.intent = intent;
                record.token = token;
                record.requestCode = requestCode;
                record.toWho = toWho;
                record.methodInvoke = originalInvoke;
                recordSparseArray.append(requestId, record);
                return 0;
            case StartActivityConfirmService.REMOTE_RESULT_RAISE_ERROR:
                return resultCode;
            case StartActivityConfirmService.REMOTE_RESULT_NOT_CAPTURE:
                return (int) originalInvoke.invoke();
        }

        return resultCode;
    }

    private static ActivityInfo resolveActivityInfo(Intent intent) {
        ResolveInfo resolveInfo = getContext().getPackageManager().resolveActivity(intent ,0);

        return resolveInfo.activityInfo;
    }

    private static Context getContext() {
        return ActivityThread.currentActivityThread().getApplication();
    }
}

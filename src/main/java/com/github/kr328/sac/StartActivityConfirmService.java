package com.github.kr328.sac;

import android.app.ActivityThread;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.util.ResourceBundle;
import java.util.TreeSet;

public class StartActivityConfirmService extends IStartActivityConfirmService.Stub {
    public static final String NAME = "start_activity_confirm";

    public static final String WHITE_LIST_PATH = "/system/etc/sac/target_whitelist";

    public static final int REMOTE_RESULT_CAPTURE = 0;
    public static final int REMOTE_RESULT_NOT_CAPTURE = 1;
    public static final int REMOTE_RESULT_RAISE_ERROR = 2;

    private TreeSet<Integer> uidWhiteList = new TreeSet<>();
    private TreeSet<String> packageWhiteList = new TreeSet<>();

    public StartActivityConfirmService() {
        Thread uiThread = new Thread(() -> {
            Looper.prepare();
            uiHandler = new Handler();
            Looper.loop();
        });
        uiThread.start();

        loadWhiteList();
    }


    private void loadWhiteList() {
        String[] files = new File(WHITE_LIST_PATH).list();
        if (files == null)
            return;

        for (String file : files) {
            if (file.startsWith("package."))
                packageWhiteList.add(file.replace("package.", ""));
            else if (file.startsWith("uid."))
                uidWhiteList.add(Integer.parseInt(file.replace("uid.", "")));
        }
    }

    @Override
    public long startConfirm(int requestId, String source, String target, IConfirmCallback callback) throws RemoteException {
        Context context = ActivityThread.currentActivityThread().getSystemContext();
        PackageManager packageManager = context.getPackageManager();

        try {
            ApplicationInfo sourceInfo = packageManager.getApplicationInfo(source, 0);
            ApplicationInfo targetInfo = packageManager.getApplicationInfo(target, 0);

            if (uidWhiteList.contains(targetInfo.uid) || packageWhiteList.contains(target))
                return buildResult(REMOTE_RESULT_NOT_CAPTURE, 0);

            source = sourceInfo.loadLabel(packageManager).toString();
            target = targetInfo.loadLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            throw new RemoteException(e.toString());
        }

        String finalSource = source;
        String finalTarget = target;
        uiHandler.post(() -> showDialog(finalSource, finalTarget, requestId, callback));

        return buildResult(0, 0);
    }

    private void showDialog(String source, String target, int requestId, IConfirmCallback callback) {
        AlertDialog dialog = new AlertDialog.Builder(ActivityThread.currentActivityThread().getSystemUiContext())
                .setTitle("启动外部应用")
                .setCancelable(false)
                .setMessage(source + " 尝试启动 " + target)
                .setPositiveButton("允许", (d, a) -> respond(callback, requestId, true))
                .setNegativeButton("拒绝", (d, a) -> respond(callback, requestId, false))
                .create();

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);

        dialog.show();
    }

    private void respond(IConfirmCallback callback, int requestId, boolean allow) {
        try {
            callback.onResult(requestId, allow);
        } catch (RemoteException e) {
            Log.w(Global.TAG, "Call callback failure", e);
        }
    }

    private long buildResult(int serviceStatus, int actvivtyStatus) {
        return ((long) serviceStatus << 32) | (long) actvivtyStatus;
    }

    private Handler uiHandler;
}

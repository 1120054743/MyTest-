package com.example.lopez.mytest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;

public class APKDownLoadService extends Service {
    private static final String advUrl = "advUrl";
    private static final String advName = "advName";
    private static final String START_STOP_ACTION = "start_stop_action";
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteViews;
    private String apkName;
    private int notifyId = 10086;
    private StartPauseBroadcastReceiver mReceiver;
    private boolean isDownload = true;//默认下载中
    private DownloadListener mDownloadListener;
    private FileDownloader mFileDownloader;
    private BaseDownloadTask downloadTask;
    private Context context = this;

    public static void startService(Context context, String url, String name) {
        Intent intent = new Intent(context, APKDownLoadService.class);
        intent.putExtra(advUrl, url);
        intent.putExtra(advName, name);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("==w", "onCreate: ");

        //动态注册个广播
        mReceiver = new StartPauseBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(START_STOP_ACTION);
        registerReceiver(mReceiver, filter);

        //初始化下载对象
        mDownloadListener = new DownloadListener();
        mFileDownloader = FileDownloader.getImpl();

        Log.d("==w", "onCreate: getPackageName: " + getPackageName());

        //notification对象
        mBuilder = new NotificationCompat.Builder(APKDownLoadService.this);
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_adv_download_view);

        //暂停/开始下载的点击事件
        Intent operateIntent = new Intent(START_STOP_ACTION);
        PendingIntent operatePI = PendingIntent.getBroadcast(context, 0, operateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.file_operate_iv, operatePI);

        mBuilder.setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_icon))
                .setAutoCancel(false)
                .setContent(mRemoteViews)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true);

        mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("==w", "onStartCommand: ");
        if (intent != null) {
            String url = intent.getStringExtra(advUrl);
            apkName = intent.getStringExtra(advName);

            if (!TextUtils.isEmpty(url)) {
                Log.d("==w", "onStartCommand: url: " + url);
                Log.d("==w", "onStartCommand: apkName: " + apkName);
                Toast.makeText(context, "开始下载 " + apkName + ".apk", Toast.LENGTH_LONG).show();

                File dir = new File(Constant.DL_APK_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                Log.d("==w", "onStartCommand: 下载任务开始");

                downloadTask = mFileDownloader.create(url)
                        .setPath(Constant.DL_APK_PATH + apkName + ".apk")
                        .setAutoRetryTimes(1)
                        .setForceReDownload(false)
                        .setListener(mDownloadListener);
                String path = downloadTask.getPath();
                Log.d("==w", "onStartCommand: path: " + path);
                downloadTask.start();

                //弹出通知
                //mNotificationManager.notify(notifyId, mNotification);
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    public static String sizeFormatNum2String(int size) {
        String s = "";
        //如果对于1M
        if (size > 1024 * 1024)
            s = String.format("%.2f", (double) size / (1024 * 1024)) + "M";
        else
            s = String.format("%.2f", (double) size / (1024)) + "KB";
        return s;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public class StartPauseBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("==w", "onReceive: 我在外面打酱油:\t\t" + (intent == null ? "传过来的是空的" : "有料"));

            if (intent != null && intent.getAction().equals(START_STOP_ACTION)) {
                Log.d("==w", "onReceive: 收到消息了");

                if (isDownload) {
                    Log.d("==w", "onReceive: 暂停");
                    downloadTask.pause();
                    mRemoteViews.setTextViewText(R.id.speed_tv, "不要停٩(๑> ₃ <)۶");
                    mRemoteViews.setImageViewResource(R.id.file_operate_iv, R.drawable.download);
                } else {
                    Log.d("==w", "onReceive: 开始");
                    downloadTask.start();
                    mRemoteViews.setImageViewResource(R.id.file_operate_iv, R.drawable.pause);
                }
                isDownload = !isDownload;
                mBuilder.setContent(mRemoteViews);

                Log.d("==w", "onReceive: 开启通知");
                mNotificationManager.notify(notifyId, mBuilder.build());
            }
        }
    }

    /**
     * 1MB = 1024KB = 1024 * 1024B = 1048576B
     * B代表字节，KB千字节，MB是兆字节
     * 还有b表示的是数据位，1B=8bit（b）
     */
    public class DownloadListener extends FileDownloadListener {

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            Log.i("==w", "可怕的东西准备");
            mNotificationManager.notify(notifyId, mNotification);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            Log.i("==w", "可怕的东西更新" + (soFarBytes + 0.0 / totalBytes));
            double percent = (double) soFarBytes / (double) totalBytes;
            mRemoteViews.setTextViewText(R.id.apk_name_tv, task.getFilename());
            mRemoteViews.setTextViewText(R.id.speed_tv, task.getSpeed() + "K/s");
            mRemoteViews.setTextViewText(R.id.file_now_all_tv, sizeFormatNum2String(soFarBytes) + "/" + sizeFormatNum2String(totalBytes));
            mRemoteViews.setProgressBar(R.id.apk_download_pb, 100, (int) (percent * 100), false);
            mBuilder.setContent(mRemoteViews);
            mNotificationManager.notify(notifyId, mBuilder.build());
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            File file = new File(task.getPath());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 24) {
                Uri apkUri = FileProvider.getUriForFile(context, "com.example.lopez.mytest.fileprovider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
            context.startActivity(intent);
            mNotificationManager.cancel(notifyId);
            Log.i("==w", "可怕的东西完成" + task.getPath());

        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            Log.d("==w", "paused: ");
            task.reuse();
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            //Log.i("==w", "可怕的东西错误" + e.getMessage());
            Log.i("==w", "可怕的东西错误" + e.getMessage() + "\t\n");
            e.printStackTrace();
        }

        @Override
        protected void warn(BaseDownloadTask task) {
            Log.d("==w", "warn: 警告");
        }

    }

}

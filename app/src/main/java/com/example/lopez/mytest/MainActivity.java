package com.example.lopez.mytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.mob.MobSDK;

import cn.sharesdk.onekeyshare.OnekeyShare;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
    private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";
    private String appName = "";
    private String appUrl = "https://test.banana-punch.com/applet-api/static/700BIKE-700-release-v2.2.3.apk";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;

        /**
         * Home是系统事件，只能通过广播监听，代码动态注册广播
         */
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Log.d("==w", "onKeyDown: KEYCODE_BACK 返回键");
                showDialog();
                break;
            case KeyEvent.KEYCODE_MENU:
                Log.d("==w", "onKeyDown: KEYCODE_MENU 菜单键");
                break;
            case KeyEvent.KEYCODE_HOME:
                Log.d("==w", "onKeyDown:测试 KEYCODE_MENU Home键");
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showDialog() {
        new AlertDialog.Builder(context).setTitle("注意!!").setMessage("确定退出吗")
                .setNeutralButton("哦，没事了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "没事了", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        }).create().show();
    }

    @Override
    protected void onUserLeaveHint() {
        Log.d("==w", "onUserLeaveHint: ");
        super.onUserLeaveHint();
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Log.d("==w", "onSaveInstanceState: ");
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.d("==w", "onDestroy: ");
        unregisterReceiver(receiver);
    }


    //广播接收者
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("==w", "onReceive: action: " + action);

            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.d("==w", "onReceive: reason: " + reason);

                //关于home键的监听
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    Log.d("==w", "onReceive: 短按home键");
                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    Log.d("==w", "onReceive: 长按Home键 或者 activity切换键");
                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    Log.d("==w", "onReceive: 锁屏");
                } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    Log.d("==w", "onReceive: 三星机 samsung 长按Home键");
                }

            }


        }
    };


    //去分享的内容
    public void onShareClick(View view) {
        // 通过代码注册你的AppKey和AppSecret
        MobSDK.init(context, "1fe3f52efffb4", "9698c2c45a2fed1118299ce202c4a1a8");

        showShare();

    }


    private void showShare() {

        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字  2.5.9以后的版本不     调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));

        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.share));

        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("http://sharesdk.cn");

        // text是分享文本，所有平台都需要这个字段
        oks.setText("我是分享文本");

        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片

        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");

        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");

        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));

        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

        // 启动分享GUI
        oks.show(this);

    }


    //启用下载
    public void onDownloadClick(View view) {
        APKDownLoadService.startService(MainActivity.this, appUrl, "异次元通讯");
        //Toast.makeText(context, "启动下载...", Toast.LENGTH_SHORT).show();
    }


}

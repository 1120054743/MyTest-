package com.example.lopez.mytest;

import android.app.Application;


import com.liulishuo.filedownloader.FileDownloader;

/**
 * Created by Lopez on 2017/8/3.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化下载器
        FileDownloader.init(getApplicationContext());
    }
    
}

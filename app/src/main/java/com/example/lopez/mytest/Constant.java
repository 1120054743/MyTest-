package com.example.lopez.mytest;

import android.os.Environment;

/**
 * Created by Lopez on 2017/8/3.
 */

public class Constant {

    public final static String SDCARD = Environment.getExternalStorageDirectory().getPath();
    public static String DOWNLOAD_DIR = SDCARD + "/download/extra_msg";

    //安装包 下载的APK存放 路径
    public static final String DL_APK_PATH = DOWNLOAD_DIR + "/apk/";


}

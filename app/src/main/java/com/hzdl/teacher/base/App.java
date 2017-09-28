package com.hzdl.teacher.base;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;

import com.hzdl.mex.utils.MyCrashHandler;
import com.hzdl.teacher.service.Mservice;

import java.io.File;

/**
 * Created by wangshuai on 2017/9/15.
 */

public class App extends Application {

    //单例
    private static App myApplication = null;
    public static App getApplication(){
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        initService();
        initParam();
        //initCrash();
    }

    private void initParam() {
        String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.FILE_PATH;
        File filePath = new File(sdDir);
        if (!filePath.exists()) {
            filePath.mkdir();
        }
    }

    private void initService() {
        Intent startIntent = new Intent(this, Mservice.class);
        startService(startIntent);
    }

    private void initCrash() {
        // 初始化捕捉异常的类
        MyCrashHandler handler = MyCrashHandler.getInstance();
        handler.init(getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }


}

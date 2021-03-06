package com.github.autoreply;

import android.app.Application;

import com.github.autoreply.crash.AppCrashHandler;
import com.github.autoreply.service.InitializeService;

/**
 * function:
 * Created by 韦国旺 on 2017/7/7 0007.
 * Copyright (c) 2017 All Rights Reserved.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NimUIKit.init(this);
        AppCrashHandler.getInstance(this);
//        new Thread().start();
        InitializeService.start(this);   //后台任务初始化
    }
}

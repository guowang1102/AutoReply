package com.github.autoreply.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * function:
 * Created by 韦国旺 on 2017/7/13 0013.
 * Copyright (c) 2017 北京联龙博通 All Rights Reserved.
 */
public class InitializeService extends IntentService {

    public static void start(Context context) {
        //初始化任务

    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public InitializeService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}

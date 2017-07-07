package com.github.autoreply;

import android.content.Context;

/**
 * function:
 * Created by 韦国旺 on 2017/7/7 0007.
 * Copyright (c) 2017 北京联龙博通 All Rights Reserved.
 */
public final class NimUIKit {
    // context
    private static Context context;

    public static void init(Context context) {
        NimUIKit.context = context.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}

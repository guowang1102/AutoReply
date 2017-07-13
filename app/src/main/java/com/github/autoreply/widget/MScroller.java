package com.github.autoreply.widget;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * function:
 * Created by 韦国旺 on 2017/7/13 0013.
 * Copyright (c) 2017 北京联龙博通 All Rights Reserved.
 */
public class MScroller extends Scroller {

    private static final Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float input) {
            input -= 1.0f;
            return input * input * input * input * input + 1.0f;
        }
    };

    public boolean noDuration;

    public void setNoDuration(boolean noDuration) {
        this.noDuration = noDuration;
    }


    public MScroller(Context context) {
        super(context);
    }

    public MScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public MScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {


    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        if (noDuration) {
            super.startScroll(startX, startY, dx, dy, 0);
        } else {
            super.startScroll(startX, startY, dx, dy, duration);
        }
    }
}

package com.github.autoreply.widget;

import android.support.v4.view.ViewPager;

import java.lang.reflect.Field;


/**
 * function:
 * Created by 韦国旺 on 2017/7/13 0013.
 * Copyright (c) 2017 北京联龙博通 All Rights Reserved.
 */
public class ViewPagerHelper {

    ViewPager viewPager;

    MScroller mScroller;

    public MScroller getScroller() {
        return mScroller;
    }

    public ViewPagerHelper(ViewPager viewPager) {
        this.viewPager = viewPager;
        init();
    }

    private void init() {
        mScroller = new MScroller(viewPager.getContext());
        Class<ViewPager> c1 = ViewPager.class;
        try {
            Field field = c1.getDeclaredField("mScroller");
            field.setAccessible(true);
            //利用反射设置mScroller为自己定义的MScroller
            field.set(viewPager, mScroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }

    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    private void setCurrentItem(int item, boolean somoth) {
        int current = viewPager.getCurrentItem();
        //如果页面间隔大于1，就设置页面切换的动画时间为0
        if (Math.abs(current - item) > 1) {
            mScroller.setNoDuration(true);
            viewPager.setCurrentItem(item, somoth);
            mScroller.setNoDuration(false);
        } else {
            mScroller.setNoDuration(false);
            viewPager.setCurrentItem(item, somoth);
        }

    }


}

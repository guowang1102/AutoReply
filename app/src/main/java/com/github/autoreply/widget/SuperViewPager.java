package com.github.autoreply.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * function:
 * Created by 韦国旺 on 2017/7/13 0013.
 * Copyright (c) 2017 北京联龙博通 All Rights Reserved.
 */
public class SuperViewPager extends ViewPager {

    private ViewPagerHelper helper;

    public SuperViewPager(Context context) {
        this(context,null);
    }

    public SuperViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        helper = new ViewPagerHelper(this);
        setReadEffect();
    }

    private void setReadEffect() {
        setPageTransformer(true, new PageTransformer() {
            private  static final float MIN_SCALE =  0.75f;

            @Override
            public void transformPage(View page, float position) {
                int pageWidth = page.getWidth();
                int pageHeight = page.getHeight();
                if(position<-1){
                    page.setAlpha(0);
                }else if(position<=0) {
                    page.setAlpha(1);
                    page.setTranslationX(0);
                    page.setScaleX(1);
                    page.setScaleY(1);
                }else if(position<=1){
                    page.setAlpha(1);
                    page.setTranslationX(pageWidth*-position);
                }else {
                    page.setAlpha(0);
                }

            }
        });
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        MScroller scroller= helper.getScroller();
        if (Math.abs(getCurrentItem() - item) > 1) {
            scroller.setNoDuration(true);
            super.setCurrentItem(item, smoothScroll);
            scroller.setNoDuration(false);
        } else {
            scroller.setNoDuration(false);
            super.setCurrentItem(item, smoothScroll);
        }

    }
}

package com.washathomes.apputils.helpers;

import android.os.Handler;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.Locale;

public class Helpers {

    public static void setSliderTimer(int delay, ViewPager viewPager, PagerAdapter pagerAdapter){
        Handler handler = new Handler();
        final Runnable[] runnable = new Runnable[1];

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int pagerIndex = viewPager.getCurrentItem();
                pagerIndex++;
                if (pagerIndex >= pagerAdapter.getCount()){
                    pagerIndex = 0;
                }
                pagerAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(pagerIndex);
                pagerAdapter.notifyDataSetChanged();

                runnable[0] = this;
                handler.postDelayed(runnable[0],delay);
            }
        },delay);
    }

    public static String getLocale(){
        return Locale.getDefault().getLanguage();
    }

    public static float convertValuesRange(float value, float oldMax, float oldMin, float newMax, float newMin){
        float oldRange = oldMax - oldMin;
        float newRange = newMax - newMin;
        return (((value - oldMin) * newRange) / oldRange) + newMin;
    }

}

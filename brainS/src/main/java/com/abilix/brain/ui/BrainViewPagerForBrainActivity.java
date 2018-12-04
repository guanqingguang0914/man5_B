package com.abilix.brain.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.utils.LogMgr;

/**
 * Created by yangz on 2017/8/29.
 */

public class BrainViewPagerForBrainActivity extends MyViewPager {
    private boolean scrollble = true;

    private  boolean noScroll = false;

    public BrainViewPagerForBrainActivity(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C9){
            LogMgr.e("设置滑动判定为页面的20%");
            BrainViewPagerForBrainActivity.this.setPageOffsetToChange(0.2f);
        }
    }

    public BrainViewPagerForBrainActivity(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!scrollble) {
            return true;
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
//		return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(noScroll){
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if(noScroll){
		    return false;
        }
        return super.onTouchEvent(arg0);
	}

    public boolean isScrollble() {
        return scrollble;
    }

    public void setScrollble(boolean scrollble) {
        this.scrollble = scrollble;
    }
    public void setNoScroll(boolean noScroll){
        this.noScroll = noScroll;
    }
}

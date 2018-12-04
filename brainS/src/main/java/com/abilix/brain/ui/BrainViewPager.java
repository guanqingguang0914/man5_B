package com.abilix.brain.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Brain中使用的ViewPager类。
 */
public class BrainViewPager extends ViewPager {
    private boolean scrollble = true;

    public BrainViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
//        if(GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C9){
//            LogMgr.e("设置滑动判定为页面的20%");
//            BrainViewPager.this.setPageOffsetToChange(0.2f);
//        }
    }

    public BrainViewPager(Context context) {
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


//	@Override
//	public boolean onTouchEvent(MotionEvent arg0) {
//		if (!scrollble) {
//			return true;
//		}
//		return super.onTouchEvent(arg0);
//	}

    public boolean isScrollble() {
        return scrollble;
    }

    public void setScrollble(boolean scrollble) {
        this.scrollble = scrollble;
    }
}

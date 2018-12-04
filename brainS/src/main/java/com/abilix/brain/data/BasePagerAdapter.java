package com.abilix.brain.data;

import java.util.LinkedList;
import java.util.List;

import com.abilix.brain.BrainActivity.ViewHoder;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.GlobalConfig;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

/**
 * {@link com.abilix.brain.BrainActivity}中Viewpage的适配器类。
 */
public class BasePagerAdapter extends PagerAdapter {
    private List<AppInfo> mAppInfos = new LinkedList<AppInfo>();

    public BasePagerAdapter(List<AppInfo> appInfos) {
        this.mAppInfos = appInfos;
    }


    @Override
    public int getCount() {
        if (GlobalConfig.isUnlimitLoopRightSide == false) {
            return mAppInfos.size();
        } else if (GlobalConfig.isUnlimitLoopRightSide == true && GlobalConfig.isUnlimitLoopBothSide == false) {
//			return Integer.MAX_VALUE;
            return GlobalConfig.MAX_PAGE_INDEX;
        } else {
//			return Integer.MAX_VALUE;
            return GlobalConfig.MAX_PAGE_INDEX;
        }

    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        LogMgr.d("destroyItem() position = " + position + " mAppInfos.size() = " + mAppInfos.size() + " 删除页面名 = " + mAppInfos.get(position % mAppInfos.size()).getViewHoder().textview.getText());
        // container.removeView(mViews.get(position));
        if (GlobalConfig.isUnlimitLoopRightSide == false) {
            View view = (View) object;
            ((ViewPager) container).removeView(view);
            view = null;
        } else if (GlobalConfig.isUnlimitLoopRightSide == true && GlobalConfig.isUnlimitLoopBothSide == false) {
            container.removeView(mAppInfos.get(position % mAppInfos.size()).getViewHoder().view);
        } else {
            container.removeView(mAppInfos.get(position % mAppInfos.size()).getViewHoder().view);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LogMgr.d("instantiateItem() position = " + position + " mAppInfos.size() = " + mAppInfos.size() + " 增加页面名 = " + mAppInfos.get(position % mAppInfos.size()).getViewHoder().textview.getText());
        if (GlobalConfig.isUnlimitLoopRightSide == false) {
            container.addView(mAppInfos.get(position).getViewHoder().view, 0);
            return mAppInfos.get(position).getViewHoder().view;
        } else if (GlobalConfig.isUnlimitLoopRightSide == true && GlobalConfig.isUnlimitLoopBothSide == false) {
            ViewGroup parent = (ViewGroup) ((mAppInfos.get(position % mAppInfos.size()).getViewHoder().view).getParent());
            if (parent != null) {
                parent.removeView(mAppInfos.get(position % mAppInfos.size()).getViewHoder().view);
//				parent.removeAllViews();
            }
            container.addView(mAppInfos.get(position % mAppInfos.size()).getViewHoder().view, 0);
            return mAppInfos.get(position % mAppInfos.size()).getViewHoder().view;
        } else {
            ViewGroup parent = (ViewGroup) ((mAppInfos.get(position % mAppInfos.size()).getViewHoder().view).getParent());
            if (parent != null) {
                parent.removeView(mAppInfos.get(position % mAppInfos.size()).getViewHoder().view);
//				parent.removeAllViews();
            }
            container.addView(mAppInfos.get(position % mAppInfos.size()).getViewHoder().view, 0);
            return mAppInfos.get(position % mAppInfos.size()).getViewHoder().view;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setAppInfos(List<AppInfo> mAppInfos) {
        this.mAppInfos = mAppInfos;
    }

}
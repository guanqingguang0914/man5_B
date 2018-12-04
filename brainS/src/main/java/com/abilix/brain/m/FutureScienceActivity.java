package com.abilix.brain.m;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abilix.brain.Application;
import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;
import com.abilix.brain.ui.ClipViewPager;
import com.abilix.brain.ui.ScalePageTransformer;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 未来科学站页面
 *
 * @author yhd
 */
public class FutureScienceActivity extends Activity {
    private ClipViewPager mViewPager;
    private ImageView mTextView;
    private final static int[] InfoName = {R.string.m_myfamily, R.string.m_findfruit, R.string.m_findstationery};
    private final static int[] image = {R.drawable.m_science_family, R.drawable.m_science_fruit, R.drawable.m_science_stationery};
    private ViewHoder[] mViewHoders = new ViewHoder[InfoName.length];
    private ViewPagerAdapter mAdapter;
    private Typeface mFromAsset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.getInstance().setOrientation(this);
        setContentView(R.layout.activity_m_future);
        mViewPager = (ClipViewPager) findViewById(R.id.m_activity_viewpager);
        findViewById(R.id.page_container).setOnTouchListener(
                new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return mViewPager.dispatchTouchEvent(event);
                    }
                });

        mFromAsset = Typeface.createFromAsset(getAssets(), "fonts/m_fzxx.ttf");
        mTextView = (ImageView) findViewById(R.id.m_activity_back);
        mTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });
        initView();
    }

    private void initView() {
        for (int i = 0; i < InfoName.length; i++) {
            View childview = LayoutInflater.from(this).inflate(
                    R.layout.m_activity_item, null);
            ViewHoder vh = new ViewHoder();
            vh.view = childview;
            vh.ll = (LinearLayout) childview
                    .findViewById(R.id.m_item_ll);

            vh.imageView = (ImageView) childview
                    .findViewById(R.id.m_item_imageview);

            vh.textview = (TextView) childview
                    .findViewById(R.id.m_item_textview);
            vh.textview.setTypeface(mFromAsset);
            mViewHoders[i] = vh;
        }
        initDate();
    }

    private void initDate() {
        for (int i = 0; i < InfoName.length; i++) {
            final int j = i;
            ViewHoder vh = mViewHoders[i];
            vh.imageView.setBackgroundResource(image[i]);
            vh.textview.setText(getString(InfoName[i]));
            vh.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO yhd 点击进入未来科学站二级页面
                    switch (j) {
                        case 0:
                            if (Utils.isAppInstalled(FutureScienceActivity.this, GlobalConfig.APP_PKGNAME_MY_FAMILY)) {
                                Utils.openApp(FutureScienceActivity.this, GlobalConfig.APP_PKGNAME_MY_FAMILY ,null, false);
                                MUtils.sIsAppBack = true;
                                MUtils.sIsVoiceAppBack = false;
                            }
                            break;

                        case 1:
                            if (Utils.isAppInstalled(FutureScienceActivity.this, GlobalConfig.APP_PKGNAME_GUESSFRUIT)) {
                                Utils.openApp(FutureScienceActivity.this, GlobalConfig.APP_PKGNAME_GUESSFRUIT,null, false);
                                MUtils.sIsAppBack = true;
                                MUtils.sIsVoiceAppBack = false;
                            }
                            break;

                        case 2:
                            if (Utils.isAppInstalled(FutureScienceActivity.this, GlobalConfig.APP_PKGNAME_GUESSSTUDY)) {
                                Utils.openApp(FutureScienceActivity.this, GlobalConfig.APP_PKGNAME_GUESSSTUDY, null,false);
                                MUtils.sIsAppBack = true;
                                MUtils.sIsVoiceAppBack = false;
                            }
                            break;

                        default:

                            break;
                    }
                }
            });
        }
        mAdapter = new ViewPagerAdapter();
        // mViewPager.setCurrentItem(Integer.MAX_VALUE / 2 + 1);
        mViewPager.setOffscreenPageLimit(InfoName.length);
        // changeListener = new MyChangeListener();
        // mViewPager.setOnPageChangeListener(changeListener);
        mViewPager.setAdapter(mAdapter);
        // mViewPager.setCurrentItem(1);
        mViewPager.setPageTransformer(true, new ScalePageTransformer());
        // mViewPager.setPageMargin(-10);
    }


    private class ViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return InfoName.length;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            ((ViewPager) container).removeView(view);
            view = null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViewHoders[position].view;
            try {
                if (view.getParent() != null) {
                    container.removeView(view);
                }
                view.setTag(position);
                container.addView(view, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return view;
        }
    }

    private class ViewHoder {
        View view;
        LinearLayout ll;
        ImageView imageView;
        TextView textview;
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);

        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
            if (BrainService.getmBrainService() != null
                    && BrainService.getmBrainService().getMService() != null
                    && !Application.getInstance().isFirstStartApplication()
                    && MUtils.sIsAppBack
                    ) {


                if (MUtils.sIsVoiceAppBack) {
                    try {
                        BrainService.getmBrainService().getMService().handAction(MUtils.B_BACK_TO_BRAIN);
                        LogMgr.d("FZXX", "handAction(MUtils.B_BACK_TO_BRAIN)");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        BrainService.getmBrainService().getMService().handAction(MUtils.b_CLICK_BACK_TO_BRAIN);
                        LogMgr.d("FZXX", "handAction(MUtils.b_CLICK_BACK_TO_BRAIN)");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        MUtils.sIsAppBack = false;
        MUtils.sIsVoiceAppBack = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogMgr.d("FutureScienceActivity-->onDestroy()");
    }
}

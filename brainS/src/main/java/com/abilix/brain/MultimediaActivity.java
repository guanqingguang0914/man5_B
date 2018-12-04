package com.abilix.brain;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.abilix.brain.fragment.ManageFragment;
import com.abilix.brain.data.AppInfo;
import com.abilix.brain.ui.ClipViewPager;
import com.abilix.brain.ui.ScalePageTransformer;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 多媒体页面
 *
 * @author luox
 */
public class MultimediaActivity extends Activity {
    private ClipViewPager mViewPager;
    private Button mTextView;
    private final static int[] InfoName = {R.string.zhaopian, R.string.luyin};
    private final static int[] image = {R.drawable.zhaopian, R.drawable.luyin};
    private ViewHoder[] mViewHoders = new ViewHoder[InfoName.length];
    private ViewPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.getInstance().setOrientation(this);
        setContentView(R.layout.activity_multimedia);
        mViewPager = (ClipViewPager) findViewById(R.id.multimedia_activity_viewpager);
        findViewById(R.id.page_container).setOnTouchListener(
                new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return mViewPager.dispatchTouchEvent(event);
                    }
                });

        mTextView = (Button) findViewById(R.id.multimedia_activity_back);
        setImageView_Bg(mTextView, GlobalConfig.BRAIN_TYPE);
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
                    R.layout.multimedia_activity_item, null);
            ViewHoder vh = new ViewHoder();
            vh.view = childview;
            vh.imageView = (ImageView) childview
                    .findViewById(R.id.multimedia_activity_item_imageview_3);
            vh.imView = (ImageView) childview
                    .findViewById(R.id.multimedia_activity_item_imageview_2);
            vh.textview = (TextView) childview
                    .findViewById(R.id.multimedia_activity_item_textview);
            mViewHoders[i] = vh;
        }
        initDate();
    }

    private void initDate() {
        for (int i = 0; i < InfoName.length; i++) {
            final int j = i;
            ViewHoder vh = mViewHoders[i];
            vh.imageView.setBackgroundResource(image[i]);
            setImageView_Bg(vh.imView, GlobalConfig.BRAIN_TYPE);
            vh.textview.setText(getString(InfoName[i]));
            vh.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(ManageFragment.class, j);
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
        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C9) {
            ViewGroup.LayoutParams layoutParams = mViewPager.getLayoutParams();
            layoutParams.width = Utils.getWindowWidth(this) * 5 / 9;
            mViewPager.setLayoutParams(layoutParams);
        }
    }

    private void startActivity(Class<?> cls, int id) {
        Intent intent = new Intent(MultimediaActivity.this, cls);
        switch (id) {
            case 0:
                intent.putExtra("id", AppInfo.PAGE_TYPE_IMAGE);
                break;
            case 1:
                intent.putExtra("id", AppInfo.PAGE_TYPE_RECORD);
                break;
        }
        startActivity(intent);
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
        ImageView imageView;
        TextView textview;
        ImageView imView;
    }

    private void setImageView_Bg(View vh, int mode) {
        if (vh instanceof Button) {
            switch (mode) {
                case GlobalConfig.ROBOT_TYPE_C:
                case GlobalConfig.ROBOT_TYPE_C1:
                case GlobalConfig.ROBOT_TYPE_C9:
                case GlobalConfig.ROBOT_TYPE_CU:
                    vh.setBackgroundResource(R.drawable.back_selector_c);
                    break;
                case GlobalConfig.ROBOT_TYPE_M:
                case GlobalConfig.ROBOT_TYPE_M1:
                    vh.setBackgroundResource(R.drawable.back_selector_m);
                    break;
                case GlobalConfig.ROBOT_TYPE_H:
                case GlobalConfig.ROBOT_TYPE_H3:
                    vh.setBackgroundResource(R.drawable.back_selector_h);
                    break;
                case GlobalConfig.ROBOT_TYPE_AF:
                    vh.setBackgroundResource(R.drawable.back_selector_af);
                    break;
                case GlobalConfig.ROBOT_TYPE_F:
                    vh.setBackgroundResource(R.drawable.back_selector_f);
                    break;
                case GlobalConfig.ROBOT_TYPE_S:
                    vh.setBackgroundResource(R.drawable.back_selector_s);
                    break;
                default:
                    vh.setBackgroundResource(R.drawable.back_selector_c);
                    break;
            }
        } else if (vh instanceof ImageView) {
            switch (mode) {
                case GlobalConfig.ROBOT_TYPE_C:
                case GlobalConfig.ROBOT_TYPE_C1:
                case GlobalConfig.ROBOT_TYPE_CU:
                case GlobalConfig.ROBOT_TYPE_C9:
                    vh.setBackgroundResource(R.drawable.multimedia_bg_c);
                    break;
                case GlobalConfig.ROBOT_TYPE_M:
                case GlobalConfig.ROBOT_TYPE_M1:
                    vh.setBackgroundResource(R.drawable.multimedia_bg_m);
                    break;
                case GlobalConfig.ROBOT_TYPE_H:
                case GlobalConfig.ROBOT_TYPE_H3:
                    vh.setBackgroundResource(R.drawable.multimedia_bg_h);
                    break;
                case GlobalConfig.ROBOT_TYPE_F:
                    vh.setBackgroundResource(R.drawable.multimedia_bg_f);
                    break;
                case GlobalConfig.ROBOT_TYPE_AF:
                    vh.setBackgroundResource(R.drawable.multimedia_bg_af);
                    break;
                case GlobalConfig.ROBOT_TYPE_S:
                    vh.setBackgroundResource(R.drawable.multimedia_bg_c);
                    break;
            }
        }
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
        LogMgr.d("MultimediaActivity-->onDestroy()");
    }
}

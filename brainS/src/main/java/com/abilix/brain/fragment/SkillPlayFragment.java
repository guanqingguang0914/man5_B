package com.abilix.brain.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.LinearInterpolator;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;
import com.abilix.brain.data.AppInfo;
import com.abilix.brain.ui.BrainViewPager;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.Utils;

/**
 * SkillPaly Fragment
 *
 * @author luox
 */
public class SkillPlayFragment extends Fragment {
    private BrainViewPager mViewPager;
    private File mFile;
    private TextView mTextView;
    private View mView;
    private List<AppInfo> mAppInfos = new ArrayList<AppInfo>();
    private List<ViewHoder> mViews = new LinkedList<ViewHoder>();
    private ManageFragment manageFragment;
    private ViewPagerAdapter mAdapter;
    private AppInfo mCurrentAppInfo = null;
    private AnimatorSet mAnimatorSet = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        manageFragment = (ManageFragment) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFile = new File(BrainUtils.ABILIX + BrainUtils.ABILIX_SKILLPLAYER);
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.skillplay_fragment, null);
        mViewPager = (BrainViewPager) mView
                .findViewById(R.id.skillplay_viewpager);
        mTextView = (TextView) mView.findViewById(R.id.skillplay_textview);
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        for (int i = 0; i < mAppInfos.size(); i++) {
            View childview = LayoutInflater.from(manageFragment).inflate(
                    R.layout.skillplay_fragment_item, null);
            ViewHoder vh = new ViewHoder();
            vh.view = childview;
            vh.imageview = (ImageView) childview
                    .findViewById(R.id.skillplay_imageview_qr);
            vh.textview = (TextView) childview
                    .findViewById(R.id.skillplay_textview);
            if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M
                    || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M1) {
                vh.imageview.setImageResource(R.drawable.multimedia_bg_m);
            } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C
                    || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C1 || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_CU
                    || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_C9) {
                vh.imageview.setImageResource(R.drawable.multimedia_bg_c);
            } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_F) {
                vh.imageview.setImageResource(R.drawable.multimedia_bg_f);
            } else if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_AF) {
                vh.imageview.setImageResource(R.drawable.multimedia_bg_af);
            } else if( GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H || GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_H3) {
                vh.imageview.setImageResource(R.drawable.multimedia_bg_h);
            }
            vh.textview.setText(mAppInfos.get(i).getName());
            mViews.add(vh);
        }
        initView();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAnimaAndFunc();
        mCurrentAppInfo = null;
        mViews.clear();
        mAdapter = null;
        mAppInfos.clear();
    }

    private void initView() {
        if (mAppInfos.size() > 0) {
            mAdapter = null;
            mTextView.setVisibility(View.GONE);
            mViewPager.setVisibility(View.VISIBLE);
            for (int i = 0; i < mViews.size(); i++) {
                final int j = i;
                mViews.get(i).view
                        .setOnLongClickListener(new OnLongClickListener() {

                            @Override
                            public boolean onLongClick(View v) {
                                if(mCurrentAppInfo == mAppInfos.get(j)){
                                    LogMgr.e("当前技能正在播放，不可删除");
                                    return true;
                                }
                                File file_parent = new File(BrainUtils.ROBOTINFO);
                                if (file_parent.exists()) {
                                    String is = FileUtils.readFile(file_parent);
                                    if (!is.contains("true")) {
                                        delViewPager(mAppInfos.get(j), j);
                                    } else {
                                        // 家长模式下无法删除
                                        Utils.showSingleButtonDialog(getActivity(), getString(R.string.tishi), getString(R.string.shezhibrain),
                                                getString(R.string.queren), false, new OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mViewPager.setScrollble(true);
                                                    }
                                                });
//										AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
//										dialog.setTitle(getString(R.string.tishi));
//										dialog.setMessage(getString(R.string.shezhibrain));
//										dialog.setCancelable(false);
//										dialog.setPositiveButton(getString(R.string.queren),
//												new DialogInterface.OnClickListener() {
//													@Override
//													public void onClick(DialogInterface dialog, int which) {
//														mViewPager.setScrollble(true);
//													}
//												});
//										dialog.show();
                                    }
                                } else {
                                    delViewPager(mAppInfos.get(j), j);
                                }
                                return true;
                            }
                        });
                mViews.get(i).view.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mAppInfos.get(j) == mCurrentAppInfo) {
                            stopAnimaAndFunc();
                        } else {
                            startAnimaAndFunc(mViews.get(j).imageview,
                                    mAppInfos.get(j));
                        }
                    }
                });
            }
            mAdapter = new ViewPagerAdapter();
            mViewPager.setAdapter(mAdapter);
        } else {
            mTextView.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.GONE);
        }
    }

    private void initData() {
        if (!mFile.exists()) {
            return;
        }
        File[] files = mFile.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    LogMgr.d("moveName = " + file.listFiles().length);
                    if (!file.isDirectory()) {
                        continue;
                    }

                    if (file.listFiles().length <= 0) {
                        continue;
                    }

                    AppInfo appInfo = new AppInfo();
                    String moveName = FileUtils.getSkillplayerMoveName(file);
                    LogMgr.d("moveName = " + moveName);
                    if (TextUtils.isEmpty(moveName)) {
                        LogMgr.e("获取skillplayer 名字失败0");
                        continue;
                    } else {
                        LogMgr.d("获取skillplayer 名字成功 moveName = " + moveName);
                        appInfo.setName(moveName);
                        appInfo.setPathName(file.getAbsolutePath().trim());
                        appInfo.setPageType(AppInfo.PAGE_TYPE_FILE);
                        appInfo.setFileType(AppInfo.FILE_TYPE_SKILLPLAYER);
                        mAppInfos.add(appInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private class ViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mViews.size();
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
            View view = mViews.get(position).view;
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
        ImageView imageview;
        TextView textview;
    }

    /**
     * 删除页面
     */
    public void delViewPager(final AppInfo appInfo, final int i) {
        Utils.showTwoButtonDialog(getActivity(), getString(R.string.tishi), getString(R.string.delete),
                getString(R.string.cancel), getString(R.string.determine), false, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setScrollble(true);
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (appInfo == mCurrentAppInfo) {
                            stopAnimaAndFunc();
                        }
                        FileUtils.deleteDirectory(appInfo.getPathName(), true);
                        mAppInfos.remove(i);
                        mViews.remove(i);
                        initView();
                        if (i < mAppInfos.size()) {
                            mViewPager.setCurrentItem(i, false);
                        } else {
                            mViewPager.setCurrentItem(0, false);
                        }
                        mViewPager.setScrollble(true);
                    }
                });
//		TextView tv = new TextView(manageFragment);
//		tv.setText(getString(R.string.delete));
//		tv.setGravity(Gravity.CENTER);
//		tv.setTextSize(24);
//		mViewPager.setScrollble(false);
//		AlertDialog alertDialog = new AlertDialog.Builder(manageFragment)
//				.setView(tv)
//				.setPositiveButton(getString(R.string.determine),
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								if (appInfo == mCurrentAppInfo) {
//									stopAnimaAndFunc();
//								}
//								FileUtils.deleteDirectory(appInfo.getPathName());
//								mAppInfos.remove(i);
//								mViews.remove(i);
//								initView();
//								if (i < mAppInfos.size()) {
//									mViewPager.setCurrentItem(i, false);
//								} else {
//									mViewPager.setCurrentItem(0, false);
//								}
//								mViewPager.setScrollble(true);
//							}
//						})
//				.setNegativeButton(getString(R.string.cancel),
//						new DialogInterface.OnClickListener() {
//
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								mViewPager.setScrollble(true);
//							}
//						}).show();
//		alertDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 开启页面动画及功能
     *
     * @param view
     */
    private void startAnimaAndFunc(ImageView view, AppInfo appInfo) {
        LogMgr.i("startAnimaAndFunc() " + appInfo.getName());
        if (appInfo != mCurrentAppInfo) {
            stopAnimaAndFunc();
        }
        ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", 0f,
                360f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.8f,
                1f);
        alpha.setRepeatCount(-1);
        rotate.setRepeatCount(-1);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.setInterpolator(new LinearInterpolator());
        mAnimatorSet.play(rotate).with(alpha);
        mAnimatorSet.setDuration(8000);
        mAnimatorSet.start();

        mCurrentAppInfo = appInfo;
        BrainService.getmBrainService().exeFilePageFunc(
                mCurrentAppInfo.getPathName(),
                GlobalConfig.CONTROL_CALLBACKMODE_SKILLPLAYER_CMD, 1);
    }

    /**
     * 关闭页面动画及功能
     */
    private void stopAnimaAndFunc() {
        BrainService.getmBrainService().exeFilePageFunc(null,
                GlobalConfig.CONTROL_CALLBACKMODE_SKILLPLAYER_CMD, 0);
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet.end();
            mAnimatorSet = null;
        }
        mCurrentAppInfo = null;
    }

}

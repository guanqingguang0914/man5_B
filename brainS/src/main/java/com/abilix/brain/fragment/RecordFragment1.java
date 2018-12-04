package com.abilix.brain.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;
import com.abilix.brain.ui.BrainRecord;
import com.abilix.brain.ui.BrainViewPager;
import com.abilix.brain.ui.MyViewPager;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.Utils;

/**
 * 录音 Fragment
 *
 * @author luox
 */
public class RecordFragment1 extends Fragment {
    private BrainViewPager mViewPager;
    private ManageFragment manageFragment;
    private View mView;
    private File mFile;
    private List<Mode> mDatas = new ArrayList<Mode>();
    private List<ViewHoder> mHoders = new ArrayList<ViewHoder>();
    private ViewPagerAdapter myBaseAdapter;
    private TextView mTextView;

    public List<Mode> getmDatas() {
        return mDatas;
    }

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
        mFile = new File(BrainRecord.RECORD_SCRATCH_VJC_);
        initDate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.record_fragment_1, null);
        mViewPager = (BrainViewPager) mView
                .findViewById(R.id.record_fragment_viewpager);
        mTextView = (TextView) mView.findViewById(R.id.record_textview);
        myBaseAdapter = new ViewPagerAdapter();
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
        mDatas.clear();
        mDatas = null;
        myBaseAdapter = null;
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
        for (int i = 0; i < mDatas.size(); i++) {
            View view = LayoutInflater.from(manageFragment).inflate(
                    R.layout.record_fragment_item_1, null);
            ViewHoder viewhoder = new ViewHoder();
            viewhoder.view = view;
            viewhoder.imageview_1 = (ImageView) view
                    .findViewById(R.id.record_fragment_item_imageview_2);
            viewhoder.imageview_2 = (ImageView) view
                    .findViewById(R.id.record_fragment_item_imageview_3);
            viewhoder.textview = (TextView) view
                    .findViewById(R.id.record_fragment_item_textview);
            setImageView(viewhoder.imageview_1);
            viewhoder.textview.setText(getString(R.string.luyin)
                    + mDatas.get(i).name);
            mHoders.add(viewhoder);
        }
        initView();
    }

    private void setImageView(ImageView vh) {
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C:
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_C9:
            case GlobalConfig.ROBOT_TYPE_CU:
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

    @Override
    public void onStop() {
        super.onStop();
        stopPlayRecord();
    }

    private class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mHoders.size();
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
            View view = mHoders.get(position).view;
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

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    private void initView() {
        if (mHoders.size() > 0) {
            mTextView.setVisibility(View.GONE);
            mViewPager.setVisibility(View.VISIBLE);
            myBaseAdapter = null;
            myBaseAdapter = new ViewPagerAdapter();
            for (int i = 0; i < mHoders.size(); i++) {
                final int j = i;
                mHoders.get(i).view
                        .setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                if(mPlayer != null && mPlayer.isPlaying()){
                                    LogMgr.w("当前正在播放录音，长按删除不执行。");
                                    return true;
                                }
                                mViewPager.setScrollble(false);
                                File file_parent = new File(BrainUtils.ROBOTINFO);
                                if (file_parent.exists()) {
                                    String is = FileUtils.readFile(file_parent);
                                    if (!is.contains("true")) {
                                        delViewPager(mDatas.get(j).name, j);
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
                                    delViewPager(mDatas.get(j).name, j);
                                }


                                return true;
                            }
                        });
                mHoders.get(i).view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPlay(j, mHoders.get(j));
                    }
                });
            }
            mViewPager.setAdapter(myBaseAdapter);
            mViewPager.setPageMargin(-10);
            mViewPager.setOffscreenPageLimit(3);
            manageFragment.mTextViewM.setText(mDatas.get(0).shijian);
            mViewPager.setOnPageChangeListener(new BrainPageChangeListener());
        } else {
            manageFragment.mTextViewM.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.GONE);
        }
    }

    private ViewHoder mViewHoder;
    private String itemName;
    private int mCurrent;

    private void isPlay(int j, ViewHoder vh) {
        LogMgr.e("start j = "+j+" vh.textview.getText().toString() = "+vh.textview.getText().toString()+" itemName = "+itemName+" mCurrent = "+mCurrent + " mPlayer == null is "+(mPlayer == null));
        if(mPlayer != null){
            LogMgr.e("mPlayer.isPlaying() is "+mPlayer.isPlaying());
        }
        if (TextUtils.equals(vh.textview.getText().toString(), itemName)) {
            if (mPlayer != null) {
                if (mPlayer.isPlaying()) {
                    stopTimer();
                    updateUiStart(j, vh);
                    mPlayer.pause();
                } else {
                    startTimer();
                    updateUiStop(j, vh);
                    mPlayer.start();
                }
            } else {
                updateUiStop(j, vh);
                playRecord(j, vh,
                        BrainRecord.RECORD_SCRATCH_VJC_ + mDatas.get(j).name
                                + BrainRecord.RECORD_PATH_3GP);
            }
        } else {
            if (mViewHoder != null) {
                updateUiStart(mCurrent, mViewHoder);
            }
            updateUiStop(j, vh);
            playRecord(j, vh, BrainRecord.RECORD_SCRATCH_VJC_
                    + mDatas.get(j).name + BrainRecord.RECORD_PATH_3GP);
        }
        itemName = vh.textview.getText().toString();
        mViewHoder = vh;
        mCurrent = j;
    }

    private void updateUiStop(int j, ViewHoder vh) {
        vh.imageview_2.setImageResource(R.drawable.luyin_stop);
        if (j < mHoders.size()) {
            mHoders.set(j, vh);
            if (myBaseAdapter != null) {
                myBaseAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateUiStart(int j, ViewHoder vh) {
        vh.imageview_2.setImageResource(R.drawable.luyin_start);
        if (j < mHoders.size()) {
            mHoders.set(j, vh);
            if (myBaseAdapter != null) {
                myBaseAdapter.notifyDataSetChanged();
            }
        }
    }

    private Timer mTimer;
    private TimerTask mTimerTask;

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    private int mTime = 0;

    private void startTimer() {
        stopTimer();
//		Message.obtain(mHandler, 0, mTime, 0).sendToTarget();
        mTimer = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                if (mPlayer != null) {
                    mTime = mPlayer.getCurrentPosition() / 1000;
//					mTime++;
                    LogMgr.v("mTime = " + mTime);
                    Message.obtain(mHandler, 0, mTime, 0).sendToTarget();
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 100);
    }

    static class ViewHoder {
        View view;
        ImageView imageview_1;
        ImageView imageview_2;
        TextView textview;
    }

    private class Mode {
        public Mode(Integer name, String shijian) {
            this.name = name;
            this.shijian = shijian;
        }

        private Integer name;
        private String shijian;
    }

    /**
     * 删除页面
     */
    public void delViewPager(final int i, final int position) {
        Utils.showTwoButtonDialog(getActivity(), getString(R.string.tishi), getString(R.string.delete),
                getString(R.string.cancel), getString(R.string.determine), false, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setScrollble(true);
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setScrollble(true);
                        mViewHoder = null;
                        stopPlayRecord();
                        File file = new File(
                                BrainRecord.RECORD_SCRATCH_VJC_ + i
                                        + BrainRecord.RECORD_PATH_3GP);
                        LogMgr.e("file:" + file.getName());
                        if (file != null) {
                            if (file.exists()) {
                                file.delete();
                            }
                            mDatas.remove(position);
                            mHoders.remove(position);
                            initView();
                            if (position < mDatas.size()) {
                                mViewPager.setCurrentItem(position,
                                        false);
                            } else {
                                mViewPager.setCurrentItem(0, false);
                            }
                        }
                    }
                });
//		TextView tv = new TextView(manageFragment);
//		tv.setText(getString(R.string.delete));
//		tv.setGravity(Gravity.CENTER);
//		tv.setTextSize(24);
//		AlertDialog alertDialog = new AlertDialog.Builder(manageFragment)
//				.setView(tv)
//				.setPositiveButton(getString(R.string.determine),
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								mViewPager.setScrollble(true);
//								mViewHoder = null;
//								stopPlayRecord();
//								File file = new File(
//										BrainRecord.RECORD_SCRATCH_VJC_ + i
//												+ BrainRecord.RECORD_PATH_3GP);
//								LogMgr.e("file:" + file.getName());
//								if (file != null) {
//									if (file.exists()) {
//										file.delete();
//									}
//									mDatas.remove(position);
//									mHoders.remove(position);
//									initView();
//									if (position < mDatas.size()) {
//										mViewPager.setCurrentItem(position,
//												false);
//									} else {
//										mViewPager.setCurrentItem(0, false);
//									}
//								}
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

    private class ComparatorR implements Comparator<Object> {
        @Override
        public int compare(Object lhs, Object rhs) {
            Mode m1 = (Mode) lhs;
            Mode m2 = (Mode) rhs;
            return m1.name.compareTo(m2.name);
        }
    }

    volatile private MediaPlayer mPlayer = null;

    /**
     * 播放录音
     */
    private void playRecord(final int j, final ViewHoder vh, String name) {
        if (mPlayer != null) {
            stopPlayRecord();
        }
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(name);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer.start();
                    startTimer();
                }
            });
            mPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    updateUiStart(j, vh);
                    stopPlayRecord();
                    manageFragment.mTextViewM.setText(mDatas.get(j).shijian);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlayRecord() {
        stopTimer();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        mTime = 0;
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                manageFragment.mTextViewM.setText(SetTime(msg.arg1));
            }
        }

        ;
    };

    private void initDate() {
        File[] names = mFile.listFiles();
        if (names.length > 0) {
            mDatas.clear();
            for (File name : names) {
                if (name.getName().endsWith(BrainRecord.RECORD_PATH_3GP)) {
                    try {
                        MediaPlayer mp = MediaPlayer.create(manageFragment,
                                Uri.fromFile(name));
                        int time = (int) Math
                                .round((float) mp.getDuration() / 1000);
                        mp = null;
                        String str = SetTime(time);
                        mDatas.add(new Mode(Integer.parseInt(name.getName()
                                .substring(0, name.getName().indexOf("."))),
                                str));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            Collections.sort(mDatas, new ComparatorR());
        }
    }

    private String SetTime(int time) {
        StringBuffer stime = new StringBuffer();
        if (time == 0) {
//            return "00:00:00";
            return "00:00";
        } else {
            int miao = time % 60;
            int fen = time >= 3600 ? time % 3600 / 60 : time / 60;

            // int xiao = time / 3600;
            // stime.append(xiao > 0 ? xiao >= 10 ? xiao : "0" + xiao : "00")
            // .append(":")
            // .append(fen > 0 ? fen >= 10 ? fen : "0" + fen : "00")
            // .append(":")
            // .append(miao > 0 ? miao >= 10 ? miao : "0" + miao : "00");

//            stime.append("00:")
            stime.append(fen > 0 ? fen >= 10 ? fen : "0" + fen : "00")
                    .append(":")
                    .append(miao > 0 ? miao >= 10 ? miao : "0" + miao : "00");
            return stime.toString();
        }
    }

    /**
     * BrainPageChangeListener
     *
     * @author luox
     */
    class BrainPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (mViewHoder != null) {
                updateUiStart(mCurrent, mViewHoder);
            }
            stopTimer();
            stopPlayRecord();
            manageFragment.mTextViewM.setText(mDatas.get(position).shijian);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}

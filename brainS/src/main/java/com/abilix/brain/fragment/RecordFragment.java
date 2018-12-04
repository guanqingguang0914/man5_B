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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abilix.brain.R;
import com.abilix.brain.ui.BrainRecord;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.Utils;

/**
 * 录音 Fragment，未被使用。
 *
 * @author luox
 */
public class RecordFragment extends Fragment {
    private ListView mListView;
    private ManageFragment manageFragment;
    private View mView;
    private File mFile;
    private List<Mode> mDatas = new ArrayList<Mode>();
    private MyBaseAdapter myBaseAdapter;
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
        mView = inflater.inflate(R.layout.record_fragment, null);
        mListView = (ListView) mView.findViewById(R.id.record_listview);
        mTextView = (TextView) mView.findViewById(R.id.record_textview);
        myBaseAdapter = new MyBaseAdapter();
        mListView.setAdapter(myBaseAdapter);
        // mListView.setOnItemClickListener(new OnItemClickListener() {
        //
        // @Override
        // public void onItemClick(AdapterView<?> parent, View view,
        // int position, long id) {
        // playRecord(BrainRecord.RECORD_SCRATCH_VJC_
        // + mDatas.get(position).name
        // + BrainRecord.RECORD_PATH_3GP);
        // }
        // });
        // mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
        //
        // @Override
        // public boolean onItemLongClick(AdapterView<?> parent, View view,
        // int position, long id) {
        // delViewPager(mDatas.get(position).name, position);
        // return true;
        // }
        // });

        if (mDatas.size() == 0) {
            mListView.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
        }
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
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPlayRecord();
    }

    private class MyBaseAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ViewHoder viewhoder = null;
            // if (convertView == null) {
            convertView = LayoutInflater.from(manageFragment).inflate(
                    R.layout.record_fragment_item_1, null);
            viewhoder = new ViewHoder();
            viewhoder.textview = (TextView) convertView
                    .findViewById(R.id.record_fragmnet_item_textview);
            viewhoder.textview_1 = (TextView) convertView
                    .findViewById(R.id.record_fragmnet_item_textview_1);
            viewhoder.button = (Button) convertView
                    .findViewById(R.id.record_fragmnet_item_button);
            viewhoder.progressBar = (ProgressBar) convertView
                    .findViewById(R.id.record_fragmnet_item_progressbar);
            convertView.setTag(viewhoder);
            // } else {
            // viewhoder = (ViewHoder) convertView.getTag();
            // }
            viewhoder.textview.setText(getString(R.string.luyin)
                    + mDatas.get(position).name);
            viewhoder.textview_1.setText(mDatas.get(position).shijian);
            viewhoder.button.setOnClickListener(new MyOnClickListener(position,
                    viewhoder));
            convertView.setOnLongClickListener(new myOnLongClickListener(
                    position));
            return convertView;
        }

        public void updataView(ViewHoder viewhoder, int currentPosition,
                               int duration) {
            viewhoder.button.setText("播放");
            viewhoder.progressBar.setMax(duration);
            viewhoder.progressBar.setProgress(currentPosition);
            viewhoder.progressBar.setVisibility(View.VISIBLE);
            if (scurrentPosition == currentPosition) {
                recovery(viewhoder);
                stopPlayRecord();
            }
            scurrentPosition = currentPosition;
        }
    }

    String itemName;
    ViewHoder mViewHoder;
    private int duration;
    private Timer timer;
    private int scurrentPosition = -1;

    class MyOnClickListener implements OnClickListener {
        int position;
        ViewHoder vh;

        public MyOnClickListener(int position, ViewHoder vh) {
            this.position = position;
            this.vh = vh;
        }

        @Override
        public void onClick(View v) {
            // View view = mListView.getChildAt(position);
            // LogMgr.e("position:" + position + " view:" + (view == null));
            // ViewHoder vh = (ViewHoder) view.getTag();
            if (vh != null) {
                if (TextUtils
                        .equals(vh.textview.getText().toString(), itemName)) {
                    if (mPlayer != null) {
                        if (mPlayer.isPlaying()) {
                            stopTimer();
                            mPlayer.pause();
                            vh.button.setText("暂停");
                        } else {
                            startTimer(vh);
                            mPlayer.start();
                            vh.button.setText("播放");
                        }
                    } else {
                        playRecord(
                                vh,
                                BrainRecord.RECORD_SCRATCH_VJC_
                                        + mDatas.get(position).name
                                        + BrainRecord.RECORD_PATH_3GP);
                    }
                } else {
                    if (mViewHoder != null) {
                        recovery(mViewHoder);
                    }
                    playRecord(
                            vh,
                            BrainRecord.RECORD_SCRATCH_VJC_
                                    + mDatas.get(position).name
                                    + BrainRecord.RECORD_PATH_3GP);
                }
                mViewHoder = vh;
                itemName = vh.textview.getText().toString();
            }
        }
    }

    class myOnLongClickListener implements OnLongClickListener {
        int position;

        public myOnLongClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onLongClick(View v) {
            LogMgr.i("onLongClick()");
            File file_parent = new File(BrainUtils.ROBOTINFO);
            if (file_parent.exists()) {
                String is = FileUtils.readFile(file_parent);
                if (!is.contains("true")) {
                    delViewPager(mDatas.get(position).name, position);
                } else {
                    // 家长模式下无法删除
                    Utils.showSingleButtonDialog(getActivity(), getString(R.string.tishi), getString(R.string.shezhibrain),
                            getString(R.string.queren), false, new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //do nothing
                                }
                            });
//					AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
//					dialog.setTitle(getString(R.string.tishi));
//					dialog.setMessage(getString(R.string.shezhibrain));
//					dialog.setCancelable(false);
//					dialog.setPositiveButton(getString(R.string.queren),
//							new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									dialog.dismiss();
//								}
//							});
//					dialog.show();
                }
            } else {
                delViewPager(mDatas.get(position).name, position);
            }

            return true;
        }

    }

    private void recovery(ViewHoder viewhoder) {
        viewhoder.button.setText("播放");
        viewhoder.progressBar.setProgress(0);
        viewhoder.progressBar.setVisibility(View.GONE);
    }

    static class ViewHoder {
        ImageView imageview;
        TextView textview;
        TextView textview_1;
        Button button;
        ProgressBar progressBar;
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
                        //do nothing
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopPlayRecord();
                        File file = new File(
                                BrainRecord.RECORD_SCRATCH_VJC_ + i
                                        + BrainRecord.RECORD_PATH_3GP);
                        if (file != null) {
                            if (file.exists()) {
                                file.delete();
                            }
                            mDatas.remove(position);
                            if (mDatas.size() > 0) {
                                mTextView.setVisibility(View.GONE);
                                mListView.setVisibility(View.VISIBLE);
                                myBaseAdapter.notifyDataSetChanged();
                            } else {
                                mListView.setVisibility(View.GONE);
                                mTextView.setVisibility(View.VISIBLE);
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
//								stopPlayRecord();
//								File file = new File(
//										BrainRecord.RECORD_SCRATCH_VJC_ + i
//												+ BrainRecord.RECORD_PATH_3GP);
//								if (file != null) {
//									if (file.exists()) {
//										file.delete();
//									}
//									mDatas.remove(position);
//									if (mDatas.size() > 0) {
//										mTextView.setVisibility(View.GONE);
//										mListView.setVisibility(View.VISIBLE);
//										myBaseAdapter.notifyDataSetChanged();
//									} else {
//										mListView.setVisibility(View.GONE);
//										mTextView.setVisibility(View.VISIBLE);
//									}
//								}
//							}
//						}).setNegativeButton(getString(R.string.cancel), null)
//				.show();
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

    private MediaPlayer mPlayer = null;

    // private boolean ispause;

    /**
     * 播放录音
     */
    private void playRecord(ViewHoder viewhoder, String name) {
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
                    duration = mPlayer.getDuration();
                }
            });
            startTimer(viewhoder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlayRecord() {
        // ispause = false;
        mViewHoder = null;
        stopTimer();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                myBaseAdapter.updataView((ViewHoder) msg.obj,
                        mPlayer.getCurrentPosition(), duration);
            }
        }

        ;
    };

    private void startTimer(final ViewHoder viewhoder) {
        stopTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mPlayer != null) {
                    Message.obtain(mHandler, 0, viewhoder).sendToTarget();
                }
            }
        }, 0, 300);
    }

    private void initDate() {
        File[] names = mFile.listFiles();
        if (names.length > 0) {
            for (File name : names) {
                if (name.getName().endsWith(BrainRecord.RECORD_PATH_3GP)) {
                    try {
                        MediaPlayer mp = MediaPlayer.create(manageFragment,
                                Uri.fromFile(name));
                        int time = (int) Math
                                .round((float) mp.getDuration() / 1000);
                        mp = null;
                        StringBuffer stime = new StringBuffer();
                        int miao = time % 60;
                        int fen = time >= 3600 ? time % 3600 / 60 : time / 60;
                        int xiao = time / 3600;
                        stime.append(
                                xiao > 0 ? xiao > 10 ? xiao : "0" + xiao : "00")
                                .append(":")
                                .append(fen > 0 ? fen > 10 ? fen : "0" + fen
                                        : "00")
                                .append(":")
                                .append(miao > 0 ? miao > 10 ? miao : "0"
                                        + miao : "00");
                        mDatas.add(new Mode(Integer.parseInt(name.getName()
                                .substring(0, name.getName().indexOf("."))),
                                stime.toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            Collections.sort(mDatas, new ComparatorR());
        }
    }
}

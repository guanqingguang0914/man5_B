package com.abilix.brain.ui;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;

/**
 * Brain 录音功能类
 *
 * @author luox
 */
public class BrainRecord implements OnCompletionListener {
    public static final String TAG = BrainRecord.class.getSimpleName();
    // 认识机器人录音
    public static final String RECORD_PATH = FileUtils.DATA_PATH
            + "/knowthe_record.3gp";
    // vjc scratch 录音
    // public static final String RECORD_PATH_ = FileUtils.DATA_PATH
    // + "/m_record.3gp";
    public static final String RECORD_SCRATCH_VJC_ = FileUtils.DATA_PATH
            + File.separator + "Record" + File.separator;
    public static final String RECORD_PATH_3GP = ".3gp";
    public static int MIN_SOUND = 70;
    private double maxSound = 0;
    private MediaRecorder mMediaRecorder;
    private static boolean isRecording = false;
    private MediaPlayer mPlayer;
    private IRecordStateListener mRecordStateListener;
    private IPlayStateListener mPlayStateListener;
    private double db = 0;
    private long speakFinishTime = 0;
    private long recordStartTime = 0;
    private int mode = -1;
    private String recordFileName;

    /**
     * @param mode 1 认识机器人 2 vjc scratch
     */
    public BrainRecord(int mode, int id) {
        this.mode = mode;
        LogMgr.d("initail brainRecorder");
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        if (mode == 1) {
            mMediaRecorder.setOutputFile(RECORD_PATH);
        } else if (mode == 2 && id != -1) {
            // File fileRecord = new File(BrainRecord.RECORD_SCRATCH_VJC_);
            // if (!fileRecord.exists()) {
            // fileRecord.mkdir();
            // }
            // fileRecord = null;
            recordFileName = RECORD_SCRATCH_VJC_ + id + RECORD_PATH_3GP;
            mMediaRecorder.setOutputFile(recordFileName);
        }
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            LogMgr.e("brainRecord initial error::" + e);
            e.printStackTrace();
        }

        // if(GlobalConfig.BRAIN_TYPE == 3){
        // MIN_SOUND = 90;
        // }

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                stopRecord();
            }
        }

        ;
    };

    public synchronized void startRecord(
            IRecordStateListener recordStateListener, final long time) {
        LogMgr.d(TAG, "startRecord()");
        if (!isRecording) {
            mMediaRecorder.start();
            recordStartTime = System.currentTimeMillis();
            isRecording = true;
        }
        this.mRecordStateListener = recordStateListener;
        if (!GlobalConfig.isUsingRecordStopCmd) {
            updateMicStatus();
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true && isRecording) {
                    LogMgr.d(TAG, "录音中 this  = " + BrainRecord.this);
//					if (db > maxSound) {
//						maxSound = db;
//					}
                    if (mode != 2) {
                        if (GlobalConfig.isUsingRecordStopCmd) {
                            LogMgr.d(TAG, "录音中 System.currentTimeMillis() - recordStartTime = " + (System.currentTimeMillis() - recordStartTime) / 1000 + " isRecording = " + isRecording);
                            if (System.currentTimeMillis() - recordStartTime > 60 * 1000) {
                                mHandler.sendEmptyMessage(0);
                            }
                        } else {
                            if (db < MIN_SOUND) {
                                if (speakFinishTime == 0) {
                                    speakFinishTime = System.currentTimeMillis();
                                }
                                if (System.currentTimeMillis() - speakFinishTime > time) {
                                    mHandler.sendEmptyMessage(0);
                                }
                            } else {
                                speakFinishTime = 0;
                            }
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

    }

    public synchronized void stopRecord() {
        try {
            LogMgr.d(TAG, "stopRecord()");
            LogMgr.d("mMediaRecorder != null is " + (mMediaRecorder != null) + " isRecording = " + isRecording + " this = " + BrainRecord.this);
            if (mMediaRecorder != null & isRecording) {
                if (mode == 1 && System.currentTimeMillis() - recordStartTime < 1 * 1000) {
                    LogMgr.i("认识机器人录音时间过短，不允许停止");
                    return;
                }
                isRecording = false;
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mRecordStateListener.onStopRecord();
                mMediaRecorder = null;

                checkAudioFile();
            }
        } catch (Exception e) {
            LogMgr.e("isRecording = " + isRecording);
            e.printStackTrace();
        }
    }

    public synchronized void playMedia(IPlayStateListener playStateListener) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            try {
                if (mode != -1) {
                    if (mode == 1) {
                        mPlayer.setDataSource(RECORD_PATH);
                    } else if (mode == 2) {
                        if (recordFileName != null) {
                            mPlayer.setDataSource(recordFileName);
                        }
                    }
                }
                mPlayer.prepare();
                mPlayer.setOnCompletionListener(this);
            } catch (Exception e) {
                LogMgr.e("record mediaplayer initial error::" + e);
                e.printStackTrace();
            }
        }
        this.mPlayStateListener = playStateListener;
        mPlayer.start();
    }

    public synchronized void stopPlayMedia() {
        mPlayer.release();
        mPlayer = null;
    }

    public synchronized void destory() {
        stopRecord();
        isRecording = false;
        recordFileName = null;
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void updateMicStatus() {
        LogMgr.i("updateMicStatus()");
        if (!isRecording) {
            return;
        }
        if (mMediaRecorder != null) {
            double ratio = (double) mMediaRecorder.getMaxAmplitude();
            LogMgr.d(TAG, "ratio1 = " + ratio);
            if (ratio > 1) {
                LogMgr.d(TAG, "ratio2 = " + ratio);
                db = 20 * Math.log10(ratio);
                // Log.e(TAG, "sound db::" + db);
            }
        }
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    updateMicStatus();
                }
            }, 100);
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.seekTo(0);
        mPlayStateListener.onFinished();
    }

    private void checkAudioFile() {
        MediaPlayer mediaPlayer;
        String path;
        int duration;
        if (mode == 1) {
            path = RECORD_PATH;
        } else if (mode == 2) {
            if (recordFileName == null) {
                return;
            }
            path = recordFileName;
        } else {
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
            File audioFile = new File(path);
            boolean isDeleted = false;
            if (duration < 500 && audioFile.exists()) {
                isDeleted = audioFile.delete();
            }
            LogMgr.e(String.format("checkAudioFile duration == %d isDeleted == %b", duration, isDeleted));
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

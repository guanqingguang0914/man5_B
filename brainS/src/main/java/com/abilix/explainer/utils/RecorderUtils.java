package com.abilix.explainer.utils;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by use038 on 2017/11/8 0008.
 */

public class RecorderUtils {

    private final String TAG = "RecorderUtils";
    private MediaRecorder mMediaRecorder;
    public static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长1000*60*10;
    private final String DEFAULT_PATH = "/dev/null";
    private OnCompletionListener mOnCompletionListener;
    private boolean mIsRecording;
    private boolean mUpdateMicStatus;
    private long startTime;
    private long endTime;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public RecorderUtils() {
        mHandlerThread = new HandlerThread("RecorderUtils");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    /**
     * 开始录音 使用amr格式
     * <p>
     * 录音文件
     *
     * @return
     */
    public synchronized void startRecord(String filePath, OnCompletionListener listener, boolean updateMicStatus) {
        mOnCompletionListener = listener;
        mUpdateMicStatus = updateMicStatus;
        FileUtils.buildDirectory(filePath);
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        try {
            /* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);//DEFAULT
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    //LogMgr.i("onInfo() what:" + what + " extra:" + extra);
                }
            });
            mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    LogMgr.e("onError() what:" + what + " extra:" + extra);
                    stopRecord();
                }
            });
            /* ③准备 */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.setMaxDuration(MAX_LENGTH);
            mMediaRecorder.prepare();
            /* ④开始 */
            db = 0;
            mMediaRecorder.start();
            mIsRecording = true;
            // AudioRecord audioRecord.
            /* 获取开始时间* */
            startTime = System.currentTimeMillis();
            LogMgr.d("startRecord() startTime: " + startTime);
            if (mUpdateMicStatus) {
                updateMicStatus();
            }
        } catch (Exception e) {
            LogMgr.e("startRecord() Error:" + e.getMessage());
            stopRecord();
        }
    }

    public synchronized void startRecord(String filePath, final int duration, OnCompletionListener listener, boolean updateMicStatus) {
        startRecord(filePath, listener, updateMicStatus);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsRecording) {
                    if (System.currentTimeMillis() - startTime >= duration * 1000) {
                        stopRecord();
                    }
                }
            }
        }).start();
    }

    /**
     * 停止录音
     */
    public synchronized int stopRecord() {
        mIsRecording = false;
        mUpdateMicStatus = false;
        if (mHandlerThread != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }
        if (mMediaRecorder == null) {
            return 0;
        }
        endTime = System.currentTimeMillis();
        LogMgr.d("stopRecord() endTime: " + endTime);
        int recordTime = (int) (endTime - startTime);
        LogMgr.d("stopRecord() RecordTime: " + recordTime  + " ms");
        try {
            mMediaRecorder.stop();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(MEDIA_RECORDER_INFO_COMPLETION);
        }
        mOnCompletionListener = null;
        return recordTime;
    }

    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            if (mUpdateMicStatus) {
                updateMicStatus();
            }
        }
    };

    /**
     * 更新话筒状态
     */
    private int BASE = 1;
    private int SPACE = 100;// 间隔取样时间
    private float db = 0;// 分贝
    private int GET_DB_DURATION = 420; //ms

    private void updateMicStatus() {
        if (mMediaRecorder != null && mIsRecording) {
            double ratio = (double) mMediaRecorder.getMaxAmplitude() / BASE;
            //double db = 0;// 分贝
            if (ratio > 1) {
                db = (float) (20 * Math.log10(ratio));
            }
            LogMgr.d("分贝值：" + db + "    TIME:" + (System.currentTimeMillis() - startTime));
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onMicStatusUpdate(db);
            }
            if (mHandler != null) {
                mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
            }
        }
    }

    public synchronized float getMicDbValue() {
        startRecord(DEFAULT_PATH, null, true);
        long recordTime;
        do {
            recordTime = System.currentTimeMillis() - startTime;
        } while (mIsRecording && recordTime < GET_DB_DURATION);
        updateMicStatus();
        stopRecord();
        return db;
    }

    /** Unspecified media recorder error.
     */
    public static final int MEDIA_RECORDER_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;

    public static final int MEDIA_RECORDER_INFO_COMPLETION = 0;

    /**
     * OnCompletionListener callback接口
     */
    public interface OnCompletionListener {
        /**
         * Called when an error occurs while recording.
         *
         * @param what    the type of error that has occurred:
         * <ul>
         * <li>{@link #MEDIA_RECORDER_ERROR_UNKNOWN}
         * <li>{@link #MEDIA_ERROR_SERVER_DIED}
         * <li>{@link #MEDIA_RECORDER_INFO_COMPLETION}
         * </ul>
         */
        void onCompletion(int what);

        void onMicStatusUpdate(float db);
    }
}

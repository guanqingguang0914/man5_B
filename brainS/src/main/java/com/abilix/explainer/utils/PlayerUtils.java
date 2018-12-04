package com.abilix.explainer.utils;

import android.media.MediaPlayer;

/**
 * Created by use038 on 2017/9/20 0020.
 */

public class PlayerUtils {

    private static PlayerUtils instance = null;
    private MediaPlayer mediaPlayer = null;
    private boolean isPaused = false;

    public static final int PLAY_STATE_COMPLETED = 0;
    public static final int PLAY_STATE_ERROR_EXCEPTION = -1;

    public static PlayerUtils getInstance() {
        LogMgr.d("getInstance()");
        if (instance == null) {
            synchronized (PlayerUtils.class) {
                instance = new PlayerUtils();
            }
        }
        return instance;
    }

    private PlayerUtils() {
        LogMgr.d("PlayerUtils()");
        mediaPlayer = new MediaPlayer();
    }

    public void play(String path) {
        play(path, null, false, false);
    }

    public void play(String path, boolean isAsync, boolean isLooping) {
        play(path, null, isAsync, isLooping);
    }

    public void play(String path, OnCompletionListener listener) {
        play(path, listener, false, false);
    }

    /**
     * play 播放音频方法
     *
     * @param path      文件路径
     * @param listener  callback接口
     * @param isAsync   是否使用异步加载
     * @param isLooping 是否循环
     */
    public synchronized void play(String path, final OnCompletionListener listener, boolean isAsync, boolean isLooping) {
        LogMgr.d("play() path:" + path + "; isAsync:" + isAsync + "; isLooping:" + isLooping + "; listener:" + listener);
        stop();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setLooping(isLooping);
            if (isAsync) {
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        LogMgr.d("onPrepared()");
                        mp.start();
                    }
                });
            }
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    LogMgr.d("onCompletion()");
                    if (listener != null) {
                        listener.onCompletion(PLAY_STATE_COMPLETED);
                    }
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    LogMgr.d("onError()");
                    if (listener != null) {
                        listener.onCompletion(extra);
                        return true;
                    }
                    return false;
                }
            });
            if (isAsync) {
                mediaPlayer.prepareAsync();
            } else {
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogMgr.e("play() Exception:" + e.toString());
            if (listener != null) {
                listener.onCompletion(PLAY_STATE_ERROR_EXCEPTION);
            }
        }
    }

    public synchronized void pause() {
        LogMgr.d("pause()");
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPaused = true;
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public synchronized void resume() {
        LogMgr.d("resume()");
        try {
            if (mediaPlayer != null && isPaused) {
                mediaPlayer.start();
                isPaused = false;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stop() {
        LogMgr.d("stop()");
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            isPaused = false;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public synchronized void destroy() {
        LogMgr.e("destroy()");
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
            isPaused = false;
        }
        instance = null;
    }

    /**
     * OnCompletionListener callback接口
     */
    public interface OnCompletionListener {
        /**
         * onCompletion 音频播放完成或播放错误时调用
         *
         * @param state 播放完成状态：
         *              {@link #PLAY_STATE_COMPLETED}
         *              {@link #PLAY_STATE_ERROR_EXCEPTION}
         *              {onError(extra)}
         */
        void onCompletion(int state);
    }
}

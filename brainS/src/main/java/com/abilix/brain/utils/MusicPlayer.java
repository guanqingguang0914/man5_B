package com.abilix.brain.utils;

import java.io.File;
import com.abilix.brain.GlobalConfig;
import android.media.MediaPlayer;
import android.os.Environment;

/**
 * 音频播放工具类
 */
public class MusicPlayer {
    private MediaPlayer mMediaPlayer;

    public String getAudioPath() {
        return audioPath;
    }

    private final String audioPath = Environment.getExternalStorageDirectory()
            .getPath()
            + File.separator
            + "Abilix"
            + File.separator
            + "AbilixMusic" + File.separator;

    public void setMusicFilePath(String musicFilePath) {
        this.musicFilePath = musicFilePath;
    }

    private String musicFilePath;

    public MusicPlayer() {
        mMediaPlayer = new MediaPlayer();
        int robot_type = GlobalConfig.BRAIN_TYPE;
        if (robot_type == GlobalConfig.ROBOT_TYPE_M || robot_type == GlobalConfig.ROBOT_TYPE_M1) {
            musicFilePath = audioPath + "music_m/";
        } else if (robot_type == GlobalConfig.ROBOT_TYPE_H || robot_type == GlobalConfig.ROBOT_TYPE_H3) {
            musicFilePath = audioPath + "music_h/";
        } else if (robot_type == GlobalConfig.ROBOT_TYPE_C ||robot_type == GlobalConfig.ROBOT_TYPE_C9 || robot_type == GlobalConfig.ROBOT_TYPE_C1) {
            musicFilePath = audioPath + "music_c/";
        } else if (robot_type == GlobalConfig.ROBOT_TYPE_F) {
            musicFilePath = audioPath + "music_f/";
        } else if (robot_type == GlobalConfig.ROBOT_TYPE_AF) {
            musicFilePath = audioPath + "music_af/";
        } else if (robot_type == GlobalConfig.ROBOT_TYPE_S) {
            musicFilePath = audioPath + "music_s/";
        } else {
            LogMgr.e("机器人类型错误");
            musicFilePath = audioPath + "music_c/";
        }

    }

    public String getMusicPath() {
        return musicFilePath;
    }

    public void play(String musicName) {
        try {
            stop();
            if (musicName != null && !"".equals(musicName)) {
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                }
                if (musicName.contains("changedansw")
                        || musicName.contains("changeddefb")) {
                    if (Utils.LANGUAGE_TYPE_EN == Utils.getLanguageType()) {
                        LogMgr.d("系统语言为英文，播放英文音频");
                        musicName = "en_" + musicName;
                    }
                }
                mMediaPlayer.reset();
                LogMgr.d("music is playing,music path:" + musicFilePath
                        + musicName);
                mMediaPlayer.setDataSource(musicFilePath + musicName);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogMgr.e("play music error::" + e);
        }
    }

    public void isPlayCh(String musicName) {
        try {
            stop();
            if (musicName != null && !"".equals(musicName)) {
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                }
                if (musicName.contains("changedansw")
                        || musicName.contains("changeddefb")) {
                    if (Utils.LANGUAGE_TYPE_CN != Utils.getLanguageType()
                            && Utils.LANGUAGE_TYPE_TW != Utils
                            .getLanguageType()) {
                        LogMgr.d("系统语言为英文，播放英文音频");
                        musicName = "en_" + musicName;
                    }
                }
                mMediaPlayer.reset();
                LogMgr.d("music is playing,music path:" + musicFilePath + File.separator
                        + musicName);
                mMediaPlayer.setDataSource(musicFilePath + File.separator + musicName);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogMgr.e("play music error::" + e);
        }
    }

    public void play(String musicName,
                     MediaPlayer.OnCompletionListener onCompletionListener) {
        // play(musicName);
        isPlayCh(musicName);
        setOnCompletionListener(onCompletionListener);
    }

    public void stop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * 设置mediaPlayer的播放完成回调
     *
     * @param onCompletionListener
     */
    public void setOnCompletionListener(
            MediaPlayer.OnCompletionListener onCompletionListener) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
    }

}

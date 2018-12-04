package com.abilix.explainer.helper;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;

import com.abilix.explainer.ExplainerApplication;
import com.abilix.explainer.MySensor;
import com.abilix.explainer.helper.Interface.IRobot;
import com.abilix.explainer.utils.ByteUtils;
import com.abilix.explainer.utils.CommonUtils;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.PlayerUtils;
import com.abilix.explainer.utils.PlayerUtils.OnCompletionListener;
import com.abilix.explainer.utils.RecorderUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public abstract class AExplainHelper implements IRobot {

    public PlayerUtils mPlayer;// 此处改为public
    private String musicDir = "";
    protected MySensor mSensor;

    public AExplainHelper() {
        mPlayer = PlayerUtils.getInstance();
        musicDir = CommonUtils.getMusicDirPath() + File.separator;
        mSensor = MySensor.obtainMySensor(ExplainerApplication.instance);
        mSensor.openSensorEventListener();
    }

    /************
     * 公共函数清单 1：readFromFlash 2： getU16 3：getName 4：getParam 5:my_Calc
     * 6:getMvalue 7:delFunNodeJava 8:addFunNodeJava 9：指南针 getCompass
     * 10：getGyro陀螺仪
     * ***************/
    @Override
    public boolean readFromFlash(byte[] src, int srcPos, byte[] dst, int length) {
        int srcLength = src.length;
        if (srcPos < 0 || srcPos > srcLength) {
            LogMgr.e("readFromFlash:: error! ArrayIndexOutOfBoundsException()");
            return false;
        }
        int copyLenth = Math.min(srcLength - srcPos, length);
        System.arraycopy(src, srcPos, dst, 0, copyLenth);
        return true;
    }

    @Override
    public int getU16(byte[] b, int index) {

        return b[index] & 0xFF | (b[index + 1] & 0XFF) << 8;
    }

    @Override
    public String getString(byte[] buffer, int index) {
        byte[] stringData = Arrays.copyOfRange(buffer, 11, 30);
        int i = 0;
        while (i < stringData.length && stringData[i] != 0) {
            i++;
        }
        byte[] nameBytes = Arrays.copyOfRange(stringData, 0, i);

        return new String(nameBytes);
    }

    @Override
    public float getParam(byte[] buffer, float[] val, int index) {
        float ff = 3;
        int i;
        int res = buffer[index];

        float pVel = ByteUtils.byte2float(buffer, index + 1);
        //LogMgr.d("pVel::" + pVel);
        if (res == 1) {
            i = (int) pVel;
            //jingh add
            if (i < 0 || i > 200) {
                return ff;
            }
            ff = val[i];
        } else {
            ff = pVel;
        }
        return ff;
    }

    @Override
    public float getMvalue(float[] val, int index) {

        return val[index];

    }

    @Override
    public int getMotoParam(byte[] buffer, float[] val, int index) {

        int ff = 3;
        int i;
        int res = buffer[index];

        int pVel = ByteUtils.byte2int_2byte(buffer, index + 1);
        if (res == 1) {
            i = (int) pVel;
            ff = (int) val[i];
        } else {
            ff = pVel;
        }
        return ff;
    }

    @Override
    public void getName(byte[] name, byte[] Data) {
        byte p = Data[10];
        int n;
        for (n = 0; n < 20; n++) {
            if (p >= 45 && p <= 123) {
                name[n] = p;
            } else {
                name[n] = '\0';
            }

            p = Data[10 + n];
        }

    }

    private boolean compassCheck = true;

    @Override
    public void setCompassCheck(boolean compassCheck) {
        this.compassCheck = compassCheck;
    }

    @Override
    public float my_Calc(float a, float b, int ch) {
        // Log.v("this is my_calc","this is mycalc");

        float val = 0;
        switch (ch) {
            case 33:
                boolean temp = (a != 0);
                temp = !temp;
                val = temp ? 1.0f : 0.0f;
                break;

            case 42:
                val = a * b;
                break;
            case 43:
                val = a + b;
                break;
            case 45:
                val = a - b;
                break;
            case 47:
                val = a / b;
                break;
            case 38:
                val = (int) a & (int) b;
                break;
            case 37:
                val = a % b;
                break;
            case 60:
                boolean temp1 = (a < b);
                return temp1 ? 1.0f : 0.0f;
            case 61:
                val = a = b;
                break;
            case 62:
                boolean temp2 = (a > b);
                return temp2 ? 1.0f : 0.0f;

            case 76:
                boolean tempa = (a != 0);
                boolean tempb = (b != 0);
                return (tempa && tempb) ? 1.0f : 0.0f;

            case 94:
                return (a != b) ? 1.0f : 0.0f; // )val=(a!=b);break;
            case 121:
                return (a <= b) ? 1.0f : 0.0f; // val=(a<=b);break;
            case 122:
                return (a == b) ? 1.0f : 0.0f; // val=(a==b);break;
            case 123:
                return (a >= b) ? 1.0f : 0.0f; // val=(a>=b);break;
            case 124:
                val = (int) a | (int) b;
                break;
            case 248:
                boolean tempaa = (a != 0);
                boolean tempbb = (b != 0);
                return (tempaa || tempbb) ? 1.0f : 0.0f;

            default:
                break;
        }
        return val;
    }

    @Override
    public int getRandom(int a, int b) {
        int random = a + new Random().nextInt(b - a);
        return random;

    }

    @Override
    public float getCompass() {
        float[] SN = mSensor.getmO();
        return SN[0];
    }

    public float getMcompass() {
        return (getCompass() + 180) % 360;
        //return mSensor.getMcompass();
    }

    @Override
    public float getGyro(float pam1, float pam2) {
        LogMgr.e("pam1::" + pam1 + "   " + "pam2::" + pam2);
        float resut = 0;
        int coor = (int) pam1;
        int type = (int) pam2;
        switch (type) {
            // 角速度
            case 0:
                switch (coor) {
                    // 这个xyz是正常的。
                    case 0:// x轴 百度xyz对应为 120；
                        resut = mSensor.getmO()[1];
                        break;
                    case 1:// y轴
                        resut = mSensor.getmO()[2];
                        break;
                    case 2:// z轴
                        resut = mSensor.getmO()[0];
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        LogMgr.e("resut::" + resut);
        return resut;
    }

    //===========================统一播放声音方法===========================//
    private final static int MUSIC_TYPE_HELLO = 0;
    private final static int MUSIC_TYPE_FACE = 1;
    private final static int MUSIC_TYPE_ACTION = 2;
    private final static int MUSIC_TYPE_ANIMAL = 3;
    private final static int MUSIC_TYPE_PIANO = 4;
    private final static int MUSIC_TYPE_VOICE = 5;

    public final static int SOUND_TYPE_INTRODUCE = 0;
    public final static int SOUND_TYPE_ANIMAL = 1;
    public final static int SOUND_TYPE_INSTRUMENT = 2;

    private final static String[] hello = {"hello.mp3", "bye-bye.mp3", "opposition.mp3", "welcome.mp3", "guanzhao.mp3"};
    private final static String[] face = {"Angry.mp3", "arrogant.mp3", "cry.mp3", "excited.mp3", "fright.mp3", "grievance.mp3", "Happy.mp3", "Kawayi.mp3", "laugh.mp3", "sad.mp3", "wrath.mp3", "tricky.mp3"};
    private final static String[] action = {"cold.mp3", "cute.mp3", "favor.mp3", "hug_coquetry.mp3", "yawn.mp3", "jiayou.mp3", "sleep.mp3", "Leisure.mp3", "guiguisuisui.mp3"};
    private final static String[] animal = {"niu.mp3", "hu.mp3", "haitun.mp3", "ququ.mp3", "yazi.mp3", "mifeng.mp3"};
    private final static String[] piano = {"1.mp3", "2.mp3", "3.mp3", "4.mp3", "5.mp3", "6.mp3", "7.mp3", "8.mp3"};
    private final static String[] instrument = {"sakesi.mp3", "gangqin.mp3", "gudian.mp3", "datiqin.mp3", "xiaohao.mp3", "jita.mp3"};
    private final static String[] random_music = {"mifeng.mp3", "gangqin.mp3", "xiaohao.mp3", "gudian.mp3", "jita.mp3", "niu.mp3"};

    /**
     * playMusic 播放0：打招呼；1：表情；2：动作；3：动物；4：钢琴；5：录音
     *
     * @param type     0：打招呼；1：表情；2：动作；3：动物；4：钢琴；5：录音
     * @param index    音频索引
     *                 打招呼分为：你好、再见、反对、欢迎、请多关照；
     *                 表情分为：生气、傲慢、哭泣、激动、惊吓、委屈、高兴、可爱、大笑、悲伤、愤怒、调皮；
     *                 动作分为：打寒颤、卖萌、赞成、求抱抱、打哈欠、加油、睡觉、休闲、鬼鬼祟祟；
     *                 动物分为：牛、虎、海豚、蟋蟀、鸭、飞虫；
     *                 钢琴分为：1-8；
     *                 录音分为：1-10；
     *                 自我介绍：0-8；
     * @param listener callback接口
     */
    public void playMusic(int type, int index, OnCompletionListener listener) {
        // 数据过滤。
        if (type < 0 || index < 0) {
            LogMgr.e("参数解析错误");
            return;
        }
        switch (type) {
            case MUSIC_TYPE_HELLO:// 打招呼 "你好","再见","反对","欢迎","请多关照"
                if (index < hello.length) {
                    mPlayer.play(musicDir + hello[index], listener);
                }
                break;

            case MUSIC_TYPE_FACE:// 表情
                // （"生气","傲慢","哭泣","激动","惊吓"，"委屈","高兴","可爱","大笑","悲伤","愤怒","调皮"）
                if (index < face.length) {
                    mPlayer.play(musicDir + face[index], listener);
                }
                break;

            case MUSIC_TYPE_ACTION:// 动作 0~8（"打寒颤","卖萌","赞成","求抱抱","打哈欠"，"加油","睡觉","休闲","鬼鬼祟祟"）
                if (index < action.length) {
                    mPlayer.play(musicDir + action[index], listener);
                }
                break;

            case MUSIC_TYPE_ANIMAL:// 动物
                if (index < animal.length) {
                    mPlayer.play(musicDir + animal[index], listener);
                }
                break;

            case MUSIC_TYPE_PIANO:// 钢琴
                if (index <= piano.length && index > 0) {
                    mPlayer.play(musicDir + piano[index - 1], listener);
                }
                break;

            case MUSIC_TYPE_VOICE:// 录音
                String path = FileUtils.getFilePath(FileUtils.DIR_ABILIX_RECORD, String.valueOf(index), FileUtils._TYPE_3GPP);
                mPlayer.play(path, listener);
                break;
        }
    }

    @Override
    public void playMusic(int type, int index) {
        playMusic(type, index, null);
    }

    /**
     * playSound 0:自我介绍 1:动物 2:乐器
     *
     * @param type     0:自我介绍 1:动物 2:乐器
     * @param index    音频索引
     * @param listener callback接口
     */
    public void playSound(int type, int index, OnCompletionListener listener) {
        switch (type) {
            case SOUND_TYPE_INTRODUCE://自我介绍
                String introduceName = CommonUtils.getMusicIntroduceName(index);
                if (introduceName != null) {
                    mPlayer.play(musicDir + introduceName, listener);
                }
                break;

            case SOUND_TYPE_ANIMAL:// 动物
                playMusic(MUSIC_TYPE_ANIMAL, index, listener);
                break;

            case SOUND_TYPE_INSTRUMENT:// 乐器
                if (index >= 0 && index <= instrument.length) {
                    mPlayer.play(musicDir + instrument[index], listener);
                }
                break;
        }
    }

    /**
     * 根据特定目录播放MP3文件
     * @param soundPath
     * @param name
     * @param listener
     */
    public void playSound(String soundPath,String name, OnCompletionListener listener) {
        if (!TextUtils.isEmpty(name)) {
            mPlayer.play(soundPath + name , listener);
        }
    }

    @Override
    public void playSound(int type, int index) {
        playSound(type, index, null);
    }

    /**
     * playSound 播放音频文件 新接口
     *
     * @param name 音频文件名字：命名规则——中文拼音_语言+下标 例如：中文“打招呼”下的“你好”——dazhaohu_c0
     */
    public void playSound(String name, OnCompletionListener listener) {
        if (name != null && name.contains("luyin_p")) {//录音
            String lunyinId = name.substring(7);
            int index = Integer.valueOf(lunyinId);
            playMusic(MUSIC_TYPE_VOICE, index, listener);
        } else {
            if (!TextUtils.isEmpty(name) && !name.endsWith(".mp3")) {
                name = name + ".mp3";
            }
            mPlayer.play(musicDir + name, listener);
        }
    }

    /**
     *播放音乐文件
     * @param soundType 0：自定义音频(在多媒体目录下/Abilix/media/upload/audio/)
     * @param name 音频文件名
     * @param listener 播放状态监听
     */
    public void playSound(int soundType, String name, OnCompletionListener listener) {
        String path = "";
        switch (soundType) {
            case 0x00:
                path = FileUtils.getFilePath(FileUtils.DIR_MEDIA_UPLOAD_AUDIO, name);
                mPlayer.play(path, listener);
                break;

            default:
                if (name != null && name.contains("luyin_p")) {//录音
                    String lunyinId = name.substring(7);
                    int index = Integer.valueOf(lunyinId);
                    playMusic(MUSIC_TYPE_VOICE, index, listener);
                } else {
                    if (!TextUtils.isEmpty(name) && !name.endsWith(".mp3")) {
                        name = name + ".mp3";
                    }
                    mPlayer.play(musicDir + name, listener);
                }
                break;
        }
    }

    @Override
    public void playSound(String name) {
        playSound(name, null);
    }

    @Override
    public void stopPlaySound() {
        mPlayer.stop();
    }

    @Override
    public void playSoundRandom() {
        if (mPlayer.isPaused()) {
            mPlayer.resume();
        } else {
            int index = new Random().nextInt(random_music.length);
            if (index >= 0 && index < random_music.length) {
                mPlayer.play(musicDir + random_music[index], false, true);
            }
        }
    }

    /**
     * @param mode     0 停止播放 1 暂停播放 2 播放
     * @param progress
     */
    @Override
    public void playeretMusic(int mode, int progress) {
        int h = (int) ((float) progress / (float) 100 * 12);
        switch (mode) {
            case 0:
                mPlayer.stop();
                break;
            case 1:
                mPlayer.pause();
                break;
            case 2:
                playSoundRandom();
                break;
        }
        AudioManager mAudioManager = (AudioManager) ExplainerApplication.instance.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.STREAM_MUSIC);

        mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        LogMgr.d("set stream volume::" + h);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, h, 0);
        LogMgr.d("stream volume::" + mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, h, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    public float getMicVolumeDbValue() {
        return new RecorderUtils().getMicDbValue();
    }
}

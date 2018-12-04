package com.abilix.vision.utils;

import android.util.Log;

import com.abilix.explainer.helper.MExplainerHelper;

/**
 * Created by LCT
 * Time:2018/10/9 10:44.
 * Annotation：方块跟踪工具类
 */
public class BlockTrackUtils {
    private static final String TAG = "BlockTrackUtils";
    static TrackThread trackThread;

    public static void startTrack(int[] data) {
        if (trackThread != null) {
            ProcessData(data);
        } else if (data.length > 1) {
            trackThread = new TrackThread(data);
            trackThread.start();
        }

    }

    public static void ProcessData(int[] data) {
        trackThread.updateData(data);
    }

    public static void stopTrack() {
        if (trackThread != null) {
            trackThread.interrupt();
            trackThread.startTrack = false;
            trackThread = null;
        }
    }

    static class TrackThread extends Thread {
        private static final String TAG = "TrackThread";
        MExplainerHelper mExplainerHelper;
        int[] data;
        /**
         * 最小系数
         */
        float minRatio=0.52f;
        /**
         * 转弯偏差系数
         */
        float offsetRatio = 0.88f;
        boolean startTrack = true;
        Object object = new Object();
        boolean isWait = false;

        public TrackThread(int[] data) {
            startTrack = true;
            mExplainerHelper = MExplainerHelper.getInstance();
            updateData(data);
        }

        public void updateData(int[] data) {
            this.data = data;
            Log.i(TAG, "updateData: --收到数据:" + data.length + isWait);
            if (data.length > 1 && trackThread != null && trackThread.isAlive() && isWait) {
                isWait = false;
                Log.i(TAG, "run: --isWait 开始启动:" + isWait);
                synchronized (object) {
                    object.notify();
                }
            }
        }

        @Override
        public void run() {
            super.run();
            while (startTrack) {
                if (data.length <= 1) {
                    waitThread(object);
                } else {
                    visionTrack(data);
                }
            }

        }

        private void waitThread(Object object) {
            if (!isWait) {
                isWait = true;
                Log.i(TAG, "run: --isWait  开始暂停:" + isWait);
                synchronized (object) {
                    try {
                        int speedData[] = new int[2];
                        speedData[0] = 0;
                        speedData[1] = 0;
                        setMotorSpeed(speedData);
                        object.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 识别跟踪
         */
        private void visionTrack(int[] data) {
            int value = mExplainerHelper.getUltrasonic(0);
            int rHeight = data[3];
            int rWidth = data[4];
            float ratio = (float) (rHeight) / 320f;
            float ratioW = (float) (rWidth) / 240f;
            if ((value % 4) == 0) {
                Log.i(TAG, "visionTrack 1: 比例距离value:" + value + "高度：" + rHeight + "比例：" + (float) (rHeight) / 320f);
            }
            if (ratio > minRatio || ratioW > minRatio) {
                Log.i(TAG, "visionTrack: 请保持距离");
                waitThread(object);
                return;
            }

            int[] speed = straightVision(ratio);
            speed = turnVision(speed, data);
            setMotorSpeed(speed);
            Log.i(TAG, "run: " + value);
        }

        /**
         * 直行跟踪
         */
        private int[] straightVision(float ratio) {
            int speedData[] = new int[2];
            /**
             * 最小启动系数minRatio  到 straightMinDistance  速度 0---20
             */
            float straightMinDistance = 0.23f;
            /**
             * 匀速最大比例系数（straightMinDistance --  straightMaxDistance ）  接近于 距离(20 - 40) 公分 匀速
             */
            float straightMaxDistance = 0.17f;
            int maxSpeed = 45;
            int benchmarkSpeed = 16;
            int speed = 0;
            /**
             * straightMinDistance 以内加减系数
             * 得到每个速度相应的比例系数
             */
//            float speedCoefficient = ((float)benchmarkSpeed / 0.17f);
            float speedCoefficient = ((float)benchmarkSpeed / (minRatio-straightMinDistance));

            /**
             * straightMaxDistance 以上加减系数
             */
            float addSpeedCoefficient = 6f;
            if (ratio >= straightMinDistance) {
                speed = (int) ((minRatio - ratio) * speedCoefficient);
            }else if(ratio>= straightMaxDistance && ratio <straightMinDistance){ //距离越远系数越小
                speed = benchmarkSpeed;
            }else {
                if (speed==0) {
                    speed=benchmarkSpeed;
                }
                //int sp = (speed +  (int)((straightMaxDistance - ratio) * addSpeedCoefficient * speed) );
                int sp = (speed +  (int)((straightMaxDistance - ratio) * addSpeedCoefficient * speedCoefficient) );
                speed = sp > maxSpeed ? maxSpeed : sp;
            }

            Log.i(TAG, "straightVision: 直行 当前轮速" + speed  +"系数：" + ratio);
            speedData[0] = speed;
            speedData[1] = speed;
            return speedData;
        }

        /**
         * 转弯跟踪
         */
        private int[] turnVision(int[] speedData, int[] data) {

            Log.i(TAG, "turnVision: data 长度" + data.length);
            int x = data[1];
            int rWidth = data[4];
            int width = 240;
            /**
             * 根据边距计算出正常的x坐标位置
             */
            int centerX = width / 2 - rWidth / 2;
            /**
             *计算出偏移的x坐标量，正数向左偏移 负数向右偏移
             */
            float offsetX = centerX - x;
            Log.i(TAG, "turnVision: 偏差：" + offsetX + " 屏幕宽度：" + width + " 图像宽度：" + rWidth + " 左脚中点:" + centerX + " x坐标：" + x);
            if (offsetX > 6) { //右偏   左转弯  提高右轮速
                offsetX = Math.abs(offsetX);
                speedData[1] = (speedData[1] + (int) (speedData[1] * (offsetX / centerX) * offsetRatio));

                Log.i(TAG, "turnVision: 左转弯 当前轮速 左边：" + speedData[0] + " 右边：" + speedData[1]);
            } else if (offsetX < -6) {//左偏  右转弯  提高左轮速
                offsetX = Math.abs(offsetX);
                speedData[0] = (speedData[0] + (int) (speedData[0] * (offsetX / centerX) * offsetRatio));
                Log.i(TAG, "turnVision: 右转弯 当前轮速 左边：" + speedData[0] + " 右边：" + speedData[1]);
            } else {
                Log.i(TAG, "turnVision: 忽略误差 当前轮速 左边：" + speedData[0] + " 右边：" + speedData[1]);
                return speedData;
            }
            return speedData;
        }

        private void setMotorSpeed(int[] speedData) {
            Log.i(TAG, "straightVision: 当前轮速 左边：" + speedData[0] + " 右边：" + speedData[1]);
            mExplainerHelper.setNewAllWheelMoto(0, 1, speedData[0], 1, speedData[1], 0, 0);

        }
    }
}


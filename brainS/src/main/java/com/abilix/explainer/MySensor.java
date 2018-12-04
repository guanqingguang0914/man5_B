package com.abilix.explainer;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 指南针 陀螺仪
 *
 * @author luox
 */
public class MySensor {
    // 指南针 ， 陀螺仪
    private Sensor mZSensor, mTSensor, accelerometerSensor;
    private SensorManager mSensorManager;
    // 监听 指南针与陀螺仪
    private MySensorEventListener mYSensorEventListener;
    // 角度
    private float[] mO = {0, 0, 0};
    // 角速度
    private float[] mG = {0, 0, 0};
    // 加速度
    private float[] mS = {0, 0, 0};

    public double dtadd = 0;//平衡车数据
    public float zero_value = 0;//平衡车数据
    public boolean startBalanceCar = false;//平衡车数据
    public boolean getBalanceDate = false;//角速度变化

    private static MySensor mYSensor;

    public static MySensor obtainMySensor(Context mContext) {
        if (mYSensor == null) {
            synchronized (MySensor.class) {
                if (mYSensor == null) {
                    mYSensor = new MySensor(mContext);
                }
            }
        }
        return mYSensor;
    }

    private MySensor(Context mContext) {
        mSensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * 监听 指南针与 陀螺仪
     *
     * @author luox
     */
//	public static int sonCount=0;
//	public static long sontime=0;
    private class MySensorEventListener implements SensorEventListener {
        private float dtadd0 = 0;
        private double dt0 = 0;
        private float[] values;
        private static final double NS2S = 1.0 / 1000000000.0;// 9个0.
        private double timestamp;

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            //LogMgr.v("sensor refresh value");
            // 角速度
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {//10秒钟大概700次，14ms获取一次陀螺仪数据
//				sonCount++;
//				if((System.currentTimeMillis()-sontime)>=10000){
//					LogMgr.d("balanceCar sensonCount = "+sonCount);
//					sonCount=0;
//					sontime=System.currentTimeMillis();
//				}
                mYSensor.setmG(event.values);
                if (startBalanceCar) {//平衡车数据
                    getBalanceDate = true;
                    values = event.values; // 角速度。
                    if (timestamp != 0) {
                        dt0 = (event.timestamp - timestamp);
                    }
                    timestamp = event.timestamp;
                    dtadd += (values[0] - zero_value) * dt0 * NS2S;// 这里减去一个偏差 得到偏移角度。
                }
            }// 角度
            else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                mYSensor.setmO(event.values);
            }    // 加速度
            else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mYSensor.setmS(event.values);
            }
        }
    }

    /**
     * 打开 指南针与陀螺仪
     */
    public void openSensorEventListener() {
        getOrientationData();
        getGyroscopeData();
        if (mYSensorEventListener == null) {
            mYSensorEventListener = new MySensorEventListener();
        }
        mSensorManager.registerListener(mYSensorEventListener, mZSensor,
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mYSensorEventListener, mTSensor,
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mYSensorEventListener,
                accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * 关闭 指南针与陀螺仪
     */
    public void stopSensorEventListener() {
        if (mYSensorEventListener != null) {
            mSensorManager.registerListener(mYSensorEventListener, mZSensor,
                    SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(mYSensorEventListener, mTSensor,
                    SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(mYSensorEventListener,
                    accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        mYSensorEventListener = null;
        mZSensor = null;
        mTSensor = null;
        accelerometerSensor = null;
        mO = null;
        mS = null;
        mG = null;
        mYSensor = null;
    }

    // 指南针
    private void getOrientationData() {
        mZSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    // 陀螺仪
    private void getGyroscopeData() {
        accelerometerSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mTSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public float[] getmO() {
        return mO;
    }

    public void setmO(float[] mO) {
        this.mO = mO;
    }

    public float[] getmG() {
        return mG;
    }

    public void setmG(float[] mG) {
        this.mG = mG;

    }

    public float[] getmS() {
        return mS;
    }

    public void setmS(float[] mS) {
        this.mS = mS;
    }

    public void unRegistSensor() {
        mSensorManager.unregisterListener(mYSensorEventListener);
    }
}

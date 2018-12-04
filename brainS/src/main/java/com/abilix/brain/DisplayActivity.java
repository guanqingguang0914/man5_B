package com.abilix.brain;

import java.util.Arrays;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.abilix.brain.aidl.Brain;
import com.abilix.brain.utils.BrainData;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.Utils;

/**
 * 处理从Control收到的信息。
 *
 * @author luox
 */

public class DisplayActivity {
    private static Thread myThread = null;
    private static Context mContext = null;

    /**
     * Brian 显示数据
     *
     * @param brain
     */
    public final static void displayActivity(Brain brain, final Handler mHandler, Context context) {
        mContext = context;
        byte[] data = brain.getSendByte();
        switch (brain.getModeState()) {
            // display open
            case 1:
                SendBroadcastActivity(BrainUtils.SCRATCH_VJC_IMAGEVIEW_STOP, "");
                SendBroadcastActivity(BrainUtils.C_OPEN_CJV, data);
                break;
            // display stop
            case 2:
                SendBroadcastActivity(BrainUtils.SCRATCH_VJC_IMAGEVIEW_STOP, "");
                SendBroadcastActivity(BrainUtils.C_STOP_CJV, "");
                break;
            // 摄像头拍照
            case 4:
                SendBroadcastActivity(BrainUtils.C_STOP_CJV, "");
                int arg2 = data.length > 1 ? data[1] : 1;
                switch (GlobalConfig.BRAIN_TYPE) {
                    case GlobalConfig.ROBOT_TYPE_M:
                    case GlobalConfig.ROBOT_TYPE_H:
                        if (data[0] == 1) {
                            Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_SYSTEM_CAMERA_PHOTO_TAKE, 1, arg2).sendToTarget();
                        } else if (data[0] == 0) {
                            Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_SYSTEM_CAMERA_PHOTO_TAKE, 0, arg2).sendToTarget();
                        }
                        break;
                    case GlobalConfig.ROBOT_TYPE_S:
                    case GlobalConfig.ROBOT_TYPE_C:
                    case GlobalConfig.ROBOT_TYPE_C9:
                    case GlobalConfig.ROBOT_TYPE_H3:
                        if (data[0] == 1) {
                            Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_USB_CAMERA_PHOTO_TAKE, 1, arg2).sendToTarget();
                        } else if (data[0] == 0) {
                            Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_USB_CAMERA_PHOTO_TAKE, 0, arg2).sendToTarget();
                        }
                        break;
                    case GlobalConfig.ROBOT_TYPE_C1:
                    case GlobalConfig.ROBOT_TYPE_C1_2:
                        if (data[0] == 1) {
                            Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_SYSTEM_CAMERA_PHOTO_TAKE, 1, arg2).sendToTarget();
                        } else if (data[0] == 0) {
                            Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_SYSTEM_CAMERA_PHOTO_TAKE, 0, arg2).sendToTarget();
                        }
                        break;
                    default:
                        break;
                }
                break;
            // Vjc 寻线采集数据
            case 5:
                mHandler.sendEmptyMessage(BrainService.HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION);
                break;
            // Vjc 寻线采集数据
            case 6:
                String s1 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT1);
                String s2 = FileUtils.readFile(FileUtils.AI_ENVIRONMENT2);
                LogMgr.e("寻线车数据", "s1:" + s1 + " s2:" + s2);
                Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_ENVIRONMENTAL_COLLECTION_SHOW_DATA, s1 + "\n" + s2).sendToTarget();
                break;
            // 录音
            case 7:
                if(data.length>1){
                    SendBroadcastActivity(BrainData.START_RECORD_EXPLAIN, data);
                }else{
                    SendBroadcastActivity(BrainData.STOP_RECORD_WINDOW, data);//关闭录音界面；
                }
                break;
            // 平衡车
            case 8:
                mHandler.sendMessage(mHandler.obtainMessage(BrainService.HANDLER_MESSAGE_BLANCE_CAR, 1, 0, true));
                if (myThread != null) {
                    myThread.interrupt();
                    myThread = null;
                }
                myThread = new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(12000);
                            mHandler.sendEmptyMessage(BrainService.HANDLER_MESSAGE_BLANCE_CAR_STOP);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                myThread.start();
                break;
            case 9:
                if (myThread != null) {
                    myThread.interrupt();
                    myThread = null;
                }
                if (data[0] == (byte) 0x01) {
                    //退出平衡车关闭弹框
                    mHandler.sendMessage(mHandler.obtainMessage(BrainService.HANDLER_MESSAGE_BLANCE_CAR_STOP, 3, 0));
                    break;
                } else {
                    //平衡车平衡成功
                    mHandler.sendMessage(mHandler.obtainMessage(BrainService.HANDLER_MESSAGE_BLANCE_CAR, 2, 0, false));
                }
                break;
            // 指南针校准
            case 10:
                Message.obtain(mHandler, BrainService.HANDLER_MESSAGE_COMPASS, data[0], 0).sendToTarget();
                break;
            // M轮子进入保护状态
            case 11:
                LogMgr.d("displayActivity() M轮子进入保护状态 step1");
                mHandler.sendMessage(mHandler.obtainMessage(BrainService.HANDLER_MESSAGE_M_BLOCKED_NOTIFY));
                break;
            // 命令的方式显示 字符串data[0]=0 图片 data[0]=1
            case 12:
                LogMgr.e("12-->:" + Arrays.toString(data));
                if (data[0] == 0) {
                    SendBroadcastActivity(BrainUtils.C_OPEN_SCRATCH,
                            Arrays.copyOfRange(data, 1, data.length));
                } else if (data[0] == 1) {
                    SendBroadcastActivity(BrainUtils.SCRATCH_VJC_IMAGEVIEW,
                            FileUtils.SCRATCH_VJC_IMAGE_ + (int)data[4]
                                    + FileUtils.SCRATCH_VJC_IMAGE_JPG);
                }else if(data[0] == 2){
                    SendBroadcastActivity(BrainUtils.SCRATCH_VJC_IMAGEVIEW,
                            FileUtils.SCRATCH_VJC_IMAGE_U201 + (int)data[4]
                                    + FileUtils.SCRATCH_VJC_IMAGE_GIF);
                }else if (data[0] == 3) {
                    SendBroadcastActivity(BrainUtils.SCRATCH_VJC_IMAGEVIEW,
                            FileUtils.SCRATCH_VJC_IMAGE_U201 + (int)data[4]
                                    + FileUtils.SCRATCH_VJC_IMAGE_JPG);
                }
                break;

            //收到Control发过来得机器人类型
            case 13:
                byte[] type_bytes = brain.getSendByte();
                int robotType = Utils.bytesToInt2(type_bytes, 0);
                LogMgr.d("从Control获取到的 robot type::" + robotType);
                if (robotType > -1) {
                    GlobalConfig.BRAIN_CHILD_TYPE = (byte) robotType;
                }

                break;
            //S5对应的LED灯,定为505
            case 14:
                byte[] led = brain.getSendByte();
                LogMgr.i("led = " + Utils.bytesToString(led));
                Message.obtain(mHandler, 505, led[0], 0).sendToTarget();
                break;
            case 15:
                byte[] color_id = brain.getSendByte();
                LogMgr.i("color_id = " + color_id);
                Message.obtain(mHandler, 505, color_id[0], 0).sendToTarget();
                break;
        }
    }
    /**
     * 发送消息 给Activity
     *
     * @param mode
     */
    private static void SendBroadcastActivity(int mode, String filePath) {
        Intent intent = new Intent(GlobalConfig.ACTION_ACTIVITY);
        intent.putExtra(GlobalConfig.ACTION_SERVICE_MODE, mode);
        // LogMgr.e("filePath:" + filePath);
        if (!"".equals(filePath)) {
            intent.putExtra(GlobalConfig.ACTION_ACTIVITY_FILE_FULL_PATH,
                    filePath);
        }
        if (mContext != null) {
            mContext.sendBroadcast(intent);
        }
    }

    /**
     * 发送消息 给Activity
     *
     * @param mode
     * @param data
     */
    private static void SendBroadcastActivity(int mode, byte[] data) {
        Intent intent = new Intent(GlobalConfig.ACTION_ACTIVITY);
        intent.putExtra(GlobalConfig.ACTION_SERVICE_MODE, mode);
        intent.putExtra("data", data);
        if (mContext != null) {
            mContext.sendBroadcast(intent);
        }
    }

}

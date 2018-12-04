package com.abilix.explainer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.abilix.brain.GlobalConfig;
import com.abilix.explainer.chart.C1ChartExplainer;
import com.abilix.explainer.chart.C9ChartExplainer;
import com.abilix.explainer.chart.CChartExplainer;
import com.abilix.explainer.chart.HChartExplainer;
import com.abilix.explainer.chart.M1ChartExplainer;
import com.abilix.explainer.chart.MChartExplainer;
import com.abilix.explainer.chart.SChartExplainer;
import com.abilix.explainer.scratch.C1ScratchExplainer;
import com.abilix.explainer.scratch.C9ScratchExplainer;
import com.abilix.explainer.scratch.CScratchExplainer;
import com.abilix.explainer.scratch.HScratchExplainer;
import com.abilix.explainer.scratch.M1ScratchExplainer;
import com.abilix.explainer.scratch.MScratchExplainer;
import com.abilix.explainer.scratch.S5ScratchExplainer;
import com.abilix.explainer.utils.CommonUtils;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.view.MainActivity;

public class ExplainTracker {

    public static final int MSG_EXPLAIN_NO_DISPLAY = 0x00;
    public static final int MSG_EXPLAIN_DISPLAY = 0x01;
    public static final int MSG_EXPLAIN_IS_GETAI = 0x02;
    public static final int MSG_EXPLAIN_DISPLAY_GRAY = 0x03;
    public static final int MSG_EXPLAIN_BALANCE_INITIALIZING = 0x04;
    public static final int MSG_EXPLAIN_BALANCE_BALANCED = 0x05;
    public static final int MSG_EXPLAIN_RECORD = 0x06;
    public static final int MSG_EXPLAIN_CAMERA = 0x07;
    public static final int MSG_EXPLAIN_COMPASS = 0x08;
    public static final int MSG_EXPLAIN_LED = 0x09;

    public static final int ARG_DISPLAY_TEXT = 0x00;
    public static final int ARG_DISPLAY_PHOTO = 0x01;
    public static final int ARG_DISPLAY_ANIMATION = 0x02;
    public static final int ARG_DISPLAY_CUSTOM_IMAGE = 0x03;
    public static final int MSG_EXPLAIN_VISION = 0x0C;

    private static ExplainTracker instance;
    private ExplainControlHandler explainControlHandler;
    private ExplainExcuteHandler explainExcuteHandler;
    private HandlerThread explainControlThread;
    private HandlerThread explainExcuteThread;
    private IExplainCallBack mIExplainCallBack;
    private IExplainer iExplainer;
    private STMValueQuery mstmvalue;
    private boolean isCancelResponseMessage = false;

    private ExplainTracker() {
        LogMgr.e("ExplainTracker:CommonUtils.getFileType()-->" + CommonUtils.getFileType());
        explainControlThread = new HandlerThread("explainControlThread");
        explainControlThread.start();
        explainControlHandler = new ExplainControlHandler(
                explainControlThread.getLooper());

        explainExcuteThread = new HandlerThread("explainExcuteThread");
        explainExcuteThread.start();
        explainExcuteHandler = new ExplainExcuteHandler(
                explainExcuteThread.getLooper());
        switch (ControlInfo.getMain_robot_type()) {
            //C系列
            case ExplainerInitiator.ROBOT_TYPE_C:
                switch (ControlInfo.getChild_robot_type()) {
                    case ExplainerInitiator.ROBOT_TYPE_BRIANC://C1--C2
                    case ExplainerInitiator.ROBOT_TYPE_C2:
                        if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_CHART
                                || CommonUtils.getFileType() == CommonUtils.FILE_TYPE_PROJECT_PROGRAM) {
                            iExplainer = new C1ChartExplainer(mHandler);
                        } else if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_SCRATCH) {
                            iExplainer = new C1ScratchExplainer(mHandler);
                        }
                        break;

                    case ExplainerInitiator.ROBOT_TYPE_C9://C9
                        if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_CHART
                                || CommonUtils.getFileType() == CommonUtils.FILE_TYPE_PROJECT_PROGRAM) {
                            iExplainer = new C9ChartExplainer(mHandler);
                        } else if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_SCRATCH) {
                            iExplainer = new C9ScratchExplainer(mHandler);
                        }
                        break;

                    default://C3--C8
                        if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_CHART
                                || CommonUtils.getFileType() == CommonUtils.FILE_TYPE_PROJECT_PROGRAM) {
                            iExplainer = new CChartExplainer(mHandler);
                        } else if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_SCRATCH) {
                            iExplainer = new CScratchExplainer(mHandler);
                        }
                        break;
                }
                break;

            //M系列
            case ExplainerInitiator.ROBOT_TYPE_M:
                switch (ControlInfo.getChild_robot_type()) {
                    case ExplainerInitiator.ROBOT_TYPE_M1://M1--M2
                    case ExplainerInitiator.ROBOT_TYPE_M2:
                        if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_CHART
                                || CommonUtils.getFileType() == CommonUtils.FILE_TYPE_PROJECT_PROGRAM) {
                            iExplainer = new M1ChartExplainer(mHandler);
                        } else if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_SCRATCH) {
                            iExplainer = new M1ScratchExplainer(mHandler);
                        }
                        break;

                    case ExplainerInitiator.ROBOT_TYPE_M7://M7--M9
                    case ExplainerInitiator.ROBOT_TYPE_M8:
                    case ExplainerInitiator.ROBOT_TYPE_M9:
                        break;

                    default://M3--M6
                        if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_CHART
                                || CommonUtils.getFileType() == CommonUtils.FILE_TYPE_PROJECT_PROGRAM) {
                            iExplainer = new MChartExplainer(mHandler);
                        } else if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_SCRATCH) {
                            iExplainer = new MScratchExplainer(mHandler);
                        }
                        GlobalConfig.ENABLE_RECORDING_PLAY = true;
                        break;
                }
                break;

            //H系列
            case ExplainerInitiator.ROBOT_TYPE_H:
                if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_CHART
                        || CommonUtils.getFileType() == CommonUtils.FILE_TYPE_PROJECT_PROGRAM) {
                    iExplainer = new HChartExplainer(mHandler);
                } else if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_SCRATCH) {
                    iExplainer = new HScratchExplainer(mHandler);
                }
                break;

            //F系列
            case ExplainerInitiator.ROBOT_TYPE_F:
                break;
            //S系列
            case ExplainerInitiator.ROBOT_TYPE_S:
                if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_CHART
                        || CommonUtils.getFileType() == CommonUtils.FILE_TYPE_PROJECT_PROGRAM) {
                    iExplainer = new SChartExplainer(mHandler);
                } else if (CommonUtils.getFileType() == CommonUtils.FILE_TYPE_SCRATCH) {
                    iExplainer = new S5ScratchExplainer(mHandler);
                }
                break;
        }

        mstmvalue = new STMValueQuery();
    }

    public static ExplainTracker getInstance() {
        if (instance == null) {
            synchronized (ExplainTracker.class) {
                if (instance == null) {
                    instance = new ExplainTracker();
                }
            }
        }
        return instance;
    }

    public void destroy() {
        if(explainControlHandler!=null){
            explainControlHandler.removeCallbacksAndMessages(null);
            explainControlHandler=null;
        }
        if(explainControlThread!=null){
            explainControlThread.quitSafely();
            explainControlThread=null;
        }
        if(explainExcuteHandler!=null){
            explainExcuteHandler.removeCallbacksAndMessages(null);
            explainExcuteHandler=null;
        }
        if (explainExcuteThread!=null){
            explainExcuteThread.quitSafely();
            explainExcuteThread=null;
        }
        instance = null;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            ExplainMessage mExplainMessage = new ExplainMessage();
            LogMgr.d("mHandler.handleMessage: " + msg.toString());
            switch (msg.what) {
                case MSG_EXPLAIN_NO_DISPLAY: // 0x00
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_NO_DISPLAY);
                    break;
                case MSG_EXPLAIN_DISPLAY: // 0x01
                    String content = "";
                    content = (String) msg.obj;
                    mExplainMessage.setState(msg.arg1);
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_DISPLAY).setStrData(content);
                    break;
                case MSG_EXPLAIN_IS_GETAI:  // 0x02
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_IS_GETAI);
                    LogMgr.e("notify brain open dialog");
                    break;
                case MSG_EXPLAIN_DISPLAY_GRAY:  // 0x03
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_DISPLAY_GRAY);
                    LogMgr.e("display gray value");
                    break;
                case MSG_EXPLAIN_BALANCE_INITIALIZING:  // 0x04
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_BALANCE_INITIALIZING).setStrData((String) msg.obj);
                    LogMgr.e("balance car initializing");
                    break;
                case MSG_EXPLAIN_BALANCE_BALANCED:  // 0x05
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_BALANCE_BALANCED).setStrData((String) msg.obj);
                    LogMgr.e("balance car balanced");
                    break;
                case MSG_EXPLAIN_RECORD:  // 0x06
                    LogMgr.e("录音");
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_RECORD);
                    int audioIndex = (msg.obj == null) ? 1 : (int) msg.obj;
                    int duration = msg.arg1;
                    String name = String.valueOf(audioIndex);
                    mExplainMessage.setStrData(name);
                    mExplainMessage.setIntData(duration);
                    LogMgr.e("display record view");
                    break;

                case MSG_EXPLAIN_CAMERA:  // 0x07
                    String imageIndex = String.valueOf(msg.arg1);
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_CAMERA).setStrData(imageIndex);
                    break;

                case MSG_EXPLAIN_COMPASS:  // 0x08
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_COMPASS);
                    break;
                case MSG_EXPLAIN_LED: // 0x09 // S5灯光控制
                    LogMgr.d("led Control message!");
                    /*Brain mBrain = new Brain(2, null);
                    mBrain.setModeState(14);
                    mBrain.setSendByte((byte[]) msg.obj);
                    BrainResponder.getInstance().responsetToBrain(mBrain);*/
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_LED).setData((byte[]) msg.obj);
                    break;
                case MSG_EXPLAIN_VISION:  // 0x0C
                    int index = msg.arg1;
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_VISION).setIntData(index);
                    break;
            }
            if (!isCancelResponseMessage && mIExplainCallBack != null) {
                LogMgr.d("do explain callback:" + mExplainMessage.toString());
                mIExplainCallBack.doExplainCallBack(mExplainMessage);
            }
        }
    };

    public void doExplainCmd(ExplainMessage explainMessage, IExplainCallBack mIExplainCallBack) {
        LogMgr.d("调用doExplainCmd函数");
        explainControlHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        if (mIExplainCallBack != null) { //mIExplainCallBack 必须为MainActivityPresent.this, 否则界面交互都无法显示；
            this.mIExplainCallBack = mIExplainCallBack;
        }
        Message msg = explainControlHandler.obtainMessage();
        msg.obj = explainMessage;
        explainControlHandler.sendMessage(msg);
    }

    private class ExplainControlHandler extends Handler {

        public ExplainControlHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (((ExplainMessage) msg.obj).getFuciton()) {
                case ExplainMessage.EXPLAIN_START:
                    LogMgr.d("Explain线程收到解析执行命令");
                    isCancelResponseMessage = false;
                    explainExcuteHandler.removeCallbacksAndMessages(null);
                    Message excuteMsg = explainExcuteHandler.obtainMessage();
                    excuteMsg.obj = msg.obj;
                    explainExcuteHandler.sendMessage(excuteMsg);
                    break;
                case ExplainMessage.EXPLAIN_STOP:
                    LogMgr.d("ExplainExcute线程收到停止解析执行命令");
                    isCancelResponseMessage = true;
                    if (iExplainer != null) {
                        iExplainer.stopExplain();
                    } else {
                        LogMgr.e("iExplainer is null");
                    }
                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    if (explainControlHandler != null) {
                        explainControlHandler.removeCallbacksAndMessages(null);
                    }
                    if (explainExcuteHandler != null) {
                        explainExcuteHandler.removeCallbacksAndMessages(null);
                    }
                    MainActivity.getActivity().finish();
                    break;

                case ExplainMessage.EXPLAIN_PAUSE:
                    LogMgr.d("ExplainExcute线程收到暂停解析执行命令");
                    if (iExplainer != null) {
                        iExplainer.pauseExplain();
                    } else {
                        LogMgr.e("iExplainer is null");
                    }
                    break;

                case ExplainMessage.EXPLAIN_RESUME:
                    LogMgr.d("ExplainExcute线程收到继续解析执行命令");
                    if (iExplainer != null) {
                        iExplainer.resumeExplain();
                    } else {
                        LogMgr.e("iExplainer is null");
                    }
                    break;
                case ExplainMessage.EXPLAIN_FIRST_GETAI:
                    LogMgr.e("采集AI");
                    mstmvalue.getAiValue(1);
                    break;
                case ExplainMessage.EXPLAIN_SECOND_GETAI:
                    LogMgr.e("采集AI");
                    mstmvalue.getAiValue(2);
                    mstmvalue.sendEnvironment();

                    // 采集完了显示出来。
                    Message responseMsg = mHandler.obtainMessage();
                    ExplainMessage mExplainMessage = new ExplainMessage();
                    mExplainMessage.setFuciton(ExplainMessage.EXPLAIN_SECOND_GETAI);
                    responseMsg.what = 3;
                    responseMsg.obj = mExplainMessage;
                    mHandler.sendMessage(responseMsg);
                    break;

                case ExplainMessage.EXPLAIN_NO_GETAI:
                    mstmvalue.sendEnvironment();
                    LogMgr.e("点击否");
                    break;

                default:

                    break;
            }

        }
    }

    private class ExplainExcuteHandler extends Handler {
        public ExplainExcuteHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (((ExplainMessage) (msg.obj)).getFuciton()) {
                case ExplainMessage.EXPLAIN_START:
                    LogMgr.d("ExplainExcute线程收到解析执行命令");
                    if (iExplainer != null) {
                        iExplainer.doExplain(((ExplainMessage) msg.obj)
                                .getStrData());
                    } else {
                        LogMgr.e("iExplainer is null");
                    }
                    break;

                default:
                    break;
            }
        }
    }
}

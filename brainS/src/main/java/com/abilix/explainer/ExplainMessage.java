/**
 *
 */
package com.abilix.explainer;


/**
 * @author jingh
 * @Descripton:
 * @date2017-2-6下午2:23:15
 */
public class ExplainMessage {
    public static final int EXPLAIN_STOP = 0;
    public static final int EXPLAIN_START = 1;
    public static final int EXPLAIN_PAUSE = 2;
    public static final int EXPLAIN_RESUME = 3;


    //灰度值
    public static final int EXPLAIN_FIRST_GETAI = 4;
    public static final int EXPLAIN_SECOND_GETAI = 5;
    public static final int EXPLAIN_NO_GETAI = 6;
    public static final int EXPLAIN_DISPLAY_GRAY = 9;
    public static final int EXPLAIN_IS_GETAI = 10;

    //显示
    public static final int EXPLAIN_DISPLAY = 7;
    public static final int EXPLAIN_NO_DISPLAY = 8;

    //平衡车正在初始化
    public static final int EXPLAIN_BALANCE_INITIALIZING = 11;

    //平衡车已经平衡
    public static final int EXPLAIN_BALANCE_BALANCED = 12;

    //录音
    public static final int EXPLAIN_RECORD = 13;

    //拍照
    public static final int EXPLAIN_CAMERA = 14;

    //指南针
    public static final int EXPLAIN_COMPASS = 15;

    //LED
    public static final int EXPLAIN_LED = 16;
    //识别跟踪
    public static final int EXPLAIN_VISION = 19;
    private int function;
    private int state;
    private byte[] byteData;
    private String strData;
    private int intData;

    public String getStrData() {
        return strData;
    }

    public ExplainMessage setStrData(String strData) {
        this.strData = strData;
        return this;
    }

    public int getState() {
        return state;
    }

    public ExplainMessage setState(int state) {
        this.state = state;
        return this;
    }

    public byte[] getData() {
        return byteData;
    }

    public ExplainMessage setData(byte[] data) {
        this.byteData = data;
        return this;
    }

    public int getFuciton() {
        return function;
    }

    public ExplainMessage setFuciton(int fuciton) {
        this.function = fuciton;
        return this;
    }

    public int getIntData() {
        return intData;
    }

    public void setIntData(int intData) {
        this.intData = intData;
    }

    public String toString() {
        return "[function:" + this.function + "]" + "[state:" + this.state + "]" + "[strData:" + this.strData + "]" + "[intData:" + this.intData + "]";
    }
}

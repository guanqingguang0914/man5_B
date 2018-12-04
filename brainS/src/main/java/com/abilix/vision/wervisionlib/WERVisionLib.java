package com.abilix.vision.wervisionlib;

public class WERVisionLib {
    /**
     * @param WERVisionModelsPath:model文件路径
     * @return  True:初始化成功
     *          False:初始化失败
     */
    public native boolean Init(String WERVisionModelsPath);

    /**
     * @param imageDate：图像数据
     * @param imageWidth：图像宽度
     * @param imageHeight：图像高度
     * @return -1：没有见测到带颜色的箭头
     *          0：蓝色向下  1：蓝色向左  2：蓝色向右  3：蓝色向上
     *          4：红色向下  5：红色向左  6：红色向右  7：红色向上
     *          8：黄色向下  9：黄色向左 10：黄色向右 11：黄色向上
     */
    public native int ColoredArrowsDetect(byte[] imageDate, int imageWidth, int imageHeight);

    /**
     * @param imageDate:图像数据
     * @param imageWidth:图像宽度
     * @param imageHeight:图像高度
     * @return  -1：没见测到数据符号
     *          0~9:对应数字0～9
     *          10：+
     *          11：-
     *          12：*
     *          13：/
     */
    public native int MathSymbolDetect(byte[] imageDate, int imageWidth, int imageHeight);

    /**
     * @param imageDate：图像数据
     * @param imageWidth：图像宽度
     * @param imageHeight：图像高度
     * @return  [0]:图像中是否检测到黄色矩形框,0,否;1,是
     *          [1]:矩形框左上角x轴坐标
     *          [2]:矩形框左上角y轴坐标
     *          [3]:矩形的高度
     *          [4]:矩形的宽度
     */
    public native int[] YellowRectDetect(byte[] imageDate, int imageWidth, int imageHeight);

    /**
     * @param shapeParameter: 可调矩形形状参数，范围0～1，值越大对矩形的长和宽的要求越小，为1时无要求
     * @param illuminationParameter: 可调光照参数，范围0～1
     */
    public native void YellowRectSetParameter(float shapeParameter, float illuminationParameter);

    /**
     * @param imageDate:图像数据
     * @param imageWidth:图像宽度
     * @param imageHeight：图像高度
     * @return  -1：没见测到符合要求的色块
     *          0：黑色
     *          1：白色
     *          2：红色
     *          3：黄色
     *          4：绿色
     *          5：蓝色
     */
    public native int ColoredBlocksDetect(byte[] imageDate, int imageWidth, int imageHeight);

    /**
     * @return: 库的版本信息
     */
    public native String GetVersion();

    static {
        System.loadLibrary("wer_vision_lib");
        System.loadLibrary("opencv_java");
    }
}

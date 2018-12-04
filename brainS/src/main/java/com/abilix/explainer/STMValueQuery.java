package com.abilix.explainer;

import com.abilix.explainer.helper.C9ExplainerHelper;
import com.abilix.explainer.helper.CExplainerHelper;
import com.abilix.explainer.helper.MExplainerHelper;
import com.abilix.explainer.utils.FileUtils;
import com.abilix.explainer.utils.LogMgr;


public class STMValueQuery {
    private STMValueQueryHelper mExplainHelper;

    public STMValueQuery() {
        mExplainHelper = new STMValueQueryHelper();
    }

    public void getAiValue(int line) {//1代表黑线。0代表白底。

        //采集AI 分为5灰度和7灰度，读文件确定。
        String config = FileUtils.readFile(FileUtils.IO_CONFIG);
        if (config != null) {//这里要求必须能读到配置文件。

            String[] iostr = null;
            iostr = config.split(",");
            int num = 1;
            if (iostr.length > 3) {
                num = Integer.parseInt(iostr[0]);
                if (num == 1) {//7灰度传感器。//这里必须成功  不然没法玩。
                    int ai0_conf = Integer.parseInt(iostr[1]);
                    int ai1_conf = Integer.parseInt(iostr[2]);
                    int ai2_conf = Integer.parseInt(iostr[3]);
                    int ai3_conf = Integer.parseInt(iostr[4]);
                    int ai4_conf = Integer.parseInt(iostr[5]);
                    int ai5_conf = Integer.parseInt(iostr[6]);
                    int ai6_conf = Integer.parseInt(iostr[7]);

                    int ai0 = 0, ai1 = 0, ai2 = 0, ai3 = 0, ai4 = 0, ai5 = 0, ai6 = 0;
                    int ai[] = new int[7];
                    int n = 0;
                    //采集20次取平均值。
                    for (int i = 0; i < 20; i++) {
                        ai = mExplainHelper.ReadAIValue();
                        if (ai != null) {
                            String ss = ai[0] + "," + " " + ai[1] + "," + " " + ai[2] + "," + " " + ai[3] + "," + " " + ai[4] + "," + " " + ai[5] + "," + " " + ai[6] + ",";
                            LogMgr.e("ai value is " + ss);
                            n++;
                            ai0 += ai[ai0_conf];
                            ai1 += ai[ai1_conf];
                            ai2 += ai[ai2_conf];
                            ai3 += ai[ai3_conf];
                            ai4 += ai[ai4_conf];
                            ai5 += ai[ai5_conf];
                            ai6 += ai[ai6_conf];
                        } else {
                            LogMgr.e("ai value err");
                        }
                    }
                    //采集20次之后，不能一次都没采集成功。
                    if (n != 0) {
                        ai0 = ai0 / n;//取平均值。
                        ai1 = ai1 / n;
                        ai2 = ai2 / n;
                        ai3 = ai3 / n;
                        ai4 = ai4 / n;
                        ai5 = ai5 / n;
                        ai6 = ai6 / n;
                        n = 0;
                        //这里存入文件。
                        String mstr = ai0 + "," + ai1 + "," + ai2 + "," + ai3 + "," + ai4 + "," + ai5 + "," + ai6 + ",";
                        //这里判断是线还是白底。
                        if (line == 1) {
                            FileUtils.saveFile(mstr, FileUtils.AI_ENVIRONMENT1);
                        } else {
                            FileUtils.saveFile(mstr, FileUtils.AI_ENVIRONMENT2);
                        }

                    }

                } else {//5灰度
                    //五灰度与7灰度的处理是一样的。
                    int ai0_conf = Integer.parseInt(iostr[1]);
                    int ai1_conf = Integer.parseInt(iostr[3]);
                    int ai2_conf = Integer.parseInt(iostr[4]);
                    int ai3_conf = Integer.parseInt(iostr[5]);
                    int ai4_conf = Integer.parseInt(iostr[7]);

                    int ai0 = 0, ai1 = 0, ai2 = 0, ai3 = 0, ai4 = 0;
                    int ai[] = new int[7];
                    int n = 0;
                    //采集20次取平均值。
                    for (int i = 0; i < 20; i++) {

                        ai = mExplainHelper.ReadAIValue();

                        if (ai != null) {
                            String ss = ai[0] + "," + " " + ai[1] + "," + " " + ai[2] + "," + " " + ai[3] + "," + " " + ai[4] + "," + " " + ai[5] + "," + " " + ai[6] + ",";
                            LogMgr.e("ai value is " + ss);
                            n++;
                            ai0 += ai[ai0_conf];
                            ai1 += ai[ai1_conf];
                            ai2 += ai[ai2_conf];
                            ai3 += ai[ai3_conf];
                            ai4 += ai[ai4_conf];
                        } else {
                            LogMgr.e("ai value err");
                        }
                    }
                    //采集20次之后，不能一次都没采集成功。
                    if (n != 0) {
                        ai0 = ai0 / n;//取平均值。
                        ai1 = ai1 / n;
                        ai2 = ai2 / n;
                        ai3 = ai3 / n;
                        ai4 = ai4 / n;
                        n = 0;
                        //这里存入文件。
                        String mstr = ai0 + "," + ai1 + "," + ai2 + "," + ai3 + "," + ai4 + ",";
                        if (line == 1) {
                            FileUtils.saveFile(mstr, FileUtils.AI_ENVIRONMENT1);
                        } else {
                            FileUtils.saveFile(mstr, FileUtils.AI_ENVIRONMENT2);
                        }
                    }


                }
            }


        } else {
            LogMgr.e("init err");
        }

    }

    public void sendEnvironment() {
        String config = FileUtils.readFile(FileUtils.IO_CONFIG);
        String[] iostr = null;
        iostr = config.split(",");

        if (iostr.length > 3) {
            mExplainHelper.readandsend(Integer.parseInt(iostr[0]));
        } else {
            mExplainHelper.readandsend(1);
        }

    }

    private class STMValueQueryHelper {
        private CExplainerHelper mCExplainHelper;
        private C9ExplainerHelper mC9ExplainHelper;
        private MExplainerHelper mMExplainHelper;

        public STMValueQueryHelper() {
            mCExplainHelper = null;
            mC9ExplainHelper = null;
            mMExplainHelper = null;
            if (ControlInfo.getMain_robot_type() == ExplainerInitiator.ROBOT_TYPE_C) {
                if (ControlInfo.getChild_robot_type() == ExplainerInitiator.ROBOT_TYPE_C9) {
                    mC9ExplainHelper = new C9ExplainerHelper();
                } else {
                    mCExplainHelper = new CExplainerHelper();
                }
            } else if (ControlInfo.getMain_robot_type() == ExplainerInitiator.ROBOT_TYPE_M) {
                mMExplainHelper = MExplainerHelper.getInstance();
            }
        }

        public int[] ReadAIValue() {
            if (mCExplainHelper != null) {
                return mCExplainHelper.ReadAIValue();
            } else if (mC9ExplainHelper != null) {
                return mC9ExplainHelper.ReadAIValue();
            } else if (mMExplainHelper != null) {
                return mMExplainHelper.ReadAIValue();
            }
            return null;
        }

        public void readandsend(int num) {
            if (mCExplainHelper != null) {
                mCExplainHelper.readandsend(num);
            } else if (mC9ExplainHelper != null) {
                mC9ExplainHelper.readandsend(num);
            } else if (mMExplainHelper != null) {
                mMExplainHelper.readandsend(num);
            }
        }
    }

}

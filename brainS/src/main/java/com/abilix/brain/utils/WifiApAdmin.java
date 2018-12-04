package com.abilix.brain.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Random;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.abilix.brain.Application;
import com.abilix.brain.GlobalConfig;

/**
 * WIFI/热点工具类
 *
 * @author luox
 */

public class WifiApAdmin {

    private static final String TAG = "WifiApAdmin";
    private WifiManager mWifiManager;
    private static WifiApAdmin instance;
    private static Object ssid_lock = new Object();
    private static Object pass_lock = new Object();

    private WifiApAdmin(WifiManager mWifiManager) {
        this.mWifiManager = mWifiManager;
    }

    public boolean isMyWifiHotOpen() {
        String hotPot_ssid = readSsid();
        boolean isMyWifiHotOpen = isWifiHotOpen(hotPot_ssid);
        LogMgr.d("is my wifi hot spot opne::" + isMyWifiHotOpen);
        return isMyWifiHotOpen;
    }

    public boolean isWifiHotOpen(String ssid) {
        if (!isWifiApEnabled()) {
            LogMgr.d("hot pot is close");
            return false;
        }
        String wifi_ssid = getWifiApSSID(mWifiManager);
        if (wifi_ssid.equals(ssid)) {
            return true;
        }
        return false;
    }

    public void createWifiHot() {
        LogMgr.d("old ap ssid::" + getWifiApSSID(mWifiManager) + " ssid = " + readSsid() + " pass = " + readPass());
        if (isMyWifiHotOpen()) {

            return;
        }
        if (!isWifiApEnabled() || !(getWifiApSSID(mWifiManager).equals(readSsid()))) {
            if (readPass().equals("") || readPass() == null || readSsid().equals("") || readSsid() == null) {
                String ssid = buildSsid();
                String pass = buildPass();
                LogMgr.d("create new ap ssid::" + ssid + "    " + "pass::" + pass);
                invokeWifiHotsPot(ssid, pass, 3);
                LogMgr.d("ssid = " + ssid + " pass = " + pass);
                savePass(pass);
                saveSsid(ssid);
            } else {
                String ssid = readSsid();
                String pass = readPass();
                invokeWifiHotsPot(ssid, pass, 3);
                LogMgr.d(TAG, "heleiflag ssid = " + ssid + " pass = " + pass);
            }
        }
    }

    public static WifiApAdmin getInstance(WifiManager mWifiManager) {
        if (instance == null) {
            instance = new WifiApAdmin(mWifiManager);
        }
        return instance;
    }

    // 激活热点
    public boolean invokeWifiHotsPot(String ssid, String psw, int type) {
        LogMgr.d(TAG, "AP ssid::" + ssid + "  " + "pass::" + psw);
        // 关闭wifi
        closeWifi();
        // 关闭热点
        closeWifiHotsPot();
        // 设置热点参数
        WifiConfiguration mWifiConfig = setWifiParams(ssid, psw, type);
        if (mWifiConfig != null) {
            try {
                Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,
                        boolean.class);
                // 启动热点
                method.invoke(mWifiManager, mWifiConfig, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long startTime = System.currentTimeMillis();
        while (true) {
            if (isWifiApEnabled()) {
                return true;
            }
            // 10秒后返回
            if ((System.currentTimeMillis() - startTime) > 10 * 1000) {
                return false;
            }
        }
    }

    // 关闭Wifi
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    // 关闭热点服务
    public void closeWifiHotsPot() {
        if (isWifiApEnabled()) {
            LogMgr.d("close wifi hot pot");
            try {
                Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);

                WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);

                Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,
                        boolean.class);
                method2.invoke(mWifiManager, config, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 检测wifi热点是否可用
    public boolean isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 设置wifi参数
    public WifiConfiguration setWifiParams(String ssid, String psw, int type) {
        // 热点的配置
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = ssid;
        // 隐藏热点
        // wifiConfig.hiddenSSID = true;
        // 分为三种情况：1没有密码2用wep加密3用wpa加密
        if (type == 1) { // WIFICIPHER_NOPASS
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;

        } else if (type == 2) { // WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = psw;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 3) { // WIFICIPHER_WPA
            config.preSharedKey = psw;
            // config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    public String getWifiApSSID(WifiManager wifiManager) {
        String ssid = "";
        try {
            Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);
            ssid = netConfig.SSID;
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogMgr.d("wifi hot ssid::" + ssid);
        return ssid;
    }

    public synchronized static void savePass(String pass) {
        synchronized (pass_lock) {
            FileUtils.saveStringToFile(pass, FileUtils.PASS_PATH);
        }
    }

    public synchronized static void saveSsid(String ssid) {
        synchronized (ssid_lock) {
            FileUtils.saveStringToFile(ssid, FileUtils.SSID_PATH);
        }
    }

    public synchronized static String readPass() {
        String pass = "";
        synchronized (pass_lock) {
            pass = FileUtils.readFile(FileUtils.PASS_PATH).replaceAll("\n", "");
        }
        if (pass == null || pass.equals("")) {
            pass = buildPass();
            savePass(pass);
        }
        return pass;
    }

    public synchronized static String readSsid() {
        String ssid = "";
        synchronized (ssid_lock) {
            ssid = FileUtils.readFile(FileUtils.SSID_PATH).replaceAll("\n", "");
        }
        if (ssid == null || ssid.equals("")) {
            ssid = buildSsid();
            saveSsid(ssid);
        }
        return ssid;
    }

    private static String buildSsid() {
//        int random = 10000 + new Random().nextInt(90000);
//        LogMgr.d("random = "+random);

        if(GlobalConfig.IS_USING_NEW_RULE_CREATE_HOTPOT_SSID){
            String random1 = null;
            try {
                random1 = getSsidSuffix();
                LogMgr.i("生成的热点后缀 = "+random1);
            } catch (Exception e) {
                LogMgr.e("生成新ssid异常");
//                random1 = String.valueOf(100000 + new Random().nextInt(900000));
                random1 = Utils.createRandomHotSSidSuffix(6);
                LogMgr.i("替补方案生成的热点后缀 = "+random1);
            }
            return getWifiContent() + random1;
        }else{
            int random = 10000 + new Random().nextInt(90000);
            LogMgr.d("random = "+random);
            return getWifiContent() + random;
        }


    }

    private static String buildPass() {
        int random = 10000000 + new Random().nextInt(90000000);
        return random + "";
    }

    /**
     * 根据设备的MAC地址生成热点名后缀
     * @throws Exception
     */
    private static String getSsidSuffix() throws Exception {
        String mac = Utils.getLocalMacAddressFromWifiInfo(Application.getInstance());
        if(TextUtils.isEmpty(mac)){
            LogMgr.e("mac = "+mac);
            throw new Exception("无法获取到设备的MAC地址");
        }
        LogMgr.d("mac = "+mac);
        String transtr = String.valueOf(new Random().nextInt(9) + 1) + mac.replace(":", "").substring(4);
        LogMgr.d("transtr = "+transtr);
        String ixx = new BigInteger(transtr,16).toString();
        LogMgr.d("ixx = "+ixx);
        String ix = Utils.d10To62(new Long(ixx), 6);
        LogMgr.d("ix = "+ix);
        return ix;
//        System.out.println(transtr+"/"+ixx+"/"+ix);

    }

    private static String getWifiContent() {
        String type = "C";
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C:
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                type = "C";
                break;
            case GlobalConfig.ROBOT_TYPE_M:
            case GlobalConfig.ROBOT_TYPE_M1:
                type = "M";
                break;
            case GlobalConfig.ROBOT_TYPE_H:
            case GlobalConfig.ROBOT_TYPE_H3:
                type = "H";
                break;
            case GlobalConfig.ROBOT_TYPE_S:
                type = "S";
                break;
            case GlobalConfig.ROBOT_TYPE_F:
                type = "F";
                break;
            case GlobalConfig.ROBOT_TYPE_AF:
                type = "AF";
                break;
            case GlobalConfig.ROBOT_TYPE_U:
            case GlobalConfig.ROBOT_TYPE_U5:
                type = "U";
                break;
        }
        return "Abilix-" + type + "-";
    }

    /**
     * 关闭WiFi热点
     */
    public void closeWifiAp(Context context) {
        LogMgr.e("close wifiAp");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (isWifiApEnabled()) {
            try {
                Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
                Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,
                        boolean.class);
                method2.invoke(wifiManager, config, false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}

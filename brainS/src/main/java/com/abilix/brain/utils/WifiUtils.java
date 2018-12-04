package com.abilix.brain.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

//import android.net.wifi.WifiManager.ActionListener;

/**
 * WIFI工具类
 * 2016年8月20日 下午6:11:30
 * @author Jinghao
 */
public class WifiUtils {
    private static final String tag = WifiUtils.class.getSimpleName();

    /**
     * 是否连接到**网络
     *
     * @param context
     * @param ssid
     */
    public static boolean isConnectWifi(Context context, String ssid) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String curSsid = wifiInfo.getSSID();
        if (curSsid != null && curSsid.contains(ssid)) {
            return true;
        }
        return false;
    }

    /**
     * 连接到**Wifi网络
     *
     * @param context
     * @param ssid    网络名称
     * @param pwd     网络密码
     * @return 连接是否成功
     */
    public static boolean connectWifi(Context context, String ssid, String pwd) {
        LogMgr.e("-------connect wfifi");
        if (TextUtils.isEmpty(pwd)) {
            return connectToWifi(context, ssid);
        }

        return connectToWifi(context, ssid, pwd);
    }

    /**
     * 连接到没有密码的**网络
     *
     * @param context
     */
    private static boolean connectToWifi(Context context, String ssid) {
        LogMgr.e("connect to wifi");
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        while (mWifiManager.getWifiState() != mWifiManager.WIFI_STATE_ENABLED) {
            try {
                LogMgr.e("wait wifi opened");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        List<WifiConfiguration> configList = mWifiManager.getConfiguredNetworks();
        ArrayList<Integer> netId = new ArrayList<Integer>();

        for (int i = 0; i < configList.size(); i++) {
            if (configList.get(i).SSID == null) {
                continue;
            }

            if (configList.get(i).SSID.equals("\"" + ssid + "\"")) {
                netId.add(configList.get(i).networkId);
            }
        }

        for (int i = 0; i < netId.size(); i++) {
            mWifiManager.removeNetwork(netId.get(i));
        }

        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.hiddenSSID = false;
        config.status = WifiConfiguration.Status.ENABLED;
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        int wcgID = mWifiManager.addNetwork(config);
        LogMgr.e("wcgID::" + wcgID);
        boolean isOpen = mWifiManager.enableNetwork(wcgID, true);
        LogMgr.e("isOpen::" + isOpen);
        if (wcgID == -1 || !isOpen) {
            return false;
        }

        return true;
    }


    private static long pre = 0, next = 0;
    /**
     * 连接到**Wifi网络
     *
     * @param context
     * @param ssid 网络名称
     * @param pwd 网络密码
     * @return 连接是否成功
     */
    private static boolean connectToWifi(Context context, String ssid, String pwd) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiApAdmin mWifiApAdmin = WifiApAdmin.getInstance(wifiManager);
        mWifiApAdmin.closeWifiHotsPot();
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiConfiguration configuration = new WifiConfiguration();
        int n = 0;
        while (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            try {
                LogMgr.e("wait wifi opened");
                if (n > 50) {
                    n = 0;
                    LogMgr.e("wifi is not open");
                    return false;
                }
                n++;
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LogMgr.e("error::" + e.toString());
                e.printStackTrace();
            }
        }
        List<WifiConfiguration> configList = wifiManager.getConfiguredNetworks();
        for (int i = 0; i < configList.size(); i++) {
            if (configList.get(i).SSID == null) {
                continue;
            }

            if (!configList.get(i).SSID.equals("\"" + ssid + "\"")) {
                LogMgr.e("remove other wifi config info::" + configList.get(i).SSID);
                wifiManager.removeNetwork(configList.get(i).networkId);
            }
        }
        configuration.SSID = "\"" + ssid + "\"";
        configuration.preSharedKey = "\"" + pwd + "\"";
        configuration.hiddenSSID = true;
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        configuration.priority = 1;

        int id = wifiManager.addNetwork(configuration);
        LogMgr.e("addNetwork result::" + id);
        wifiManager.saveConfiguration();
        // Allow a previously configured network to be associated with 这里只是允许连接。
        boolean success = wifiManager.enableNetwork(id, true);
        int i = 0;
        while (i < 10 & !connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
            try {
                LogMgr.e("wait wifi connect");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!wifiNetworkInfo.isConnected()) {
            LogMgr.e("wifi is not connected");
            return false;
        } else {
            String ss = wifiManager.getConnectionInfo().getSSID().replaceAll("\"", "");
            if (!ss.equals(ssid)) {
                LogMgr.e("wifi ssid is not correct");
                return false;
            }
            LogMgr.e("wifi connect sucess");
            return true;
        }
    }

    /**
     * 判断是否打开wifi网络
     *
     * @param context
     */
    public static boolean isOpenWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    /**
     * 判断是否打开网络（wifi或移动网络）
     *
     * @param context
     * @return true表示打开
     */
    public static boolean isOpenNet(Context context) {
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if (network != null) {
            return conManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    /**
     * 获取wifi加密类型
     *
     * @param context
     */
    public static String getWiFiCipher(Context context) {
        String type = "WPA";
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = mWifiManager.getConnectionInfo();
        // 得到配置好的网络连接
        List<WifiConfiguration> wifiConfigList = mWifiManager.getConfiguredNetworks();
        if (wifiConfigList != null) {
            for (WifiConfiguration wifiConfiguration : wifiConfigList) {
                // 配置过的SSID
                String configSSid = wifiConfiguration.SSID;
                configSSid = configSSid.replace("\"", "");

                // 当前连接SSID
                String currentSSid = info.getSSID();
                currentSSid = currentSSid.replace("\"", "");

                // 比较networkId，防止配置网络保存相同的SSID
                if (currentSSid.equals(configSSid) && info.getNetworkId() == wifiConfiguration.networkId) {
                    type = getSecurity(wifiConfiguration);
                    break;
                }
            }
        }

        return type;
    }

    /**
     * These values are matched in string arrays -- changes must be kept in sync
     */
    static String getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return "PSK";
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return "EAP";
        }

        return (config.wepKeys[0] != null) ? "WEP" : "NONE";
    }

    /**
     * get current wifi ssid
     *
     */
    public static String getWiFiSSID(Context mContext) {
        String ssid = "";
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        ssid = wifiInfo.getSSID().replace("\"", "");
        LogMgr.d(tag, "get current wifi ssid:" + ssid);
        if (ssid.contains("unknown ssid") || ssid.contains("0x")) {
            ssid = "";
            return ssid;
        }
        return ',' + ssid;
    }

    /**
     * 获得本地ip地址
     *
     */
    public static String getLocalIpAddress() {
        String tempIp = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String HostAdd = inetAddress.getHostAddress().toString();
                        if (InetAddressUtils.isIPv4Address(HostAdd) && !HostAdd.equals("::1")) {
                            if (HostAdd.startsWith("192")) {
                                return HostAdd;
                            } else {
                                tempIp = HostAdd;
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
            LogMgr.e(tag, "===>>get current ipAddress:" + ex.toString());
        }
        LogMgr.d(tag, "===>>get current ipAddress:" + tempIp);
        return tempIp;
    }

    public static String getMacAddress(Context context) {
        String result = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        result = wifiInfo.getMacAddress();
        if (result == null) {
            LogMgr.e("macAddress is null");
        }
        LogMgr.i(tag, "===>>get current device macAdd:" + result);
        return result;
    }

    public static String getMac() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    /**
     * judge if exist config wifi
     *
     */
    public static boolean isExistConfigNet(Context mContext) {
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configList = mWifiManager.getConfiguredNetworks();
        if (configList != null && configList.size() > 0) {
            LogMgr.d(tag, "wifi config exist..");
            return true;
        } else {
            LogMgr.d(tag, "wifi config not exist..");
            return false;
        }
    }

    /**
     * 断开当前连接的网络
     */
    public static void disConnect(Context mcontext) {
        WifiManager nWifiManager = (WifiManager) mcontext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo nWifiInfo = nWifiManager.getConnectionInfo();
        if (nWifiInfo != null) {
            int netId = nWifiInfo.getNetworkId();
            LogMgr.d(tag, "--disConnect current wifi, netID = " + netId);
            nWifiManager.disableNetwork(netId);
            nWifiManager.disconnect();
        } else {
            LogMgr.e(tag, "--disconnect mWifiInof null !!!");
        }
    }

    /**
     * remove all already exist network
     *
     */
    /*
	 * public static void removeExistNetwork(Context mContext) { LogMgr.d(tag,
	 * "remove existnetwork"); WifiManager mWifiManager = (WifiManager)
	 * mContext.getSystemService(Context.WIFI_SERVICE); List<WifiConfiguration>
	 * configList = mWifiManager.getConfiguredNetworks(); if (configList ==
	 * null) { return; } for (WifiConfiguration config : configList) { if
	 * (config.SSID == null) { continue; } mWifiManager.forget(config.networkId,
	 * new ActionListener() {
	 * 
	 * @Override public void onSuccess() { LogMgr.d(tag,
	 * "---->>remove wifi config success"); }
	 * 
	 * @Override public void onFailure(int reason) { LogMgr.d(tag,
	 * "---->remove wifi config fail"); } }); LogMgr.d(tag,
	 * "remove existnetwork :" + config.SSID); } }
	 */
    public static void controlWifiSwitch(Context mContext, boolean isOpen) {
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(isOpen);
    }

	/*	*/

    /**
     * @param context Context
     * @return true:已连接到wifi false:不连接wifi。
     */
	/*
	 * public static boolean isWifiConnected(Context context) { if (context !=
	 * null) { ConnectivityManager mConnectivityManager = (ConnectivityManager)
	 * context .getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo
	 * mWiFiNetworkInfo =
	 * mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); if
	 * (mWiFiNetworkInfo != null) { return mWiFiNetworkInfo.isConnected(); } }
	 * else { LogMgr.e(tag, "Context is null."); } return false; }
	 */

    // 判断是否已经连上指定wifi
    public static boolean isMyWifiConnected(Context context) {
        String[] wifiInfo = readWifiInfo();
        String wifiSsid = wifiInfo[0];
        boolean isConnect = isWifiConnected(context, wifiSsid);
        LogMgr.d("is connect to my wifi::" + isConnect);
        return isConnect;
    }

    public static boolean isWifiConnected(Context context, String ssid) {
        LogMgr.d("check is connect to wifi " + ssid);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!wifiNetworkInfo.isConnected()) {
            LogMgr.e("wifi is not connected");
            return false;
        } else {
            String ss = wifiManager.getConnectionInfo().getSSID().replaceAll("\"", "");
            if (!ss.equals(ssid)) {
                LogMgr.e("wifi ssid is not correct");
                return false;
            }
            LogMgr.e("wifi connect sucess");
            return true;
        }

    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     */
    public static final boolean pingCheckWifiStatus() {
        String result = "";
        int pos1, pos2;
        String percent = "";
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 10 -w 5 " + ip);// ping网址3次
            // ,wait
            // 5s
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
                pos1 = content.indexOf("received,") + 10;
                pos2 = content.indexOf(" packet loss,");
                if ((pos1 - 10) >= 0 && pos2 >= 0) {
                    percent = content.substring(pos1, pos2);
                }
            }
            LogMgr.d(tag, "package dropPercent:" + percent);
            if ((percent.equals("")) || (percent.trim().equals("100%"))) {
                result = "failed";
            } else {
                result = "success";
                return true;
            }
            // ping的状态
            int status = p.waitFor();
            LogMgr.d(tag, "cmd invoke status:" + status);
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            LogMgr.d(tag, "ping baidu result = " + result);
        }
        return false;
    }

    public static void saveWifiInfo(String wifiSsid, String wifiPass) {
        LogMgr.i("save wifi info ssid::" + wifiSsid + "   " + wifiPass);
        FileUtils.saveStringToFile(wifiPass, FileUtils.WIFI_PASS_PATH);
        FileUtils.saveStringToFile(wifiSsid, FileUtils.WIFI_SSID_PATH);
    }

    public static String[] readWifiInfo() {
        String[] wifiInfo = new String[2];
        wifiInfo[0] = FileUtils.readFile(FileUtils.WIFI_SSID_PATH).replaceAll("\n", "");
        wifiInfo[1] = FileUtils.readFile(FileUtils.WIFI_PASS_PATH).replaceAll("\n", "");
        return wifiInfo;
    }

    public static boolean connectSavedWifi(Context context) {
        String[] wifiInfo = readWifiInfo();
        String wifiSsid = wifiInfo[0];
        String wifiPass = wifiInfo[1];
        LogMgr.e("saved wifi info ssid::" + wifiSsid + "    " + "wifiPass::" + wifiPass);
        if ((!wifiSsid.equals("")) && (wifiSsid != null)) {
            return connectToWifi(context, wifiSsid, wifiPass);
        }
        return false;
    }
}
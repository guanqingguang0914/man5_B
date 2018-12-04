package com.abilix.brain.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

import com.abilix.brain.GlobalConfig;
import com.abilix.brain.data.AppInfo;

/**
 * 获取应用信息工具类
 * http://www.cnblogs.com/leehongee/archive/2013/09/16/3323887.html
 *
 * @author Yang
 */
public class PackageInfoProvider {
    private static final String tag = "PakageInfoProvider";

    private Context context;
    private List<AppInfo> appInfos;
    PackageManager pm;

    public PackageInfoProvider(Context context) {
        super();
        this.context = context;
        pm = context.getPackageManager();
    }

    public List<AppInfo> getAppInfos() {
//		PackageManager pm = context.getPackageManager();
        List<PackageInfo> pakageinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        appInfos = new ArrayList<AppInfo>();

        for (PackageInfo packageInfo : pakageinfos) {
            AppInfo appInfo = getAppinfoFromApplicationInfo(packageInfo);
            appInfos.add(appInfo);
        }

        return appInfos;
    }

    public List<AppInfo> getAppInfos(String catogory) throws NameNotFoundException {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addCategory(catogory);

        appInfos = new ArrayList<AppInfo>();

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
        for (ResolveInfo reInfo : resolveInfos) {
            String pkgName = reInfo.activityInfo.packageName;
            PackageInfo packageInfo;
            try {
                packageInfo = pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
            } catch (Exception e) {
                e.printStackTrace();
                LogMgr.e(tag, "出异常的包名 = " + pkgName);
                continue;
            }
            AppInfo appInfo = getAppinfoFromApplicationInfo(packageInfo);
            appInfos.add(appInfo);
        }

        return appInfos;
    }

    public AppInfo getAppinfoFromApplicationInfo(PackageInfo packageInfo) {
        AppInfo appInfo = new AppInfo();

        appInfo.setAppLabel(packageInfo.applicationInfo.loadLabel(pm).toString());
        appInfo.setRobotClass(GlobalConfig.BRAIN_TYPE);
        appInfo.setPkgName(packageInfo.packageName);
        appInfo.setVersionName(packageInfo.versionName);
        appInfo.setVersionCode(packageInfo.versionCode);
        appInfo.setInstallTime(packageInfo.lastUpdateTime);
        appInfo.setAppLevel(0);

        return appInfo;
    }

    /**
     * 三方应用程序的过滤器
     *
     * @param info
     * @return true 三方应用 false 系统应用
     */
    public boolean filterApp(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            // 代表的是系统的应用,但是被用户升级了. 用户应用
            return true;
        } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            // 代表的用户的应用
            return true;
        }
        return false;
    }

}

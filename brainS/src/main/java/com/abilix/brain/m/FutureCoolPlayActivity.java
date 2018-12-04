package com.abilix.brain.m;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abilix.brain.Application;
import com.abilix.brain.BrainActivity;
import com.abilix.brain.BrainService;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;
import com.abilix.brain.control.ServerHeartBeatProcesser;
import com.abilix.brain.data.AppInfo;
import com.abilix.brain.data.BrainDatabaseHelper;
import com.abilix.brain.ui.ClipViewPager;
import com.abilix.brain.ui.ScalePageTransformer;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.LogMgr;
import com.abilix.brain.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 未来酷乐园页面
 *
 * @author yhd
 */
public class FutureCoolPlayActivity extends Activity {

    private Intent mMainIntent = null;
    private PackageManager mPackageManager;
    //排序相关
    private BrainDatabaseHelper mBrainDatabaseHelper;
    private SQLiteDatabase mSqLiteDatabase;

    private List<AppInfo> mAppInfos = new LinkedList<AppInfo>();
    private InstallationBroadcastReceiver installationBroadcastReceiver;
    private File file_parent = null;


    private ClipViewPager mViewPager;
    private ImageView mTextView;
    private ViewHoder[] mViewHoders;
    private ViewPagerAdapter mAdapter;
    private Typeface mFromAsset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.getInstance().setOrientation(this);
        setContentView(R.layout.activity_m_future);
        mViewPager = (ClipViewPager) findViewById(R.id.m_activity_viewpager);
        findViewById(R.id.page_container).setOnTouchListener(
                new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return mViewPager.dispatchTouchEvent(event);
                    }
                });

        mFromAsset = Typeface.createFromAsset(getAssets(), "fonts/m_fzxx.ttf");

        mTextView = (ImageView) findViewById(R.id.m_activity_back);
        mTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });

        file_parent = new File(BrainUtils.ROBOTINFO);
        initData();
        initView(true);
        registerBroadcastReceiver();

    }

    private void registerBroadcastReceiver() {
        if (installationBroadcastReceiver == null) {
            installationBroadcastReceiver = new InstallationBroadcastReceiver();
        }
        IntentFilter filter1 = new IntentFilter();
        // 安装
        filter1.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter1.addDataScheme("package");
        filter1.setPriority(1000);
        registerReceiver(installationBroadcastReceiver, filter1);
    }

    private void initData() {
        mAppInfos.clear();
        queryAppInfo();
        sort();
        mViewHoders = new ViewHoder[mAppInfos.size()];
    }

    private void initView(boolean isFirst) {
        for (int i = 0; i < mAppInfos.size(); i++) {
            View childview = LayoutInflater.from(this).inflate(
                    R.layout.m_activity_item, null);
            ViewHoder vh = new ViewHoder();
            vh.view = childview;
            vh.ll = (LinearLayout) childview
                    .findViewById(R.id.m_item_ll);

            vh.imageView = (ImageView) childview
                    .findViewById(R.id.m_item_imageview);
            vh.textview = (TextView) childview
                    .findViewById(R.id.m_item_textview);
            vh.textview.setTypeface(mFromAsset);
            mViewHoders[i] = vh;
        }
        initDate(isFirst);
    }

    private void initDate(boolean isFirst) {
        for (int i = 0; i < mAppInfos.size(); i++) {
            final int j = i;
            ViewHoder vh = mViewHoders[i];
            vh.imageView.setImageDrawable(mAppInfos.get(i).getAppIcon());
            vh.textview.setText(mAppInfos.get(i).getAppLabel());
            vh.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isAppInstalled(BrainActivity.getmBrainActivity(), mAppInfos.get(j).getPkgName())) {
                        Utils.openApp(FutureCoolPlayActivity.this, mAppInfos.get(j).getPkgName(), null, false);
                        MUtils.sIsAppBack = true;
                        MUtils.sIsVoiceAppBack = false;
                    }
                }
            });

            vh.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (file_parent.exists()) {
                        String is = FileUtils.readFile(file_parent);
                        if (!is.contains("true")) {
                            delViewPager(j, mAppInfos.get(j));
                        } else {
                            // 家长模式下无法删除
                            Utils.showSingleButtonDialog(FutureCoolPlayActivity.this, getString(R.string.tishi), getString(R
                                            .string.shezhibrain),
                                    getString(R.string.queren), false, new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //TODO yhd
                                        }
                                    });
                        }
                    } else {
                        delViewPager(j, mAppInfos.get(j));
                    }


                    return true;
                }
            });
        }

        if (isFirst) {
            mAdapter = new ViewPagerAdapter();
            // mViewPager.setCurrentItem(Integer.MAX_VALUE / 2 + 1);
            mViewPager.setOffscreenPageLimit(mAppInfos.size());
            // changeListener = new MyChangeListener();
            // mViewPager.setOnPageChangeListener(changeListener);
            mViewPager.setAdapter(mAdapter);
            // mViewPager.setCurrentItem(1);
            mViewPager.setPageTransformer(true, new ScalePageTransformer());
            // mViewPager.setPageMargin(-10);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }


    private class ViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mAppInfos.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            ((ViewPager) container).removeView(view);
            view = null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViewHoders[position].view;
            try {
                if (view.getParent() != null) {
                    container.removeView(view);
                }
                view.setTag(position);
                container.addView(view, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return view;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    private class ViewHoder {
        View view;
        LinearLayout ll;
        ImageView imageView;
        TextView textview;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);

        if (GlobalConfig.BRAIN_TYPE == GlobalConfig.ROBOT_TYPE_M) {
            if (BrainService.getmBrainService() != null
                    && BrainService.getmBrainService().getMService() != null
                    && !Application.getInstance().isFirstStartApplication()
                    && MUtils.sIsAppBack
                    ) {

                if (MUtils.sIsVoiceAppBack) {
                    try {
                        BrainService.getmBrainService().getMService().handAction(MUtils.B_BACK_TO_BRAIN);
                        LogMgr.d("FZXX", "handAction(MUtils.B_BACK_TO_BRAIN)");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        BrainService.getmBrainService().getMService().handAction(MUtils.b_CLICK_BACK_TO_BRAIN);
                        LogMgr.d("FZXX", "handAction(MUtils.b_CLICK_BACK_TO_BRAIN)");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        MUtils.sIsAppBack = false;
        MUtils.sIsVoiceAppBack = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (installationBroadcastReceiver != null) {
            unregisterReceiver(installationBroadcastReceiver);
        }

        LogMgr.d("FutureScienceActivity-->onDestroy()");
    }

    /**
     * 查询apk
     */
    private void queryAppInfo() {
        mPackageManager = getPackageManager();
        //排序相关
        mBrainDatabaseHelper = new BrainDatabaseHelper(this);
        mSqLiteDatabase = mBrainDatabaseHelper.getWritableDatabase();

        mMainIntent = new Intent(Intent.ACTION_MAIN, null);
        mMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mMainIntent.addCategory(GlobalConfig.APP_ABILIX_LAUNCHER);

        List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(mMainIntent, 0);

        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(mPackageManager));
        if (mAppInfos != null) {
            for (ResolveInfo reInfo : resolveInfos) {
                String activityName = reInfo.activityInfo.name;
                String pkgName = reInfo.activityInfo.packageName;
                String appLabel = (String) reInfo.loadLabel(mPackageManager).toString();
                Drawable icon = reInfo.loadIcon(mPackageManager);
                ApplicationInfo appInfo = null;
                try {
                    appInfo = mPackageManager.getApplicationInfo(pkgName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String appFile = appInfo.sourceDir;
                long installed = new File(appFile).lastModified();
                Intent launchIntent = mPackageManager.getLaunchIntentForPackage(pkgName);
                AppInfo appInfos = new AppInfo();
                appInfos.setAppName(activityName);
                appInfos.setAppLabel(appLabel);
                appInfos.setPkgName(pkgName);
                appInfos.setAppIcon(icon);
                appInfos.setIntent(launchIntent);
                appInfos.setInstallTime(installed);

                appInfos.setPageType(AppInfo.PAGE_TYPE_APK);
                if (TextUtils.equals(GlobalConfig.APP_PKGNAME_BRAINSET, pkgName)
                        || TextUtils.equals(GlobalConfig.APP_PKGNAME_GUESSFRUIT, pkgName)
                        || TextUtils.equals(GlobalConfig.APP_PKGNAME_GUESSSTUDY, pkgName)
                        || TextUtils.equals(GlobalConfig.APP_PKGNAME_MY_FAMILY, pkgName)
                        || TextUtils.equals(BrainActivity.OCULUSNAME, pkgName)

                        ) {
                    continue;
                } else if (TextUtils.equals(GlobalConfig.APP_PKGNAME_IM_QOMOLANGMA, pkgName)) {
                    appInfos.setOrder(AppInfo.PAGE_ORDER_IM_QOMOLANGMA);
                    mAppInfos.add(1, appInfos);
                } else if (TextUtils.equals(GlobalConfig.APP_PKGNAME_QOMOLANGMA_SHOW, pkgName)) {
                    appInfos.setOrder(AppInfo.PAGE_ORDER_QOMOLANGMA_SHOW);
                    mAppInfos.add(2, appInfos);
                } else {
                    long rowId = mBrainDatabaseHelper.queryTablePageSortInfo(mSqLiteDatabase, pkgName);
                    if (rowId == -1) {
                        LogMgr.i("页面排序数据库中还没有应用" + pkgName + "的信息");
                        long newRowId = mBrainDatabaseHelper.insertTablePageSortInfo(mSqLiteDatabase, pkgName,
                                BrainDatabaseHelper.PAGE_TYPE_APK);
                        if (newRowId == -1) {
                            LogMgr.e("插入页面排序表失败pkgName = " + pkgName + " 此页面不显示处理");
                            continue;
                        } else {
                            LogMgr.i("插入页面排序表成功pkgName = " + pkgName + " newRowId = " + newRowId);
                            appInfos.setOrder(newRowId);
                        }
                    } else {
                        LogMgr.i("页面排序数据库中有应用" + pkgName + "的信息 rowId = " + rowId);
                        appInfos.setOrder(rowId);
                    }
                    mAppInfos.add(appInfos);
                }
            }
        }
    }

    private void sort() {
        Collections.sort(mAppInfos, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                if (lhs.getOrder() < rhs.getOrder()) {
                    return -1;
                } else if (lhs.getOrder() > rhs.getOrder()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        LogMgr.i("排序表中的行数 = " + mBrainDatabaseHelper.getRowCountInTablePageSortInfo(mSqLiteDatabase) + " 页面总数 = " +
                mAppInfos.size());
        if (mBrainDatabaseHelper.getRowCountInTablePageSortInfo(mSqLiteDatabase) - mAppInfos.size() >
                BrainDatabaseHelper.MOST_INVALID_ROW_NUMBER) {
            List<String> pageNames = new ArrayList<String>();
            for (int i = 0; i < mAppInfos.size(); i++) {
                if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_APK) {
                    pageNames.add(mAppInfos.get(i).getPkgName());
                } else if (mAppInfos.get(i).getPageType() == AppInfo.PAGE_TYPE_FILE && mAppInfos.get(i).getFileType()
                        != AppInfo.FILE_TYPE_SKILLPLAYER) {
                    pageNames.add(mAppInfos.get(i).getPathName());
                }
            }
            int deleteRows = mBrainDatabaseHelper.deleteAllInvalidPageSortInfo(mSqLiteDatabase, pageNames);
            LogMgr.i("删除排序表中无用行数 deleteRows = " + deleteRows);

        }
    }

    /**
     * 插入apk页面
     */
    public void insertApkView(String apkPakName) {
        if (apkPakName != null) {
            if (mMainIntent == null) {
                mMainIntent = new Intent(Intent.ACTION_MAIN, null);
                mMainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                mMainIntent.addCategory(GlobalConfig.APP_ABILIX_LAUNCHER);
            }
            List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(mMainIntent, 0);

            Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(mPackageManager));
            if (mAppInfos != null) {
                for (ResolveInfo reInfo : resolveInfos) {
                    String activityName = reInfo.activityInfo.name;
                    String pkgName = reInfo.activityInfo.packageName;
                    if (apkPakName.equals(pkgName)) {
                        String appLabel = (String) reInfo.loadLabel(mPackageManager).toString();
                        Drawable icon = reInfo.loadIcon(mPackageManager);
                        ApplicationInfo appInfo = null;
                        try {
                            appInfo = mPackageManager.getApplicationInfo(pkgName, 0);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        String appFile = appInfo.sourceDir;
                        long installed = new File(appFile).lastModified();
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkgName);
                        AppInfo appInfos = new AppInfo();
                        appInfos.setAppName(activityName);
                        appInfos.setAppLabel(appLabel);
                        appInfos.setPkgName(pkgName);
                        appInfos.setAppIcon(icon);
                        appInfos.setIntent(launchIntent);
                        appInfos.setInstallTime(installed);
                        // appInfos.setApk(true);
                        appInfos.setPageType(AppInfo.PAGE_TYPE_APK);
                        insertPage(mViewHoders.length, appInfos);
                        return;
                    } else {
                        continue;
                    }
                }
            }
        }
    }


    /**
     * 插入页面：将appInfo viewHolder添加到列表中 并刷新页面 跳至该页面
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void insertPage(int i, AppInfo appInfo) {
        if (appInfo == null) {
            LogMgr.e("insertPage() appInfo == null");
            return;
        }
        mAppInfos.add(i, appInfo);
        //用于插入数据库中
        String pageName = appInfo.getPageType() == AppInfo.PAGE_TYPE_APK ? appInfo.getPkgName() : appInfo
                .getPathName();
        int pageType = appInfo.getPageType() == AppInfo.PAGE_TYPE_APK ? appInfo.getPageType() : appInfo
                .getFileType();
        long rowID = mBrainDatabaseHelper.insertTablePageSortInfo(mSqLiteDatabase, pageName, pageType);
        if (rowID == -1) {
            LogMgr.e("插入数据库中pageName = " + pageName + "时，数据库中已存在该数据，插入失败。");
            if (mBrainDatabaseHelper.deleteTablePageSortInfo(mSqLiteDatabase, pageName)) {
                LogMgr.i("删除老旧数据成功pageName = " + pageName);
                rowID = mBrainDatabaseHelper.insertTablePageSortInfo(mSqLiteDatabase, pageName, pageType);
                if (rowID == -1) {
                    LogMgr.e("删除数据库中pageName = " + pageName + "后，插入数据库仍失败，放弃插入，重启后会丢失页面");
                } else {
                    LogMgr.i("删除数据库中pageName = " + pageName + "后，插入数据库成功 rowID = " + rowID);
                    appInfo.setOrder(rowID);
                }
            } else {
                LogMgr.e("删除老旧数据失败pageName = " + pageName + "  页面顺序重启后可能不按时间顺序排列");
            }
        } else {
            LogMgr.i("插入数据库中pageName = " + pageName + "时，插入成功 rowID = " + rowID);
            appInfo.setOrder(rowID);
        }

        //刷新UI
        mViewHoders = new ViewHoder[mAppInfos.size()];
        initView(false);
        mViewPager.setCurrentItem(i);
    }

    /**
     * 删除页面
     *
     * @param appInfo
     */
    public void delViewPager(final int i, final AppInfo appInfo) {
        Utils.showTwoButtonDialog(this, getString(R.string.tishi), getString(R.string.delete),
                getString(R.string.cancel),
                getString(R.string.determine), false, new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (appInfo != null) {
                            String pageName = null;
                            if (appInfo.getPageType() == AppInfo.PAGE_TYPE_FILE) {
                                String fileName = appInfo.getPathName();
                                pageName = fileName;
                                if (!TextUtils.isEmpty(fileName)) {
                                    File file = new File(fileName);
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    file = null;
                                }
                            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_APK) {
                                pageName = appInfo.getPkgName();
                                ServerHeartBeatProcesser.getInstance().feedbackToAppStore(appInfo.getPkgName(), true);
                            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_RECORD) {
                            } else if (appInfo.getPageType() == AppInfo.PAGE_TYPE_IMAGE) {
                            }

                            if (mBrainDatabaseHelper.deleteTablePageSortInfo(mSqLiteDatabase, pageName)) {
                                LogMgr.i("删除文件排序表成功 pageName = " + pageName);
                            } else {
                                LogMgr.e("删除文件排序表失败 pageName = " + pageName);
                            }

                            int realIndex = mAppInfos.indexOf(appInfo);
                            LogMgr.i("删除对象的原index = " + i + " 真实index = " + realIndex);
//                            mViewHoders.remove(realIndex);
                            mAppInfos.remove(realIndex);
                            //刷新界面
                            mViewHoders = new ViewHoder[mAppInfos.size()];
                            LogMgr.d("mAppInfos.size : " + mAppInfos.size());
                            LogMgr.d("mViewHoders.size : " + mViewHoders.length);
                            initView(false);

                        }
                    }
                });
    }


    /**
     * 监听apk 安装卸载
     *
     * @author luox
     */
    class InstallationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogMgr.d("InstallationBroadcastReceiver onReceive intent.getAction() = " + intent.getAction());
            // 安装
            if (TextUtils.equals(Intent.ACTION_PACKAGE_ADDED, intent.getAction())) {
                BrainUtils.utilisToast(getString(R.string.anzhuangchenggong), FutureCoolPlayActivity.this);
                // 更新页面
                //如果是内置的三个应用,不更新页面
                String packageName = intent.getData().getSchemeSpecificPart();
                if (TextUtils.equals(GlobalConfig.APP_PKGNAME_MY_FAMILY, packageName)
                        || TextUtils.equals(GlobalConfig.APP_PKGNAME_GUESSFRUIT, packageName)
                        || TextUtils.equals(GlobalConfig.APP_PKGNAME_GUESSSTUDY, packageName)) {
                    LogMgr.d("内置应用,不改变界面");
                } else {
                    insertApkView(intent.getData().getSchemeSpecificPart());
                }
            }
        }
    }

}

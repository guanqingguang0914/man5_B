package com.abilix.explainer;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.abilix.control.aidl.IControl;
import com.abilix.explainer.utils.LogMgr;
import com.abilix.explainer.utils.SPUtils;

/**
 * @author jingh
 * @Descripton:
 * @date2017-1-18下午5:49:46
 */
public class ExplainerApplication {
    public static Application instance;

    public static void init(Application application) {
        instance = application;
    }
}

/**
 *
 */
package com.abilix.explainer;

import java.io.File;

import android.os.Build;
import android.os.Environment;

/**
 * @author jingh
 * @Descripton:
 * @date2017-2-4下午1:36:29
 */
public class GlobalConfig {
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "Download";

}

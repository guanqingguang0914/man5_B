/**
 *
 */
package com.abilix.explainer.view;

import android.content.Context;

/**
 * @author jingh
 * @Descripton:
 * @date2017-2-3下午5:05:04
 */
public interface IExplainerAlertDialogs {
    void showBalancingAlertDialog(Context context);

    void dismissBalancingAlertDialog();

    void showBalanceFailedAlertDialog(Context context);

    void dismissBalanceFailedAlertDialog();

    void showBalanceSucessedAlertDialog(Context context);

    void dismissBalanceSucessedAlertDialog();

    void showCompassAdjustNotificationAlertDialog(Context context);

    void dismissCompassAdjustNotificationAlertDialog();

    void showCompassAdjustFinishedAlertDialog(Context context);

    void dismissCompassAdjustFinishedAlertDialog();

    void showCameraOpeningAlertDialog(Context context);

    void dismissCameraOpeningAlertDialog();

    void showCameraConnectiongErrorAlertDialog(Context context);

    void dismissCameraConnectiongErrorAlertDialog();

    void showGrayValueAlertDialog(Context context, String data);

    void dismissGrayValueAlertDialog();

    void showGrayIsCollectAlertDialog(Context context);

    void dismissGrayIsCollectAlertDialog();

    void showGrayBlackLineAlertDialog(Context context);

    void dismissGrayBlackLineAlertDialog();

    void showGrayWhiteBackGroundAlertDialog(Context context);

    void dismissGrayWhiteBackGroundAlertDialog();
}

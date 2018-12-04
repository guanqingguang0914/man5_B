/**
 *
 */
package com.abilix.explainer.present;

import android.content.Intent;
import android.view.View;

/**
 * @author jingh
 * @Descripton:
 * @date2017-2-3下午5:32:13
 */
public interface IMainActivityPresent {
    void compassNotifiNegEvent();

    void compassNotifiPosEvent();

    void compassAdjustFinishedNegEvent();

    void compassAdjustFinishedPosEvent();

    void grayIsCollectNegEvent();

    void grayIsCollectPosEvent();

    void grayBlackLineNegEvent();

    void grayBlackLinePosEvent();

    void grayWhiteBackgroundNegEvent();

    void grayWhiteBackgroundPosEvent();

    void disposeFilePath(Intent i);

    void disposeOnClickEvent(View v);

    void disposeOnLongClickEvent(View v);

    void pause();

    void destroy();

    void responseAndfinish();
}

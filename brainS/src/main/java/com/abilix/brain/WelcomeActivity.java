package com.abilix.brain;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.LogMgr;

/**
 * 欢迎界面，现未被使用。
 */
public class WelcomeActivity extends Activity {

    private ImageView imageView_qr;
    private AnimatorSet mAnimatorSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogMgr.i("WelcomeActivity onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brain_content_layout);
        initViews();
        startAnima();
//		startJumpTimer();
    }

    private void initViews() {
        imageView_qr = (ImageView) findViewById(R.id.imageview_qr);
        imageView_qr.setBackgroundResource(BrainUtils.getAppBg());
    }

    private void startAnima() {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(imageView_qr, "rotation", 0f, 360f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(imageView_qr, "alpha", 1f, 0.8f, 1f);
        alpha.setRepeatCount(0);
        rotate.setRepeatCount(0);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                LogMgr.d("onAnimationStart()");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                LogMgr.d("onAnimationRepeat()");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LogMgr.d("onAnimationEnd()");
                Intent intent = new Intent(WelcomeActivity.this, BrainActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                LogMgr.d("onAnimationCancel()");
            }
        });
        mAnimatorSet.setInterpolator(new LinearInterpolator());
        mAnimatorSet.play(rotate).with(alpha);
        mAnimatorSet.setDuration(4 * 1000);
        mAnimatorSet.start();
    }

//	private void startJumpTimer() {
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				Intent intent = new Intent(WelcomeActivity.this, BrainActivity.class);
//				startActivity(intent);
//				WelcomeActivity.this.finish();
//			}
//		}, 3*1000);
//	}

    @Override
    protected void onStart() {
        LogMgr.i("WelcomeActivity onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        LogMgr.i("WelcomeActivity onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogMgr.i("WelcomeActivity onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogMgr.i("WelcomeActivity onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LogMgr.i("WelcomeActivity onDestroy()");
        super.onDestroy();
    }

}

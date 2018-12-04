package com.abilix.brain.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.abilix.brain.Application;
import com.abilix.brain.GlobalConfig;
import com.abilix.brain.R;
import com.abilix.brain.data.AppInfo;

/**
 * 照片，录音，技能页面。
 */
public class ManageFragment extends FragmentActivity {
    private Button mBack;
    private Fragment mFragment;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    public TextView mTextViewM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Application.getInstance().setOrientation(this);
        setContentView(R.layout.manage);
        mBack = (Button) findViewById(R.id.manage_back);
        mTextViewM = (TextView) findViewById(R.id.manage_fragment_textview);
        switch (GlobalConfig.BRAIN_TYPE) {
            case GlobalConfig.ROBOT_TYPE_C:
            case GlobalConfig.ROBOT_TYPE_C1:
            case GlobalConfig.ROBOT_TYPE_CU:
            case GlobalConfig.ROBOT_TYPE_C9:
                mBack.setBackgroundResource(R.drawable.back_selector_c);
                break;
            case GlobalConfig.ROBOT_TYPE_M:
            case GlobalConfig.ROBOT_TYPE_M1:
                mBack.setBackgroundResource(R.drawable.back_selector_m);
                break;
            case GlobalConfig.ROBOT_TYPE_H:
            case GlobalConfig.ROBOT_TYPE_H3:
                mBack.setBackgroundResource(R.drawable.back_selector_h);
                break;
            case GlobalConfig.ROBOT_TYPE_AF:
                mBack.setBackgroundResource(R.drawable.back_selector_af);
                break;
            case GlobalConfig.ROBOT_TYPE_S:
                mBack.setBackgroundResource(R.drawable.back_selector_s);
                break;
            default:
                mBack.setBackgroundResource(R.drawable.back_selector_c);
                break;
        }
        mBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // int i = -1;
                // if (mFragment != null) {
                // if (mFragment instanceof PhotoFragment) {
                // i = ((PhotoFragment) mFragment).getmDatas().size() == 0 ? 1
                // : 0;
                // } else if (mFragment instanceof RecordFragment) {
                // i = ((RecordFragment) mFragment).getmDatas().size() == 0 ? 1
                // : 0;
                // }
                // }
                // setResult(i);
                onBackPressed();
            }
        });
    }

    private void initFragment(int id) {
        switch (id) {
            case AppInfo.PAGE_TYPE_IMAGE:
                mTextViewM.setVisibility(View.GONE);
                mFragment = new PhotoFragment();
                break;
            case AppInfo.PAGE_TYPE_RECORD:
                mTextViewM.setVisibility(View.VISIBLE);
                mFragment = new RecordFragment1();
                break;
            case AppInfo.FILE_TYPE_SKILLPLAYER:
                mTextViewM.setVisibility(View.GONE);
                mFragment = new SkillPlayFragment();
                break;
        }
        addFragment(mFragment, mFragment.getTag());
    }

    private void addFragment(Fragment fragment, String tag) {
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        transaction.add(R.id.manage_fragment, fragment, tag);
        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        int id = getIntent().getIntExtra("id", 0);
        initFragment(id);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFragment = null;
        manager = null;
        transaction = null;
    }

}

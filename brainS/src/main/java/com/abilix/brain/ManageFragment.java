package com.abilix.brain;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.abilix.brain.fragment.PhotoFragment;
import com.abilix.brain.fragment.RecordFragment;
import com.abilix.brain.fragment.SkillPlayFragment;
import com.abilix.brain.data.AppInfo;
import com.umeng.analytics.MobclickAgent;

/**
 * 此类未被使用，相关功能参见{@link com.abilix.brain.fragment.ManageFragment}。
 */
@Deprecated
public class ManageFragment extends FragmentActivity {
    private Button mBack;
    private Fragment mFragment;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.manage);
        mBack = (Button) findViewById(R.id.manage_back);
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
            case GlobalConfig.ROBOT_TYPE_F:
                mBack.setBackgroundResource(R.drawable.back_selector_f);
                break;
            case GlobalConfig.ROBOT_TYPE_AF:
                mBack.setBackgroundResource(R.drawable.back_selector_af);
                break;
            case GlobalConfig.ROBOT_TYPE_S:
                mBack.setBackgroundResource(R.drawable.back_selector_s);
                break;
            default:
                mBack.setBackgroundResource(R.drawable.back_selector_f);
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
                mFragment = new PhotoFragment();
                break;
            case AppInfo.PAGE_TYPE_RECORD:
                mFragment = new RecordFragment();
                break;
            case AppInfo.FILE_TYPE_SKILLPLAYER:
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
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
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

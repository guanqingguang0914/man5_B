package com.abilix.brain.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abilix.brain.R;
import com.abilix.brain.ui.BrainViewPager;
import com.abilix.brain.ui.MyTransformation;
import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.FileUtils;
import com.abilix.brain.utils.Utils;

/**
 * 显示照片 Fragment
 *
 * @author luox
 */
public class PhotoFragment extends Fragment {
    private BrainViewPager mViewPager;
    private ManageFragment manageFragment;
    private View mView;
    private Map<Integer, Bitmap> mDatas = new HashMap<Integer, Bitmap>();
    private File mFile;
    private List<ViewHoder> mViews = new LinkedList<ViewHoder>();
    private ViewPagerAdapter mAdapter;
    private TextView mTextView;

    public Map<Integer, Bitmap> getmDatas() {
        return mDatas;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        manageFragment = (ManageFragment) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFile = new File(FileUtils.SCRATCH_VJC_IMAGE_);
        initDate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.photo_fragment, null);
        mViewPager = (BrainViewPager) mView.findViewById(R.id.photo_viewpager);
        mTextView = (TextView) mView.findViewById(R.id.photo_textview);
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mViews.clear();
        mDatas.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        List<Entry<Integer, Bitmap>> list = new ArrayList<Entry<Integer, Bitmap>>();
        list.addAll(mDatas.entrySet());
        Collections.sort(list, new ComparatorP());
        for (final Entry<Integer, Bitmap> entry : list) {
            View childview = LayoutInflater.from(manageFragment).inflate(
                    R.layout.photo_fragment_item, null);
            ViewHoder vh = new ViewHoder();
            vh.view = childview;
            vh.imageview = (ImageView) childview
                    .findViewById(R.id.photo_fragment_item_imageview);
            vh.textview = (TextView) childview
                    .findViewById(R.id.photo_fragment_item_textview);
            vh.imageview.setImageBitmap(entry.getValue());
            vh.textview.setText(getString(R.string.zhaopian) + entry.getKey());
            // LogMgr.e("entry.getKey():" + entry.getKey());
            vh.id = entry.getKey();
            mViews.add(vh);
        }
        initView();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void initView() {
        if (mViews.size() > 0) {
            mTextView.setVisibility(View.GONE);
            mViewPager.setVisibility(View.VISIBLE);
            mAdapter = null;
            for (int i = 0; i < mViews.size(); i++) {
                final int j = i;
                mViews.get(i).view
                        .setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                mViewPager.setScrollble(false);
                                File file_parent = new File(BrainUtils.ROBOTINFO);
                                if (file_parent.exists()) {
                                    String is = FileUtils.readFile(file_parent);
                                    if (!is.contains("true")) {
                                        delViewPager(mViews.get(j).id, j);
                                    } else {
                                        // 家长模式下无法删除
                                        Utils.showSingleButtonDialog(getActivity(), getString(R.string.tishi), getString(R.string.shezhibrain),
                                                getString(R.string.queren), false, new OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mViewPager.setScrollble(true);
                                                    }
                                                });
//										AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
//										dialog.setTitle(getString(R.string.tishi));
//										dialog.setMessage(getString(R.string.shezhibrain));
//										dialog.setCancelable(false);
//										dialog.setPositiveButton(getString(R.string.queren),
//												new DialogInterface.OnClickListener() {
//													@Override
//													public void onClick(DialogInterface dialog, int which) {
//														mViewPager.setScrollble(true);
//													}
//												});
//										dialog.show();
                                    }
                                } else {
                                    delViewPager(mViews.get(j).id, j);
                                }
                                return true;
                            }
                        });
            }
            mAdapter = new ViewPagerAdapter();
            mViewPager.setOffscreenPageLimit(3);
            mViewPager.setPageMargin(-65);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setPageTransformer(true, new MyTransformation());
        } else {
            mTextView.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.GONE);
        }
    }

    private void initDate() {
        FileInputStream fis = null;
        File[] names = mFile.listFiles();
        if (names.length > 0) {
            for (File name : names) {
                if (name.getName().endsWith(FileUtils.SCRATCH_VJC_IMAGE_JPG)) {
                    try {
                        fis = new FileInputStream(name);
                        if (fis.available() > 0) {
                            Bitmap mBitmap = BitmapFactory.decodeStream(fis);
                            // LogMgr.e("name:"
                            // + name.getName().substring(0,
                            // name.getName().indexOf(".")));
                            mDatas.put(Integer.parseInt(name.getName().substring(0, name.getName().indexOf("."))), mBitmap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mViews.size();
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
            View view = mViews.get(position).view;
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
    }

    private class ViewHoder {
        int id;
        View view;
        ImageView imageview;
        TextView textview;
    }

    private static class ComparatorP implements
            Comparator<Map.Entry<Integer, Bitmap>> {
        @Override
        public int compare(Entry<Integer, Bitmap> lhs,
                           Entry<Integer, Bitmap> rhs) {
            return lhs.getKey().compareTo(rhs.getKey());
        }
    }

    /**
     * 删除页面
     */
    public void delViewPager(final int i, final int j) {
        Utils.showTwoButtonDialog(getActivity(), getString(R.string.tishi), getString(R.string.delete),
                getString(R.string.cancel), getString(R.string.determine), false, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setScrollble(true);
                    }
                }, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setScrollble(true);
                        File file = new File(FileUtils.SCRATCH_VJC_IMAGE_ + i + FileUtils.SCRATCH_VJC_IMAGE_JPG);
                        if (file != null) {
                            // LogMgr.e("name:" +
                            // file.getAbsolutePath());
                            if (file.exists()) {
                                file.delete();
                            }
                            mDatas.remove(i);
                            mViews.remove(j);
                            initView();
                            if (j < mDatas.size()) {
                                mViewPager.setCurrentItem(j, false);
                            } else {
                                mViewPager.setCurrentItem(0, false);
                            }
                        }
                    }
                });
//		TextView tv = new TextView(manageFragment);
//		tv.setText(getString(R.string.delete));
//		tv.setGravity(Gravity.CENTER);
//		tv.setTextSize(24);
//		AlertDialog alertDialog = new AlertDialog.Builder(manageFragment)
//				.setView(tv)
//				.setPositiveButton(getString(R.string.determine),
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								mViewPager.setScrollble(true);
//								File file = new File(
//										FileUtils.SCRATCH_VJC_IMAGE_
//												+ i
//												+ FileUtils.SCRATCH_VJC_IMAGE_JPG);
//								if (file != null) {
//									// LogMgr.e("name:" +
//									// file.getAbsolutePath());
//									if (file.exists()) {
//										file.delete();
//									}
//									mDatas.remove(i);
//									mViews.remove(j);
//									initView();
//									if (j < mDatas.size()) {
//										mViewPager.setCurrentItem(j, false);
//									} else {
//										mViewPager.setCurrentItem(0, false);
//									}
//								}
//							}
//						})
//				.setNegativeButton(getString(R.string.cancel),
//						new DialogInterface.OnClickListener() {
//
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								mViewPager.setScrollble(true);
//							}
//						}).show();
//		alertDialog.setCanceledOnTouchOutside(false);
    }

}

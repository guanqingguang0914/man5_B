package com.abilix.explainer.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import android.media.Image;
import android.os.Environment;
import android.os.StatFs;

import com.abilix.explainer.utils.LogMgr;


public class ImageSaver implements Runnable {

	/**
	 * The JPEG image
	 */
	private  Image mImage;
	private  byte[] imageData;
	/**
	 * The file we save the image into.
	 */
	private final String mImgPath;

	private PictureFileSaveListener mFileSaveListener;

	public ImageSaver(byte[] data, String imgPath, PictureFileSaveListener pictureFileSaveListener) {
		imageData = data;
		mImgPath = imgPath;
		mFileSaveListener = pictureFileSaveListener;
	}

	@Override
	public void run() {


		save(imageData, mImgPath);
		//mImage.close();
	}

	private void save(byte[] data, String path) {
		try {
			// 判断是否装有SD卡
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				// 判断SD卡上是否有足够的空间
				String storage = Environment.getExternalStorageDirectory().toString();
				StatFs fs = new StatFs(storage);
				long available = fs.getAvailableBlocks() * fs.getBlockSize();
				if (available < data.length) { // 空间不足直接返回空 return
					mFileSaveListener.onFailed(RobotCameraStateCode.TAKE_PICTURE_NOT_ENOUGH_MEMORY_ERROR);
				}
				if (path==null) {
					LogMgr.e("照片保存路径为空");
				}
				LogMgr.d("照片保存路径："+path);
				File file = new File(path);
				if (!file.exists()) {
					File dir = new File(file.getParent());
					dir.mkdirs();
					file.createNewFile();
				}
				if (!file.exists()) {
					mFileSaveListener.onFailed(RobotCameraStateCode.TAKE_PICTURE_FILE_IS_NULL);
					return;
				}
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(data);
				fos.flush();
				mFileSaveListener.onSucess();
				LogMgr.e("保存照片文件成功");
				fos.close();
			} else {
				mFileSaveListener.onFailed(RobotCameraStateCode.TAKE_PICTURE_NO_SDCARD);
				LogMgr.e("未安装SD卡");
			}
		} catch (Exception e) {
			e.printStackTrace();
			mFileSaveListener.onFailed(RobotCameraStateCode.TAKE_PICTURE_WRITE_FILE_ERROR);
		}
	}
}

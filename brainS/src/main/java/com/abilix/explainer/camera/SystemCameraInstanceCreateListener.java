package com.abilix.explainer.camera;

public interface SystemCameraInstanceCreateListener {
	void onSucess(SystemCameraInstance instance);
	void onFailed(int state);
}

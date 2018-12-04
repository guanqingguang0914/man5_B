package com.abilix.explainer.view;

public interface IMainActivity {
    void showAlertDialog(int which, String message);

    void dismissAlertDialog(int which);

    void startAnimation();

    void stopAnimation();

    void showProgram(String programName);

    void dismissProgram();

    void display(String content);

    void finishDisplay();

    void showRecordView();

    void showPlayRecordView();

    void dimissRecordView();

    void showPicture(String picturePath);

    void dismissPicture();

    void sendBroadCastToBrain(String action);

    void responseResult();
}

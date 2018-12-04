package com.abilix.explainer;

public interface IExplainer {
    void doExplain(String filePath);

    void stopExplain();

    void pauseExplain();

    void resumeExplain();
}

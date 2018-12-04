package com.abilix.explainer;

public class FunNode {
    public int pos;
    public FunNode next;
    public int valStart;
    public int valLength;

    public FunNode() {
        this.pos = -1;
        this.next = null;
    }
}

package com.abilix.brain.ui;

import java.util.Arrays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.abilix.brain.utils.BrainUtils;
import com.abilix.brain.utils.LogMgr;

/**
 * 认识机器人中自定义划线View类。
 */
public class PathView extends View {
    private Paint paint;
    private Path path;
    private float startX = 0f, startY = 0f;

    // private long start;

    public PathView(Context context) {
        super(context);
        initCanvas();
    }

    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCanvas();
    }

    public PathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCanvas();
    }

    public void initCanvas() {
        if (path == null && paint == null) {
            path = new Path();
            paint = new Paint();
        }
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
        if (path == null && paint == null) {
            path = new Path();
            paint = new Paint();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                startX = event.getX();
                startY = event.getY();

                path.moveTo(startX, startY);
//			LogMgr.d("startX = "+startX+" startY = "+startY);
                setListener('D', startX, startY);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                float dx = Math.abs(moveX - startX);
                float dy = Math.abs(moveY - startY);

                if (path.isEmpty()) {
//				LogMgr.e("moveX = "+moveX+" moveY = "+ moveY + " path.isEmpty() = "+ path.isEmpty());
                    path.moveTo(moveX, moveY);
                    setListener('D', moveX, moveY);
                    break;
                }

                if (dx > 2 || dy > 2) {
//				LogMgr.i("moveX = "+moveX+" moveY = "+ moveY + " path.isEmpty() = "+ path.isEmpty());
                    path.lineTo(moveX, moveY);
                    setListener('M', moveX, moveY);
                }

                startX = moveX;
                startY = moveY;

                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }

    private void setListener(char c, float fx, float fy) {
        if (listener != null) {
            byte[] x = BrainUtils.float2byte(fx);
            byte[] y = BrainUtils.float2byte(fy);
            byte[] path = BrainUtils.byteMerger(x, y);
            byte[] by = Arrays.copyOf(path, path.length + 1);
            by[path.length] = (byte) c;
            listener.onPath(by);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    private onPathListener listener;

    public void setOnPathListener(onPathListener listener) {
        this.listener = listener;
    }

    public interface onPathListener {
        public void onPath(byte[] path);
    }

    public void destroy() {
        if (paint != null && path != null) {
            paint.reset();
            paint.setColor(Color.TRANSPARENT);
            paint.setStrokeWidth(0);
            path.reset();
            paint = null;
            path = null;
        }
    }

    public synchronized void clear() {
//		LogMgr.d("clear()");
        if (paint != null && path != null) {
            paint.reset();
            paint.setColor(Color.TRANSPARENT);
            paint.setStrokeWidth(0);
            path.reset();
            invalidate();
        }
        initCanvas();
    }
}

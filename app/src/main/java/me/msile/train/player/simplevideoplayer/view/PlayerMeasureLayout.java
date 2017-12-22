package me.msile.train.player.simplevideoplayer.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 视频绘制外层布局
 */

public class PlayerMeasureLayout extends FrameLayout {

    //宽高比
    private float aspectRatio = 0.5f;
    //是否全屏
    private boolean fullScreen;

    private int screenHeight, screenWidth;

    public PlayerMeasureLayout(Context context) {
        super(context);
        init();
    }

    public PlayerMeasureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerMeasureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerMeasureLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newWidthSpec;
        int newHeightSpec;
        if (fullScreen) {
            newWidthSpec = MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.EXACTLY);
            newHeightSpec = MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY);
        } else {
            newWidthSpec = MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY);
            newHeightSpec = MeasureSpec.makeMeasureSpec((int) (screenWidth * aspectRatio), MeasureSpec.EXACTLY);
        }
        super.onMeasure(newWidthSpec, newHeightSpec);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig != null) {
            setFullScreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
        }
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
        requestLayout();
    }

    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio != 0) {
            this.aspectRatio = aspectRatio;
        }
    }

}

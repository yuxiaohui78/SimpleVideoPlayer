package me.msile.train.player.simplevideoplayer.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;

import me.msile.train.player.simplevideoplayer.R;

/**
 * 简单视频操作界面
 */

public class SimplerPlayerControllerLayout extends FrameLayout implements IMediaController, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private int mCurrentLayout = LAYOUT_SMALL;
    private static final int LAYOUT_FULL = 1;             //全屏布局
    private static final int LAYOUT_SMALL = 0;            //默认布局

    private MediaController.MediaPlayerControl mPlayer;
    private static final int sDefaultTimeout = 4000;

    private View mCenterStateLay;                           //中间状态布局
    private View mBottomControlLay;                         //底部控制布局
    private ImageView mCenterStateIv;                       //中间状态
    private ProgressBar mLoadingPb;                         //loading
    private ImageView mStartPauseIv;                        //暂停/播放
    private ImageView mFullScreenIv;                        //全屏
    private SeekBar mProgressSeekBar;                       //进度条
    private TextView mStartTimeTv;                          //开始时间
    private TextView mEndTimeTv;                            //结束时间
    private TextView mCenterTimeTv;                         //中心视频播放时间

    private boolean mIsShowing;                             //是否正在展示
    private boolean mDragging;                              //是否正在拖动进度条
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private boolean enableFullScreen = true;                //是否可以全屏

    private OnSeekBarDragListener mSeekBarDragListener;
    private OnPlayerStateChangeListener mPlayerStateChangeListener;
    private OnScreenStateChangeListener mOnScreenStateChangeListener;

    private int mTouchSeekPro;
    private long mNewSeekPro;

    public SimplerPlayerControllerLayout(Context context) {
        super(context);
    }

    public SimplerPlayerControllerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimplerPlayerControllerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCenterStateLay = findViewById(R.id.center_state_fl);
        mBottomControlLay = findViewById(R.id.bottom_control_ll);
        mLoadingPb = (ProgressBar) findViewById(R.id.loading_pb);
        mCenterStateIv = (ImageView) findViewById(R.id.center_state_iv);
        mCenterStateIv.setOnClickListener(this);
        mStartPauseIv = (ImageView) findViewById(R.id.start_pause_iv);
        mStartPauseIv.setOnClickListener(this);
        mFullScreenIv = (ImageView) findViewById(R.id.full_screen_iv);
        mFullScreenIv.setOnClickListener(this);
        mFullScreenIv.setVisibility(enableFullScreen ? VISIBLE : GONE);
        mProgressSeekBar = (SeekBar) findViewById(R.id.progress_seek_bar);
        mProgressSeekBar.setOnSeekBarChangeListener(this);
        mStartTimeTv = (TextView) findViewById(R.id.start_time_tv);
        mEndTimeTv = (TextView) findViewById(R.id.end_time_tv);
        mCenterTimeTv = (TextView) findViewById(R.id.center_time_tv);
        initFormatter();
    }

    /**
     * 设置支持横向全屏模式
     *
     * @param enableFullScreen 横向全屏
     */
    public void setEnableFullScreen(boolean enableFullScreen) {
        this.enableFullScreen = enableFullScreen;
        if (mFullScreenIv != null) {
            mFullScreenIv.setVisibility(enableFullScreen ? VISIBLE : GONE);
        }
    }

    /**
     * 屏幕发生变化时
     */
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig != null) {
            boolean isFullScreen = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
            setFullScreen(isFullScreen);
            Context context = getContext();
            if (context instanceof Activity) {
                Window window = ((Activity) context).getWindow();
                if (window != null) {
                    if (isFullScreen) {
                        mCurrentLayout = LAYOUT_FULL;
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        window.setAttributes(params);
                        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

                        if (mOnScreenStateChangeListener != null) {
                            mOnScreenStateChangeListener.fullScreen();
                        }
                    } else {
                        mCurrentLayout = LAYOUT_SMALL;
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        window.setAttributes(params);
                        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

                        if (mOnScreenStateChangeListener != null) {
                            mOnScreenStateChangeListener.smallScreen();
                        }
                    }
                }
            }
        }
    }

    /**
     * 是否是全屏
     */
    public boolean isFullScreen() {
        return mCurrentLayout == LAYOUT_FULL;
    }

    private void initFormatter() {
        if (mFormatter == null) {
            mFormatBuilder = new StringBuilder();
            mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        }
    }

    public void setFullScreen(boolean isFullScreen) {
        if (mFullScreenIv != null) {
            mFullScreenIv.setImageResource(isFullScreen ? R.drawable.shrink_screen_icon : R.drawable.full_screen_icon);
        }
    }

    @Override
    public void onClick(View v) {
        if (checkPlayerIsNull()) {
            return;
        }
        int resId = v.getId();
        switch (resId) {
            case R.id.start_pause_iv:
            case R.id.center_state_iv:
                if (mPlayer.isPlaying()) {
                    doPause();
                } else {
                    doResume();
                }
                show(sDefaultTimeout);
                break;
            case R.id.full_screen_iv:
                if (mPlayer.isPlaying()) {
                    hide();
                }
                Context context = getContext();
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (mCurrentLayout == LAYOUT_SMALL) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        mCurrentLayout = LAYOUT_FULL;
                    } else if (mCurrentLayout == LAYOUT_FULL) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        mCurrentLayout = LAYOUT_SMALL;
                    }
                }
                break;
        }
    }

    /**
     * 暂停
     */
    @Override
    public void doPause() {
        if (checkPlayerIsNull()) {
            return;
        }
        mPlayer.pause();
        updatePausePlay();
        if (mPlayerStateChangeListener != null) {
            mPlayerStateChangeListener.onPlayerPause();
        }
    }

    /**
     * 恢复
     */
    @Override
    public void doResume() {
        if (checkPlayerIsNull()) {
            return;
        }
        mPlayer.start();
        updatePausePlay();
        if (mPlayerStateChangeListener != null) {
            mPlayerStateChangeListener.onPlayerStart();
        }
    }

    /**
     * 更新暂停/播放按钮
     */
    private void updatePausePlay() {
        if (checkPlayerIsNull()) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mStartPauseIv.setImageResource(R.drawable.pause_icon);
            mCenterStateLay.setVisibility(GONE);
        } else {
            mStartPauseIv.setImageResource(R.drawable.play_icon);
            mCenterStateLay.setVisibility(VISIBLE);
            mCenterStateIv.setImageResource(R.drawable.play_p_icon);
        }
    }

    /**
     * 设置当前时间
     */
    private int setProgress() {
        if (checkPlayerIsNull() || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgressSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgressSeekBar.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgressSeekBar.setSecondaryProgress(percent * 10);
        }

        if (mEndTimeTv != null)
            mEndTimeTv.setText(stringForTime(duration));

        if (mStartTimeTv != null) {
            mStartTimeTv.setText(stringForTime(position));
        }

        Log.i("PlayerControllerLayout", "=position=" + position + "==duration==" + duration);
        return position;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (checkPlayerIsNull()) {
            return;
        }
        if (!fromUser) {
            // We're not interested in programmatically generated changes to
            // the progress bar's position.
            return;
        }
        // changing the progress,but the player not seek
        long duration = mPlayer.getDuration();
        long newPosition = (duration * progress) / 1000L;
        if (mStartTimeTv != null)
            mStartTimeTv.setText(stringForTime((int) newPosition));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        show(3600000);

        mDragging = true;

        // By removing these pending progress messages we make sure
        // that a) we won't update the progress while the user adjusts
        // the seekbar and b) once the user is done dragging the thumb
        // we will post one of these messages to the queue again and
        // this ensures that there will be exactly one message queued up.
        removeCallbacks(mShowProgress);

        if (mSeekBarDragListener != null) {
            mSeekBarDragListener.onDragStart();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //when stop track,the player can seek to the progress
        if (checkPlayerIsNull()) {
            return;
        }
        long duration = mPlayer.getDuration();
        long newPosition = (duration * seekBar.getProgress()) / 1000L;
        mPlayer.seekTo((int) newPosition);
        if (mStartTimeTv != null)
            mStartTimeTv.setText(stringForTime((int) newPosition));

        mDragging = false;
        //        setProgress();
        updatePausePlay();

        show(sDefaultTimeout);

        // Ensure that progress is properly updated in the future,
        // the call to show() does not guarantee this because it is a
        // no-op if we are already showing.
        post(mShowProgress);

        if (mSeekBarDragListener != null) {
            mSeekBarDragListener.onDragEnd();
        }
    }

    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (mPlayer != null && !mDragging && mIsShowing && mPlayer.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000));
            }
        }
    };

    @Override
    public void hide() {
        if (mIsShowing) {
            mIsShowing = false;
            removeCallbacks(mFadeOut);
            removeCallbacks(mShowProgress);
            doHide();
        }
    }

    @Override
    public boolean isShowing() {
        return mIsShowing;
    }

    @Override
    public void setAnchorView(View view) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mStartPauseIv != null) {
            mStartPauseIv.setEnabled(enabled);
        }
        if (mCenterStateLay != null) {
            mCenterStateLay.setEnabled(enabled);
        }
        if (mCenterStateIv != null) {
            mCenterStateIv.setEnabled(enabled);
        }
        if (mProgressSeekBar != null) {
            mProgressSeekBar.setEnabled(enabled);
        }
        if (mFullScreenIv != null) {
            mFullScreenIv.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    @Override
    public void show(int timeout) {
        if (!mIsShowing) {
            doShow();
            //            setProgress();
            mIsShowing = true;
        }
        updatePausePlay();

        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        removeCallbacks(mShowProgress);
        post(mShowProgress);

        if (timeout != 0) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
    }

    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    @Override
    public void onPreparing() {
        if (mLoadingPb != null) {
            mLoadingPb.setVisibility(VISIBLE);
        }
        if (mCenterStateLay != null) {
            mCenterStateLay.setVisibility(GONE);
        }
    }

    @Override
    public void onPrepared() {
        if (mPlayerStateChangeListener != null) {
            mPlayerStateChangeListener.onPlayerStart();
        }
        if (mLoadingPb != null) {
            mLoadingPb.setVisibility(GONE);
        }
        if (mCenterStateLay != null) {
            mCenterStateLay.setVisibility(GONE);
        }
    }

    @Override
    public void onComplete() {
        setProgress();
        if (mCenterStateLay != null) {
            mCenterStateLay.setVisibility(VISIBLE);
            mCenterStateIv.setImageResource(R.drawable.refresh_icon);
        }
        if (mLoadingPb != null) {
            mLoadingPb.setVisibility(GONE);
        }
        if (mPlayerStateChangeListener != null) {
            mPlayerStateChangeListener.onPlayerPause();
            mPlayerStateChangeListener.onPlayerStop();
        }
    }

    @Override
    public void onError() {
    }

    private void doShow() {
        if (mBottomControlLay != null) {
            mBottomControlLay.setVisibility(VISIBLE);
        }
    }

    private void doHide() {
        if (mBottomControlLay != null) {
            mBottomControlLay.setVisibility(GONE);
        }
    }

    @Override
    public void showSeekTime(int touchDiff) {
        if (checkPlayerIsNull()) return;
        if (mCenterTimeTv != null) {
            final int width = isFullScreen() ? mProgressSeekBar.getWidth() * 4 : mProgressSeekBar.getWidth() * 2;
            long duration = mPlayer.getDuration();
            if (mTouchSeekPro == 0) {
                long position = mPlayer.getCurrentPosition();
                mTouchSeekPro = (int) (position * 1.0f / duration * width);
            }
            mTouchSeekPro += touchDiff;
            mNewSeekPro = (long) (mTouchSeekPro * 1.0f / width * duration);
            if (mNewSeekPro <= 0) {
                mNewSeekPro = 0;
            }
            if (mNewSeekPro >= duration) {
                mNewSeekPro = duration;
            }
            mCenterTimeTv.setText(stringForTime((int) mNewSeekPro));
            mCenterTimeTv.setVisibility(VISIBLE);
        }
    }

    @Override
    public void setSeekTime() {
        if (mCenterTimeTv != null) {
            mTouchSeekPro = 0;
            long currentPos = mPlayer.getCurrentPosition();
            if (Math.abs(currentPos - mNewSeekPro) > 1000L) {
                mPlayer.seekTo((int) mNewSeekPro);
            }
            mCenterTimeTv.setVisibility(GONE);
        }
    }

    private String stringForTime(int timeMs) {
        initFormatter();
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 检查播放器是否为null
     */
    private boolean checkPlayerIsNull() {
        return mPlayer == null || mStartPauseIv == null;
    }

    public void setSeekBarDragListener(OnSeekBarDragListener mSeekBarDragListener) {
        this.mSeekBarDragListener = mSeekBarDragListener;
    }

    public void setPlayerStateChangeListener(OnPlayerStateChangeListener mPlayerStateChangeListener) {
        this.mPlayerStateChangeListener = mPlayerStateChangeListener;
    }

    public void setScreenStateChangeListener(OnScreenStateChangeListener mOnScreenStateChangeListener) {
        this.mOnScreenStateChangeListener = mOnScreenStateChangeListener;
    }

    public interface OnSeekBarDragListener {
        void onDragStart();

        void onDragEnd();
    }

    public interface OnPlayerStateChangeListener {
        void onPlayerStart();

        void onPlayerPause();

        void onPlayerStop();
    }

    public interface OnScreenStateChangeListener {
        void fullScreen();

        void smallScreen();
    }
}

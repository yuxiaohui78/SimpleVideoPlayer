package me.msile.train.player.simplevideoplayer.view;

import android.view.View;
import android.widget.MediaController;

public interface IMediaController {
    void hide();

    boolean isShowing();

    void setAnchorView(View view);

    void setEnabled(boolean enabled);

    void setMediaPlayer(MediaController.MediaPlayerControl player);

    void show(int timeout);

    void show();

    void onPreparing();

    void onPrepared();

    void onComplete();

    void onError();

    void doPause();

    void doResume();

    void showSeekTime(int touchDiff);

    void setSeekTime();
}

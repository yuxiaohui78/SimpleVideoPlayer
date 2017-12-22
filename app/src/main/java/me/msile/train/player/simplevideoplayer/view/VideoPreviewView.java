package me.msile.train.player.simplevideoplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import me.msile.train.player.simplevideoplayer.R;

/**
 * 视频预览图片
 */

public class VideoPreviewView extends ImageView {

    private Drawable overLayerDraw;
    private Context context;

    public VideoPreviewView(Context context) {
        super(context);
        init(context);
    }

    public VideoPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        overLayerDraw = context.getResources().getDrawable(R.drawable.play_p_icon);
        overLayerDraw.setBounds(0, 0, overLayerDraw.getIntrinsicWidth(), overLayerDraw.getIntrinsicHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (overLayerDraw == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }
        canvas.save();
        canvas.translate(getWidth() / 2 - overLayerDraw.getIntrinsicWidth() / 2, getHeight() / 2 - overLayerDraw.getIntrinsicHeight() / 2);
        overLayerDraw.draw(canvas);
        canvas.restore();
    }

    /**
     * 设置预览图片(封面图)
     */
    public void setPreviewImage(String previewImage) {
        if (!TextUtils.isEmpty(previewImage)) {
//            Glide.with(context).load(previewImage).into(this);
        }
    }

}

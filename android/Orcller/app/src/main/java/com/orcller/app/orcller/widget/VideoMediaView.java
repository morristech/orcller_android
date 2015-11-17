package com.orcller.app.orcller.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.VideoMedia;

import pisces.psfoundation.utils.Log;
import pisces.psuikit.widget.PSVideoView;

/**
 * Created by pisces on 11/17/15.
 */
public class VideoMediaView extends MediaView implements PSVideoView.PlayStateListener {
    private boolean activity;
    private Button controlButton;
    private PSVideoView videoView;
    private Point controlButtonSize;

    public VideoMediaView(Context context) {
        super(context);
    }

    public VideoMediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoMediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoMediaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // ================================================================================================
    //  Overridden: MediaView
    // ================================================================================================

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.init(context, attrs, defStyleAttr, defStyleRes);

        activity = true;
        controlButtonSize = new Point(
                (int) getResources().getDimension(R.dimen.videomediaview_controlbutton_width),
                (int) getResources().getDimension(R.dimen.videomediaview_controlbutton_height));

        controlButton = new Button(context);
        controlButton.setVisibility(GONE);
        controlButton.setBackgroundColor(Color.BLACK);

        videoView = new PSVideoView(getContext());
        videoView.setBackgroundResource(android.R.color.transparent);
        videoView.setVisibility(GONE);
        videoView.setPlayStateListener(this);

        addView(videoView);
        addView(controlButton, new LayoutParams(controlButtonSize.x, controlButtonSize.y));

        controlButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playOrPause();
            }
        });
    }

    @Override
    protected void loadImages() {
        controlButton.setVisibility(getMedia() != null ? VISIBLE : GONE);
        videoView.setVideoPath(getVideo().videos.standard_resolution.url);

        loadImages(new CompleteHandler() {
            @Override
            public void onComplete() {
                if (delegate != null)
                    delegate.onComplete(imageView.getDrawable());
            }

            @Override
            public void onError() {
                if (delegate != null)
                    delegate.onError();

                if (activity)
                    controlButton.setVisibility(GONE);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        controlButton.setLayoutParams(getControlButtonLayoutParams());
    }

    // ================================================================================================
    //  Handler
    // ================================================================================================

    public void onComplete(MediaPlayer mp) {
        Log.i("onComplete");
        videoView.stopPlayback();
        videoView.seekTo(0);
        layoutControlButton(false, true);
    }

    public void onError(MediaPlayer mp, int what, int extra) {
        Log.i("onError");
    }

    public void onPause() {
        Log.i("onPause");
    }

    public void onPlay() {
        Log.i("onPlay");
    }

    public void onPrepare(MediaPlayer mp) {
        Log.i("onPrepare");
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void setActivity(boolean activity) {
        if (activity == this.activity)
            return;

        this.activity = activity;

        setActivated(activity);
        controlButton.setScaleX(getControlButtonScale());
        controlButton.setScaleY(getControlButtonScale());
        controlButton.setLayoutParams(getControlButtonLayoutParams());
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private ViewGroup.LayoutParams getControlButtonLayoutParams() {
        LayoutParams params = (LayoutParams) controlButton.getLayoutParams();
        int buttonWidth = Math.round(controlButtonSize.x * getControlButtonScale());
        int buttonHeight = Math.round(controlButtonSize.y * getControlButtonScale());

        if (activity) {
            params.setMargins(
                    (int) (getMeasuredWidth() - buttonWidth)/2,
                    (int) (getMeasuredHeight() - buttonHeight)/2, 0, 0);
        } else {
            int margin = (int) getResources().getDimension(R.dimen.videomediaview_controlbutton_margin);
            params.setMargins(
                    getMeasuredWidth() - buttonWidth - margin,
                    getMeasuredHeight() - buttonHeight - margin, 0, 0);
        }

        return params;
    }

    private float getControlButtonScale() {
        return activity ? 1.0f : 0.4f;
    }

    private VideoMedia getVideo() {
        return (VideoMedia) getMedia();
    }

    private void layoutControlButton(boolean isPlaying, boolean animated) {
        int buttonWidth = Math.round(controlButtonSize.x * getControlButtonScale());
        int buttonHeight = Math.round(controlButtonSize.y * getControlButtonScale());
        int margin = (int) getResources().getDimension(R.dimen.videomediaview_controlbutton_margin);
        final float scale = isPlaying ? 0.6f : getControlButtonScale();
        final float x = isPlaying ? getMeasuredWidth() - buttonWidth - margin : (getMeasuredWidth() - buttonWidth)/2;
        final float y = isPlaying ? getMeasuredHeight() - buttonHeight - margin : (getMeasuredHeight() - buttonHeight)/2;

        if (animated) {
            controlButton
                    .animate()
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(250)
                    .scaleX(2.0f)
                    .scaleY(2.0f)
                    .alpha(0)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            controlButton.setX(x);
                            controlButton.setY(y);

                            controlButton
                                    .animate()
                                    .setListener(null)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .setDuration(250)
                                    .scaleX(scale)
                                    .scaleY(scale)
                                    .alpha(1)
                                    .start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    })
                    .start();
        } else {
            controlButton.setX(x);
            controlButton.setY(y);
            controlButton.setScaleX(scale);
            controlButton.setScaleY(scale);
            controlButton.setAlpha(1);
        }
    }

    private void playOrPause() {
        if (videoView.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void pause() {
        videoView.pause();
        layoutControlButton(false, true);
    }

    public void play() {
        if (!videoView.isPlaying()) {
            videoView.requestFocus();
            videoView.start();
            videoView.setVisibility(VISIBLE);
            imageView.setVisibility(GONE);
            layoutControlButton(true, true);
        }
    }
}

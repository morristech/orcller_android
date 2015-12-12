package com.orcller.app.orcller.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.album.VideoMedia;

import de.greenrobot.event.EventBus;
import pisces.psuikit.ext.PSVideoView;

/**
 * Created by pisces on 11/17/15.
 */
public class VideoMediaView extends MediaView implements PSVideoView.PlayStateListener {
    private enum ControlButtonState {
        Pause,
        Play
    }

    private boolean allowsShowProgressBar;
    private boolean controlEnabled;
    private ControlButtonState controlButtonState;
    private Point controlButtonSize;
    private Button controlButton;
    private PSVideoView videoView;

    public VideoMediaView(Context context) {
        super(context);
    }

    public VideoMediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoMediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: MediaView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        controlEnabled = true;
        controlButtonState = ControlButtonState.Pause;
        controlButtonSize = new Point(
                (int) getResources().getDimension(R.dimen.videomediaview_controlbutton_width),
                (int) getResources().getDimension(R.dimen.videomediaview_controlbutton_height));

        controlButton = new Button(context);
        controlButton.setBackgroundResource(R.drawable.video_control_button);
        controlButton.setVisibility(GONE);

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
        allowsShowProgressBar = true;
        controlButton.setVisibility(getModel() != null ? VISIBLE : GONE);
        videoView.setVideoPath(getVideo().videos.standard_resolution.url);

        final MediaView self = this;

        loadImages(new CompleteHandler() {
            @Override
            public void onComplete() {
                if (delegate != null)
                    delegate.onCompleteImageLoad(self);
            }

            @Override
            public void onError() {
                if (delegate != null)
                    delegate.onError(self);

                if (controlEnabled)
                    controlButton.setVisibility(GONE);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Point point = getControlButtonPoint(controlButtonState.equals(ControlButtonState.Play));
        controlButton.setX(point.x);
        controlButton.setY(point.y);
    }

    @Override
    protected void setUpSubviews(Context context) {
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onComplete(MediaPlayer mp) {
        videoView.requestFocus();
        videoView.seekTo(0);
        layoutControlButton(ControlButtonState.Pause, true);
    }

    public void onError(MediaPlayer mp, int what, int extra) {
    }

    public void onPause() {
    }

    public void onPlay() {
    }

    public void onPrepare(MediaPlayer mp) {
        progressBar.setVisibility(GONE);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public void setControlEnabled(boolean controlEnabled) {
        if (controlEnabled == this.controlEnabled)
            return;

        this.controlEnabled = controlEnabled;

        if (!controlEnabled) {
            imageView.setVisibility(VISIBLE);
            videoView.setVisibility(GONE);
        }

        Point point = getControlButtonPoint(controlEnabled && controlButtonState.equals(ControlButtonState.Play));
        controlButton.setScaleX(getControlButtonScale());
        controlButton.setScaleY(getControlButtonScale());
        controlButton.setX(point.x);
        controlButton.setY(point.y);
        controlButton.setEnabled(controlEnabled);
        videoView.setEnabled(controlEnabled);
    }

    public void pause() {
        allowsShowProgressBar = false;
        progressBar.setVisibility(GONE);
        videoView.pause();
        layoutControlButton(ControlButtonState.Pause, true);
        EventBus.getDefault().post(new Event(Event.DID_END_VIDEO_PLAYING, this));
    }

    public void play() {
        if (!videoView.isPlaying()) {
            if (allowsShowProgressBar)
                progressBar.setVisibility(VISIBLE);

            imageView.setVisibility(GONE);
            videoView.setVisibility(VISIBLE);
            videoView.requestFocus();
            videoView.start();
            layoutControlButton(ControlButtonState.Play, true);
            EventBus.getDefault().post(new Event(Event.DID_START_VIDEO_PLAYING, this));
        }
    }

    public void playOrPause() {
        if (!controlEnabled)
            return;

        if (videoView.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void stop() {
        videoView.stopPlayback();
        videoView.requestFocus();
        videoView.seekTo(0);
        videoView.setVisibility(GONE);
        imageView.setVisibility(VISIBLE);
        controlButton.animate().cancel();
        layoutControlButton(ControlButtonState.Pause, false);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private Point getControlButtonPoint(boolean isPlaying) {
        int buttonWidth = Math.round(controlButtonSize.x * getControlButtonScale());
        int buttonHeight = Math.round(controlButtonSize.y * getControlButtonScale());
        int margin = (int) getResources().getDimension(R.dimen.videomediaview_controlbutton_margin);
        return new Point(isPlaying ? getMeasuredWidth() - buttonWidth - margin : (getMeasuredWidth() - buttonWidth)/2,
                isPlaying ? getMeasuredHeight() - buttonHeight - margin : (getMeasuredHeight() - buttonHeight)/2);
    }

    private float getControlButtonScale() {
        return controlEnabled ? 1.0f : 0.4f;
    }

    private VideoMedia getVideo() {
        return (VideoMedia) getModel();
    }

    private void layoutControlButton(final ControlButtonState controlButtonState, boolean animated) {
        if (controlButtonState.equals(this.controlButtonState))
            return;

        final VideoMediaView self = this;
        final boolean isPlaying = controlButtonState.equals(ControlButtonState.Play);
        final Point point = getControlButtonPoint(!isPlaying);
        final float scale = isPlaying ? 0.6f : getControlButtonScale();

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
                            Point point = getControlButtonPoint(isPlaying);
                            controlButton.setSelected(isPlaying);
                            controlButton.setX(point.x);
                            controlButton.setY(point.y);
                            controlButton
                                    .animate()
                                    .setListener(null)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .setDuration(250)
                                    .scaleX(scale)
                                    .scaleY(scale)
                                    .alpha(1)
                                    .start();
                            self.controlButtonState = controlButtonState;
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
            controlButton.setSelected(isPlaying);
            controlButton.setX(point.x);
            controlButton.setY(point.y);
            controlButton.setScaleX(scale);
            controlButton.setScaleY(scale);
            controlButton.setAlpha(1);

            this.controlButtonState = controlButtonState;
        }
    }

    public static class Event extends pisces.psfoundation.event.Event {
        public static final String DID_START_VIDEO_PLAYING = "didStartVideoPlaying";
        public static final String DID_END_VIDEO_PLAYING = "didEndVideoPlaying";

        public Event(String type, Object target) {
            super(type, target);
        }
    }
}

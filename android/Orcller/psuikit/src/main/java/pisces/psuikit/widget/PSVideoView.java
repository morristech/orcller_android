package pisces.psuikit.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

/**
 * Created by pisces on 11/17/15.
 */
public class PSVideoView extends VideoView {
    private PlayStateListener listener;

    public PSVideoView(Context context) {
        super(context);

        setListeners();
    }

    public PSVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setListeners();
    }

    public PSVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setListeners();
    }

    public void setPlayStateListener(PlayStateListener listener) {
        this.listener = listener;
    }

    // ================================================================================================
    //  Overridden: VideoView
    // ================================================================================================

    @Override
    public void pause() {
        super.pause();

        if (listener != null)
            listener.onPause();
    }

    @Override
    public void start() {
        super.start();

        if (listener != null)
            listener.onPlay();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setListeners() {
        setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (listener != null)
                    listener.onError(mp, what, extra);
                return false;
            }
        });

        setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (listener != null)
                    listener.onComplete(mp);
            }
        });

        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (listener != null)
                    listener.onPrepare(mp);
            }
        });
    }

    // ================================================================================================
    //  Interface: PlayStateListener
    // ================================================================================================

    public static interface PlayStateListener {
        void onComplete(MediaPlayer mp);
        void onError(MediaPlayer mp, int what, int extra);
        void onPause();
        void onPlay();
        void onPrepare(MediaPlayer mp);
    }
}

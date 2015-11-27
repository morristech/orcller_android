package pisces.psuikit.ext;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by pisces on 11/17/15.
 */
public class PSVideoView extends VideoView implements PSComponent {
    private boolean immediatelyUpdating;
    private boolean initializedSubviews;
    private PlayStateListener listener;

    public PSVideoView(Context context) {
        super(context);

        initProperties(context, null, 0, 0);
    }

    public PSVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initProperties(context, attrs, 0, 0);
    }

    public PSVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initProperties(context, attrs, defStyleAttr, 0);
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
    //  Public
    // ================================================================================================

    public boolean isImmediatelyUpdating() {
        return immediatelyUpdating;
    }

    public void setImmediatelyUpdating(boolean immediatelyUpdating) {
        this.immediatelyUpdating = immediatelyUpdating;
    }

    public void setPlayStateListener(PlayStateListener listener) {
        this.listener = listener;
    }

    public void invalidateProperties() {
        if (isAttachedToWindow() || immediatelyUpdating)
            commitProperties();
    }

    public void validateProperties() {
        commitProperties();
    }

    // ================================================================================================
    //  Protected
    // ================================================================================================

    protected void commitProperties() {
    }

    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setListeners();
    }

    protected void setUpSubviews(Context context) {
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

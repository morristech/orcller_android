package pisces.psuikit.manager;

import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.HashMap;

import pisces.android.R;
import pisces.psuikit.widget.PSProgressBar;

/**
 * Created by pisces on 11/9/15.
 */
public class ProgressBarManager {
    public static final int DISMISS_MODE_NONE = 0;
    public static final int DISMISS_MODE_ERROR = 1;
    public static final int DISMISS_MODE_COMPLETE = 2;
    private static HashMap<String, ProgressBarManager.ViewInfo> viewInfoMap = new HashMap<String, ProgressBarManager.ViewInfo>();

    public static void hide(Activity activity) {
        hide(activity, DISMISS_MODE_NONE, null);
    }

    public static void hide(Activity activity, int mode) {
        hide(activity, mode, null);
    }

    public static void hide(final Activity activity, int mode, final DismissHandler dismissHandler) {
        final String key = String.valueOf(activity.hashCode());

        if (viewInfoMap.containsKey(key)) {
            final ProgressBarManager.ViewInfo viewInfo = viewInfoMap.get(key);
            final ViewGroup layout = (ViewGroup) viewInfo.activity.findViewById(android.R.id.content).getRootView();

            viewInfo.progressBar.dismiss();
            viewInfo.relativeLayout.removeView(viewInfo.progressBar);

            final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    layout.removeView(viewInfo.relativeLayout);
                    viewInfoMap.remove(key);

                    if (dismissHandler != null)
                        dismissHandler.onDismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            };

            if (mode != DISMISS_MODE_NONE) {
                int drawable = mode == DISMISS_MODE_ERROR ? R.drawable.psprogressbar_error : R.drawable.psprogressbar_complete;
                final ProgressBar progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleLarge);
                progressBar.setIndeterminate(true);
                progressBar.setIndeterminateDrawable(activity.getResources().getDrawable(drawable));
                viewInfo.relativeLayout.addView(progressBar);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.psprogressbar_dismiss_ani);
                        animation.setAnimationListener(animationListener);
                        progressBar.startAnimation(animation);
                    }
                }, 500);
            } else {
                animationListener.onAnimationEnd(null);
            }
        }
    }

    public static void show(Activity activity) {
        String key = String.valueOf(activity.hashCode());

        if (!viewInfoMap.containsKey(key)) {
            ViewGroup layout = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();

            PSProgressBar progressBar = new PSProgressBar(activity);
            progressBar.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams params = new
                    RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            RelativeLayout relativeLayout = new RelativeLayout(activity);

            relativeLayout.setGravity(Gravity.CENTER);
            layout.addView(relativeLayout, params);
            progressBar.show(relativeLayout);
            viewInfoMap.put(key, new ProgressBarManager.ViewInfo(activity, relativeLayout, progressBar));
        }
    }

    private static class ViewInfo {
        public Activity activity;
        public RelativeLayout relativeLayout;
        public PSProgressBar progressBar;

        public ViewInfo(Activity activity, RelativeLayout relativeLayout, PSProgressBar progressBar) {
            this.activity = activity;
            this.relativeLayout = relativeLayout;
            this.progressBar = progressBar;
        }
    }

    public interface DismissHandler {
        void onDismiss();
    }
}

package pisces.psuikit.manager;

import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
    private static boolean showing;
    private static HashMap<String, ProgressBarManager.ViewInfo> viewInfoMap = new HashMap<String, ProgressBarManager.ViewInfo>();

    public static void hide(Activity activity) {
        final String key = String.valueOf(activity.hashCode());

        if (viewInfoMap.containsKey(key)) {
            ProgressBarManager.ViewInfo viewInfo = viewInfoMap.get(key);
            ViewGroup layout = (ViewGroup) viewInfo.activity.findViewById(android.R.id.content).getRootView();

            viewInfo.relativeLayout.removeView(viewInfo.progressBar);
            layout.removeView(viewInfo.relativeLayout);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            viewInfoMap.remove(key);

            showing = false;
        }
    }

    public static boolean isShowing() {
        return showing;
    }

    public static void show(Activity activity) {
        show(activity, false);
    }

    public static void show(Activity activity, boolean modal) {
        String key = String.valueOf(activity.hashCode());

        if (!viewInfoMap.containsKey(key)) {
            ViewGroup layout = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();

            ProgressBar progressBar = new ProgressBar(activity);
            progressBar.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams params = new
                    RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
            RelativeLayout relativeLayout = new RelativeLayout(activity);

            if (modal) {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            relativeLayout.setGravity(Gravity.CENTER);
            layout.addView(relativeLayout, params);
            relativeLayout.addView(progressBar);
            viewInfoMap.put(key, new ProgressBarManager.ViewInfo(activity, relativeLayout, progressBar));

            showing = true;
        }
    }

    private static class ViewInfo {
        public Activity activity;
        public RelativeLayout relativeLayout;
        public ProgressBar progressBar;

        public ViewInfo(Activity activity, RelativeLayout relativeLayout, ProgressBar progressBar) {
            this.activity = activity;
            this.relativeLayout = relativeLayout;
            this.progressBar = progressBar;
        }
    }

    public interface DismissHandler {
        void onDismiss();
    }
}

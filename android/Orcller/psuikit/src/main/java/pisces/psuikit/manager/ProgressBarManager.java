package pisces.psuikit.manager;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.HashMap;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 11/9/15.
 */
public class ProgressBarManager {
    private static HashMap<String, ProgressBarManager.ViewInfo> viewInfoMap = new HashMap<String, ProgressBarManager.ViewInfo>();

    public static void hide() {
        hide(Application.getTopActivity());
    }

    public static void hide(Activity activity) {
        String key = String.valueOf(activity.hashCode());

        if (viewInfoMap.containsKey(key)) {
            ProgressBarManager.ViewInfo viewInfo = viewInfoMap.get(key);

            viewInfo.relativeLayout.removeView(viewInfo.progressBar);
            viewInfo.viewGroup.removeView(viewInfo.relativeLayout);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            viewInfoMap.remove(key);
        }
    }

    public static void hide(ViewGroup viewGroup) {
        String key = String.valueOf(viewGroup.hashCode());

        if (viewInfoMap.containsKey(key)) {
            ProgressBarManager.ViewInfo viewInfo = viewInfoMap.get(key);

            viewInfo.relativeLayout.removeView(viewInfo.progressBar);
            viewGroup.removeView(viewInfo.relativeLayout);
            viewInfoMap.remove(key);
        }
    }

    public static void show() {
        show(Application.getTopActivity());
    }

    public static void show(boolean modal) {
        show(Application.getTopActivity(), modal);
    }

    public static void show(boolean modal, int progressStyle) {
        show(Application.getTopActivity(), modal, progressStyle);
    }

    public static void show(Activity activity) {
        show(activity, false);
    }

    public static void show(Activity activity, boolean modal) {
        show(activity, modal, android.R.attr.progressBarStyle);
    }

    public static void show(Activity activity, boolean modal, int progressStyle) {
        String key = String.valueOf(activity.hashCode());

        if (!viewInfoMap.containsKey(key)) {
            ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();

            ProgressBar progressBar = new ProgressBar(activity, null, progressStyle);
            progressBar.setVisibility(View.VISIBLE);

            RelativeLayout relativeLayout = new RelativeLayout(activity);

            if (modal) {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            relativeLayout.setGravity(Gravity.CENTER);
            viewGroup.addView(relativeLayout);
            relativeLayout.addView(progressBar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            viewInfoMap.put(key, new ViewInfo(viewGroup, relativeLayout, progressBar));
        }
    }

    public static void show(ViewGroup viewGroup) {
        show(viewGroup, android.R.attr.progressBarStyleSmallInverse);
    }

    public static void show(ViewGroup viewGroup, int progressStyle) {
        if (viewGroup == null)
            return;

        String key = String.valueOf(viewGroup.hashCode());

        if (!viewInfoMap.containsKey(key)) {
            ProgressBar progressBar = new ProgressBar(viewGroup.getContext(), null, progressStyle);
            progressBar.setVisibility(View.VISIBLE);

            RelativeLayout relativeLayout = new RelativeLayout(viewGroup.getContext());

            relativeLayout.setGravity(Gravity.CENTER);
            viewGroup.addView(relativeLayout);
            relativeLayout.addView(progressBar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            viewInfoMap.put(key, new ViewInfo(viewGroup, relativeLayout, progressBar));
        }
    }

    private static class ViewInfo {
        public ViewGroup viewGroup;
        public RelativeLayout relativeLayout;
        public ProgressBar progressBar;

        public ViewInfo(ViewGroup viewGroup, RelativeLayout relativeLayout, ProgressBar progressBar) {
            this.viewGroup = viewGroup;
            this.relativeLayout = relativeLayout;
            this.progressBar = progressBar;
        }
    }

    public interface DismissHandler {
        void onDismiss();
    }
}

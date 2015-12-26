package pisces.psuikit.manager;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.HashMap;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.GraphicUtils;

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

            viewInfo.layout.removeView(viewInfo.progressBar);
            viewInfo.viewGroup.removeView(viewInfo.layout);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            viewInfoMap.remove(key);
        }
    }

    public static void hide(ViewGroup viewGroup) {
        String key = String.valueOf(viewGroup.hashCode());

        if (viewInfoMap.containsKey(key)) {
            ProgressBarManager.ViewInfo viewInfo = viewInfoMap.get(key);

            viewInfo.layout.removeView(viewInfo.progressBar);
            viewGroup.removeView(viewInfo.layout);
            viewInfoMap.remove(key);
        }
    }

    public static ProgressBar show() {
        return show(Application.getTopActivity());
    }

    public static ProgressBar show(boolean modal) {
        return show(Application.getTopActivity(), modal);
    }

    public static ProgressBar show(boolean modal, int progressStyle) {
        return show(Application.getTopActivity(), modal, progressStyle);
    }

    public static ProgressBar show(Activity activity) {
        return show(activity, false);
    }

    public static ProgressBar show(Activity activity, boolean modal) {
        return show(activity, modal, android.R.attr.progressBarStyle);
    }

    public static ProgressBar show(Activity activity, boolean modal, int progressStyle) {
        String key = String.valueOf(activity.hashCode());

        if (!viewInfoMap.containsKey(key)) {
            ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content).getRootView();
            ProgressBar progressBar = new ProgressBar(activity, null, progressStyle);
            FrameLayout layout = new FrameLayout(activity);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;

            if (modal) {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            progressBar.setVisibility(View.VISIBLE);
            layout.addView(progressBar, params);
            viewGroup.addView(layout);
            viewInfoMap.put(key, new ViewInfo(viewGroup, layout, progressBar));
            return progressBar;
        }
        return null;
    }

    public static ProgressBar show(ViewGroup viewGroup) {
        return show(viewGroup, android.R.attr.progressBarStyleSmallInverse);
    }

    public static ProgressBar show(ViewGroup viewGroup, int progressStyle) {
        if (viewGroup == null)
            return null;

        String key = String.valueOf(viewGroup.hashCode());

        if (!viewInfoMap.containsKey(key)) {
            ProgressBar progressBar = new ProgressBar(viewGroup.getContext(), null, progressStyle);
            FrameLayout layout = new FrameLayout(viewGroup.getContext());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;

            if (viewGroup.getLayoutParams().height == FrameLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.topMargin = GraphicUtils.convertDpToPixel(15);

            progressBar.setVisibility(View.VISIBLE);
            layout.addView(progressBar, params);
            viewGroup.addView(layout, layoutParams);
            viewInfoMap.put(key, new ViewInfo(viewGroup, layout, progressBar));
            return progressBar;
        }

        return null;
    }

    private static class ViewInfo {
        public ViewGroup viewGroup;
        public FrameLayout layout;
        public ProgressBar progressBar;

        public ViewInfo(ViewGroup viewGroup, FrameLayout layout, ProgressBar progressBar) {
            this.viewGroup = viewGroup;
            this.layout = layout;
            this.progressBar = progressBar;
        }
    }

    public interface DismissHandler {
        void onDismiss();
    }
}

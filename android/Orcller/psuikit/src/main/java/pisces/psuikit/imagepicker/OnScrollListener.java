package pisces.psuikit.imagepicker;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by pisces on 11/25/15.
 */
public class OnScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int i, int i2) {
        int childCount = recyclerView.getChildCount();
        int width = recyclerView.getChildAt(0).getWidth();
        int padding = (recyclerView.getWidth() - width) / 2;

        for (int j = 0; j < childCount; j++) {
            View v = recyclerView.getChildAt(j);
            float rate = 0;
            ;
            if (v.getLeft() <= padding) {
                if (v.getLeft() >= padding - v.getWidth()) {
                    rate = (padding - v.getLeft()) * 1f / v.getWidth();
                } else {
                    rate = 1;
                }
                v.setScaleY(1 - rate * 0.1f);
                v.setScaleX(1 - rate * 0.1f);

            } else {
                if (v.getLeft() <= recyclerView.getWidth() - padding) {
                    rate = (recyclerView.getWidth() - padding - v.getLeft()) * 1f / v.getWidth();
                }
                v.setScaleY(0.9f + rate * 0.1f);
                v.setScaleX(0.9f + rate * 0.1f);
            }
        }
    }
}

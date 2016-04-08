package com.orcller.app.orcller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import com.orcller.app.orcller.animator.AnimationOptions;
import com.orcller.app.orcller.animator.FlipAnimator;
import com.orcller.app.orcller.manager.AlbumIndexManager;
import com.orcller.app.orcller.manager.FlipAlbumIndexManager;
import com.orcller.app.orcller.model.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pisces on 11/19/15.
 */
public class AlbumFlipView extends AlbumView {
    public AlbumFlipView(Context context) {
        super(context);
    }

    public AlbumFlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumFlipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: AlbumView
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        pageCountAtOne = 2;
        animator = new FlipAnimator();
        templateViewClass = FlipView.class;

        setPadding(0, 8, 0, 8);
        setClipChildren(false);
        container.setClipChildren(false);
    }

    @Override
    protected void alignContainer(int pageIndex, int duration, Interpolator interpolator, boolean animated) {
        if (getWidth() < 1 || getModel() == null)
            return;

        int x;
        int pw = getPageWidth();
        int cw = container.getLayoutParams().width;

        if (pageIndex == 0)
            x = (getWidth() - cw)/2 - (pw/2);
        else if ((pageIndex * 2) + 1 > getModel().pages.count)
            x = (getWidth() - cw)/2 + (pw/2);
        else
            x = (getWidth() - cw)/2;

        if (animated) {
            container.animate()
                    .setDuration(duration - 50)
                    .setInterpolator(interpolator)
                    .x(x)
                    .y(getPaddingTop())
                    .start();
        } else {
            container.setX(x);
        }
    }

    @Override
    protected boolean cannotAnimate(float value) {
        return false;
    }

    @Override
    protected AlbumIndexManager createAlbumIndexManager() {
        return new FlipAlbumIndexManager();
    }

    @Override
    public TemplateView getFocusedView(float changedValue) {
        if (changedValue > 0)
            return getSelectedView().getCurrentPageView().getModel() != null ? getSelectedView() : visibleViews.get(CENTER_INDEX_OF_VISIBLE_VIEWS - 1);
        return visibleViews.get(CENTER_INDEX_OF_VISIBLE_VIEWS - 1);
    }

    @Override
    protected List<Page> getPages(int pageIndex) {
        int frontIndex = pageIndex * pageCountAtOne;
        int backIndex = frontIndex + 1;
        Page p1 = frontIndex < getModel().pages.count ? getModel().pages.getPageAtIndex(frontIndex) : null;
        Page p2 = backIndex < getModel().pages.count ? getModel().pages.getPageAtIndex(backIndex) : null;

        List<Page> pages = new ArrayList<Page>();
        if (p1 != null)     pages.add(p1);
        if (p2 != null)     pages.add(p2);

        return pages.size() > 0 ? pages : null;
    }

    @Override
    protected boolean hitTest(TemplateView view, MotionEvent event) {
        FlipView flipView = (FlipView) view;
        int addendX = flipView.getDirection().equals(AnimationOptions.Direction.Left) ? -view.getWidth() : 0;
        float xMin = container.getX() + view.getX() + addendX;
        float xMax = container.getX() + view.getX() + addendX + view.getWidth();
        float yMin = container.getY() + view.getY();
        float yMax = container.getY() + view.getY() + view.getHeight();
        return event.getX() >= xMin &&
                event.getX() <= xMax &&
                event.getY() >= yMin &&
                event.getY() <= yMax;
    }

    @Override
    protected void initTemplateView(TemplateView view, int index, boolean resizable) {
        FlipView flipView = (FlipView) view;
        AnimationOptions.Direction direction = index < CENTER_INDEX_OF_VISIBLE_VIEWS ? AnimationOptions.Direction.Left : AnimationOptions.Direction.Right;

        if (resizable) {
            flipView.setDirection(AnimationOptions.Direction.Right);
            flipView.setVisibility(VISIBLE);
            flipView.getLayoutParams().width = getPageWidth();
            flipView.getLayoutParams().height = getPageHeight();
            flipView.setX(getPageWidth());
        }

        if (!flipView.equals(focusedView))
            flipView.setDirection(direction);

        flipView.getShadowImageView().setVisibility(getModel().pages.total_count < 2 ? GONE : VISIBLE);

        if (flipView.getDirection().equals(AnimationOptions.Direction.Left)) {
            container.bringChildToFront(view);
        } else {
            container.moveChildToBack(view);
        }
    }

    @Override
    protected boolean isTemplateViewHidden(TemplateView view) {
        if (view.getPages() != null)
            return view.getPages().size() < 1;
        return true;
    }

    @Override
    protected void sortTemplateViews(AnimationOptions.Direction direction) {
    }

    @Override
    protected void updatePageDescLabels() {

    }

    @Override
    protected void updateTemplateView(TemplateView view, AnimationOptions.Direction direction, boolean reset) {
        if (reset) {
            FlipView flipView = (FlipView) view;

            flipView.setDirection(direction);
            container.moveChildToBack(view);
        } else {
            container.bringChildToFront(view);
        }
    }
}

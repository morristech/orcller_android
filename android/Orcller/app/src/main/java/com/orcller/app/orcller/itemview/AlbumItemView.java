package com.orcller.app.orcller.itemview;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.event.CoeditEvent;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.Comments;
import com.orcller.app.orcller.model.Contributors;
import com.orcller.app.orcller.model.Favorites;
import com.orcller.app.orcller.model.Likes;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import com.orcller.app.orcller.widget.AlbumFlipView;
import com.orcller.app.orcller.widget.AlbumInfoProfileView;
import com.orcller.app.orcller.widget.FlipView;
import com.orcller.app.orcller.widget.PageView;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;
import pisces.psuikit.widget.PSButton;

/**
 * Created by pisces on 12/6/15.
 */
public class AlbumItemView extends PSLinearLayout implements AlbumFlipView.Delegate, View.OnClickListener {
    public static final int COEDIT = 1<<0;
    public static final int HEART = 1<<1;
    public static final int COMMENT = 1<<2;
    public static final int STAR = 1<<3;

    public enum ButtonType {
        None,
        Coedit,
        Comment,
        CommentList,
        Heart,
        HeartList,
        Options,
        Star,
        StarList,
        Control
    }

    private boolean allowsShowOptionIcon;
    private int buttonVisiblity = COEDIT | HEART | COMMENT | STAR;
    private Album model;
    private Delegate delegate;
    private AlbumInfoProfileView albumInfoProfileView;
    private AlbumFlipView albumFlipView;
    private TextView descriptionTextView;
    private TextView heartTextView;
    private TextView commentTextView;
    private TextView starTextView;
    private PSButton controlButton;
    private PSButton coeditButton;
    private PSButton heartButton;
    private PSButton commentButton;
    private PSButton starButton;

    public AlbumItemView(Context context) {
        super(context);
    }

    public AlbumItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlbumItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.itemview_album, this);

        albumInfoProfileView = (AlbumInfoProfileView) findViewById(R.id.albumInfoProfileView);
        albumFlipView = (AlbumFlipView) findViewById(R.id.albumFlipView);
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        heartTextView = (TextView) findViewById(R.id.heartTextView);
        commentTextView = (TextView) findViewById(R.id.commentTextView);
        starTextView = (TextView) findViewById(R.id.starTextView);
        controlButton = (PSButton) findViewById(R.id.controlButton);
        coeditButton = (PSButton) findViewById(R.id.coeditButton);
        heartButton = (PSButton) findViewById(R.id.heartButton);
        commentButton = (PSButton) findViewById(R.id.commentButton);
        starButton = (PSButton) findViewById(R.id.starButton);

        albumInfoProfileView.setBackgroundResource(R.drawable.background_bordered_white);
        albumInfoProfileView.getOptionsIcon().setOnClickListener(this);
        albumFlipView.setDelegate(this);
        controlButton.setOnClickListener(this);
        heartTextView.setOnClickListener(this);
        commentTextView.setOnClickListener(this);
        starTextView.setOnClickListener(this);
        coeditButton.setOnClickListener(this);
        heartButton.setOnClickListener(this);
        commentButton.setOnClickListener(this);
        starButton.setOnClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (albumFlipView.getPageWidth() < 1) {
            albumFlipView.setPageWidth(getWidth() / 2);
            albumFlipView.setPageHeight(getWidth() / 2);
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public AlbumInfoProfileView getAlbumInfoProfileView() {
        return albumInfoProfileView;
    }

    public AlbumFlipView getAlbumFlipView() {
        return albumFlipView;
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public boolean isAllowsShowOptionIcon() {
        return allowsShowOptionIcon;
    }

    public void setAllowsShowOptionIcon(boolean allowsShowOptionIcon) {
        if (allowsShowOptionIcon == this.allowsShowOptionIcon)
            return;

        this.allowsShowOptionIcon = allowsShowOptionIcon;
        albumInfoProfileView.getOptionsIcon().setVisibility(allowsShowOptionIcon ? VISIBLE : GONE);
    }

    public int getButtonVisiblity() {
        return buttonVisiblity;
    }

    public void setButtonVisiblity(int buttonVisiblity) {
        if (buttonVisiblity == this.buttonVisiblity)
            return;

        this.buttonVisiblity = buttonVisiblity;

        coeditButton.setVisibility((buttonVisiblity & COEDIT) == COEDIT ? VISIBLE : GONE);
        heartButton.setVisibility((buttonVisiblity & HEART) == HEART ? VISIBLE : GONE);
        commentButton.setVisibility((buttonVisiblity & COMMENT) == COMMENT ? VISIBLE : GONE);
        starButton.setVisibility((buttonVisiblity & STAR) == STAR ? VISIBLE : GONE);
    }

    public int getDescriptionMode() {
        return albumInfoProfileView.getDescriptionMode();
    }

    public void setDescriptionMode(int descriptionMode) {
        albumInfoProfileView.setDescriptionMode(descriptionMode);
    }

    public Album getModel() {
        return model;
    }

    public void setModel(Album model) {
        if (ObjectUtils.equals(model, this.model))
            return;

        if (this.model != null)
            this.model.pages.deleteHeavyData();

        this.model = model;

        modelChanged();
        AlbumDataProxy.getDefault().remainPages(model);
    }

    public void loadRemainPages() {
        albumFlipView.loadRemainPages();
    }

    public void reload() {
        albumFlipView.reload();
        albumInfoProfileView.reload();
        updateDisplayList();
    }

    public void updateDisplayList() {
        descriptionTextView.setText(model.desc);
        descriptionTextView.setVisibility(TextUtils.isEmpty(model.desc) ? GONE : VISIBLE);
        heartTextView.setText(getInfoText(heartTextView));
        heartTextView.setVisibility(model.likes.total_count > 0 ? VISIBLE : GONE);
        commentTextView.setText(getInfoText(commentTextView));
        commentTextView.setVisibility(model.comments.total_count > 0 ? VISIBLE : GONE);
        starTextView.setText(getInfoText(starTextView));
        starTextView.setVisibility(model.favorites.total_count > 0 ? VISIBLE : GONE);
        coeditButton.setSelected(model.contributors.isParticipated());
        heartButton.setText(getButtonText(heartButton));
        heartButton.setSelected(model.likes.isParticipated());
        commentButton.setSelected(model.comments.isParticipated());
        starButton.setText(getButtonText(starButton));
        starButton.setSelected(model.favorites.isParticipated());
        setLeftMargin(commentTextView, heartTextView);
        setLeftMargin(starTextView, commentTextView);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof Model.Event) {
            Model.Event casted = (Model.Event) event;

            if (Model.Event.SYNCHRONIZE.equals(casted.getType())) {
                if (casted.getTarget() instanceof Album) {
                    synchronizeAlbum((Album) casted.getTarget());
                } else if (casted.getTarget() instanceof AlbumAdditionalListEntity) {
                    synchronizeAlbumInfo((AlbumAdditionalListEntity) casted.getTarget());
                }
            } else if (Model.Event.CHANGE.equals(casted.getType())) {
                if (casted.getTarget() instanceof Album) {
                    changeAlbum((Album) casted.getTarget());
                } else if (casted.getTarget() instanceof Comments) {
                    synchronizeAlbumInfo((Comments) casted.getTarget());
                }
            }
        } else if (event instanceof CoeditEvent &&
                CoeditEvent.CHANGE.equals(((CoeditEvent) event).getType())) {
            synchronizeAlbumInfo(((CoeditEvent) event).getContributors());
        }
    }

    /**
     * View.OnClickListener
     */
    public void onClick(View v) {
        if (delegate == null)
            return;

        ButtonType type = ButtonType.None;

        if (heartTextView.equals(v))
            type = ButtonType.HeartList;
        else if (commentTextView.equals(v))
            type = ButtonType.CommentList;
        else if (starTextView.equals(v))
            type = ButtonType.StarList;
        else if (albumInfoProfileView.getOptionsIcon().equals(v))
            type = ButtonType.Options;
        else if (coeditButton.equals(v))
            type = ButtonType.Coedit;
        else if (heartButton.equals(v))
            type = ButtonType.Heart;
        else if (commentButton.equals(v))
            type = ButtonType.Comment;
        else if (starButton.equals(v))
            type = ButtonType.Star;
        else if (controlButton.equals(v))
            type = ButtonType.Control;

        delegate.onClick(this, type, v);
    }

    /**
     * AlbumFlipView.Delegate
     */

    public void onCancelPanning(AlbumFlipView view) {
        if (delegate != null)
            delegate.onCancelPanning(this, view);
    }

    public void onChangePageIndex(AlbumFlipView view, int pageIndex) {
        if (delegate != null)
            delegate.onChangePageIndex(this, view, pageIndex);
    }

    public void onLoadRemainPages(AlbumFlipView view) {
        if (delegate != null)
            delegate.onLoadRemainPages(this, view);
    }

    public void onPlay(AlbumFlipView view) {
        controlButton.setSelected(true);

        if (delegate != null)
            delegate.onPlay(this, view);
    }

    public void onPause(AlbumFlipView view) {
        controlButton.setSelected(false);

        if (delegate != null)
            delegate.onPause(this, view);
    }

    public void onStartLoadRemainPages(AlbumFlipView view) {
        if (delegate != null)
            delegate.onStartLoadRemainPages(this, view);
    }

    public void onStartPanning(AlbumFlipView view) {
        if (delegate != null)
            delegate.onStartPanning(this, view);
    }

    public void onStop(AlbumFlipView view) {
        controlButton.setSelected(false);

        if (delegate != null)
            delegate.onStop(this, view);
    }

    public void onTap(AlbumFlipView view, FlipView flipView, PageView pageView) {
        if (delegate != null)
            delegate.onTap(this, view, flipView, pageView);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private AlbumAdditionalListEntity getAlbumAdditionalListEntity(AlbumAdditionalListEntity entity) {
        if (entity instanceof Comments)
            return model.comments;
        if (entity instanceof Contributors)
            return model.contributors;
        if (entity instanceof Favorites)
            return model.favorites;
        if (entity instanceof Likes)
            return model.likes;
        return null;
    }

    private String getButtonText(PSButton button) {
        if (heartButton.equals(button))
            return getResources().getString(model.likes.isParticipated() ? R.string.w_unheart : R.string.w_heart);
        if (starButton.equals(button))
            return getResources().getString(model.favorites.isParticipated() ? R.string.w_unstar : R.string.w_star);
        return null;
    }

    private String getInfoText(TextView textView) {
        if (heartTextView.equals(textView))
            return SharedObject.getAlbumInfoText(model.likes);
        if (commentTextView.equals(textView))
            return SharedObject.getAlbumInfoText(model.comments);
        if (starTextView.equals(textView))
            return SharedObject.getAlbumInfoText(model.favorites);
        return null;
    }

    private void modelChanged() {
        albumFlipView.setModel(model);
        albumFlipView.setPageIndex(model.default_page_index);
        albumInfoProfileView.setModel(model);
        updateDisplayList();
    }

    private void postAlbumInfoSynchronizeEvent(AlbumAdditionalListEntity model) {
        updateDisplayList();

        if (delegate != null)
            delegate.onAlbumInfoSynchronize(this, model);
    }

    private void postPageChangeEvent() {
        albumFlipView.reload();

        if (delegate != null)
            delegate.onPageChange(this);
    }

    private void postAlbumSynchronizeEvent() {
        reload();

        if (delegate != null)
            delegate.onAlbumSynchronize(this);
    }

    private void setLeftMargin(View target, View relativeView) {
        LayoutParams params = (LayoutParams) target.getLayoutParams();
        params.leftMargin = relativeView.getVisibility() == View.VISIBLE ? GraphicUtils.convertDpToPixel(8) : 0;
    }

    private void changeAlbum(Album album) {
        if (album.equals(model)) {
            postPageChangeEvent();
        } else if (album.id == model.id) {
            model.pages.synchronize(album.pages, new Runnable() {
                @Override
                public void run() {
                    postPageChangeEvent();
                }
            });
        }
    }

    private void synchronizeAlbum(Album album) {
        if (album.equals(model)) {
            postAlbumSynchronizeEvent();
        } else if (album.id == model.id) {
            model.synchronize(album, new Runnable() {
                @Override
                public void run() {
                    postAlbumSynchronizeEvent();
                }
            });
        }
    }

    private void synchronizeAlbumInfo(final AlbumAdditionalListEntity model) {
        AlbumAdditionalListEntity target = getAlbumAdditionalListEntity(model);

        if (target != null) {
            if (model.equals(target)) {
                postAlbumInfoSynchronizeEvent(model);
            } else if (model.id == target.id) {
                target.synchronize(model, new Runnable() {
                    @Override
                    public void run() {
                        postAlbumInfoSynchronizeEvent(model);
                    }
                });
            }
        }
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public interface Delegate {
        void onAlbumInfoSynchronize(AlbumItemView itemView, AlbumAdditionalListEntity model);
        void onAlbumSynchronize(AlbumItemView itemView);
        void onClick(AlbumItemView itemView, ButtonType type, View view);
        void onCancelPanning(AlbumItemView itemView, AlbumFlipView view);
        void onChangePageIndex(AlbumItemView itemView, AlbumFlipView view, int pageIndex);
        void onLoadRemainPages(AlbumItemView itemView, AlbumFlipView view);
        void onPageChange(AlbumItemView itemView);
        void onPause(AlbumItemView itemView, AlbumFlipView view);
        void onPlay(AlbumItemView itemView, AlbumFlipView view);
        void onStartLoadRemainPages(AlbumItemView itemView, AlbumFlipView view);
        void onStartPanning(AlbumItemView itemView, AlbumFlipView view);
        void onStop(AlbumItemView itemView, AlbumFlipView view);
        void onTap(AlbumItemView itemView, AlbumFlipView view, FlipView flipView, PageView pageView);
    }
}
package com.orcller.app.orcller.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.event.CoeditEvent;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.Contributor;
import com.orcller.app.orcller.model.album.Contributors;
import com.orcller.app.orcller.model.api.ApiCoedit;
import com.orcller.app.orcller.proxy.CoeditDataProxy;
import com.orcller.app.orcllermodules.utils.AlertDialogUtils;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.widget.PSButton;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/10/15.
 */
public class CoeditButton extends PSButton implements View.OnClickListener {
    private long albumId;
    private Model model;
    private Delegate delegate;
    private ProgressDialog progressDialog;

    public CoeditButton(Context context) {
        super(context);
    }

    public CoeditButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoeditButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSButton
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.initProperties(context, attrs, defStyleAttr, defStyleRes);

        int pd = GraphicUtils.convertDpToPixel(12);

        textView.setTextColor(getResources().getColorStateList(R.drawable.color_button_coedit));
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextSize(12);
        setPadding(pd, 0, pd, 0);
        setBackgroundResource(R.drawable.background_ripple_coeditbutton);
        setMinimumWidth(GraphicUtils.convertDpToPixel(70));
        setVisibility(INVISIBLE);
        setOnClickListener(this);
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog = null;
        }
    }

    @Override
    public boolean invalidDataLoading() {
        boolean invalid = super.invalidDataLoading();

        if (!invalid)
            progressDialog = ProgressDialog.show(getContext(), null, getResources().getString(R.string.w_processing));

        return invalid;
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    public void onClick(View v) {
        if (model == null)
            return;

        if (model instanceof Album) {
            if (album().contributors.contributor_status == Contributor.Status.Ask.value())
                unask();
            else if (album().contributors.contributor_status == Contributor.Status.Invite.value())
                accept();
            else if (album().contributors.contributor_status == Contributor.Status.None.value())
                ask();
        } else if (model instanceof Contributor) {
            if (contributor().contributor_status == Contributor.Status.Invite.value())
                uninvite();
            if (contributor().contributor_status == Contributor.Status.Ask.value())
                accept();
            if (contributor().contributor_status == Contributor.Status.None.value() && !contributor().isMe())
                invite();
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) throws Exception {
        setModel(model, 0);
    }

    public void setModel(Model model, long albumId) throws Exception {
        if (ObjectUtils.equals(model, this.model))
            return;

        if (!(model instanceof Album) && !(model instanceof Contributor))
            throw new Exception("Model must be instance of Album or Controbutor!");

        if ((model instanceof Contributor) && albumId < 1)
            throw new Exception("AlbumId need when model is instance of Contributor!");

        this.model = model;
        this.albumId = album() != null ? album().id : albumId;

        modelChanged();
    }

    public void reload() {
        modelChanged();
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private Album album() {
        return  model instanceof Album ? (Album) model : null;
    }

    private Contributor contributor() {
        return  model instanceof Contributor ? (Contributor) model : null;
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    private String getTitle() {
        if (model instanceof Album) {
            Album album = album();
            if (album.isMine()) {
                if (album.contributors.contributor_status == Contributor.Status.Ask.value())
                    return getContext().getString(R.string.w_cancel);
                if (album.contributors.contributor_status == Contributor.Status.Invite.value())
                    return getContext().getString(R.string.w_confirm);
            } else {
                if (album.contributors.contributor_status == Contributor.Status.Ask.value())
                    return getContext().getString(R.string.w_cancel_asking);
                if (album.contributors.contributor_status == Contributor.Status.Invite.value())
                    return getContext().getString(R.string.w_confirm_collaboration);
                if (album.contributors.contributor_status == Contributor.Status.None.value())
                    return getContext().getString(R.string.w_ask_collaboration);
            }
        } else if (model instanceof Contributor){
            if (contributor().contributor_status == Contributor.Status.Invite.value())
                return getContext().getString(R.string.w_cancel);
            if (contributor().contributor_status == Contributor.Status.Ask.value())
                return getContext().getString(R.string.w_confirm);
            if (contributor().contributor_status == Contributor.Status.None.value() && !contributor().isMe())
                return getContext().getString(R.string.w_add);
        }
        return null;
    }

    private void modelChanged() {
        setText(getTitle());
        setVisibility(TextUtils.isEmpty(getText()) ? GONE : VISIBLE);
    }

    /**
     * Methods for request
     */
    private void accept() {
        String contributorId = album() != null ? album().contributors.contributor_id : contributor().contributor_id;
        call(CoeditDataProxy.getDefault().service().accept(contributorId), Contributor.Status.Accept);
    }

    private void ask() {
        call(CoeditDataProxy.getDefault().service().ask(albumId), Contributor.Status.Ask);
    }

    private void invite() {
        call(CoeditDataProxy.getDefault().service().invite(albumId, String.valueOf(contributor().user_uid)), Contributor.Status.Invite);
    }

    private void unask() {
        call(CoeditDataProxy.getDefault().service().unask(albumId), Contributor.Status.None);
    }

    private void uninvite() {
        call(CoeditDataProxy.getDefault().service().uninvite(albumId, contributor().user_uid), Contributor.Status.None);
    }

    private void call(final Call<ApiCoedit.ContributorsRes> call, final Contributor.Status toStatus) {
        if (invalidDataLoading())
            return;

        final CoeditButton target = this;
        final Runnable error = new Runnable() {
            @Override
            public void run() {
                endDataLoading();
                AlertDialogUtils.retry(R.string.m_fail_common, new Runnable() {
                    @Override
                    public void run() {
                        call(call, toStatus);
                    }
                });
            }
        };

        CoeditDataProxy.getDefault().enqueueCall(call, new Callback<ApiCoedit.ContributorsRes>() {
            @Override
            public void onResponse(Response<ApiCoedit.ContributorsRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    final Contributors contributors = response.body().entity;

                    if (album() != null) {
                        album().contributors.synchronize(contributors, new Runnable() {
                            @Override
                            public void run() {
                                album().contributors.contributor_status = toStatus.value();
                                endDataLoading();
                                modelChanged();

                                if (delegate != null)
                                    delegate.onSync(target, contributors);

                                EventBus.getDefault().post(new CoeditEvent(CoeditEvent.SYNC, target, contributors));
                            }
                        }, true);
                    } else {
                        contributor().contributor_status = toStatus.value();
                        endDataLoading();
                        modelChanged();

                        if (delegate != null)
                            delegate.onChange(target, contributors);

                        EventBus.getDefault().post(new CoeditEvent(CoeditEvent.CHANGE, target, contributors));
                    }
                } else {
                    if (DEBUG)
                        e("Api Error", response.body());

                    error.run();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                error.run();
            }
        });
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public static interface Delegate {
        void onChange(CoeditButton target, Contributors contributors);
        void onSync(CoeditButton target, Contributors contributors);
    }
}

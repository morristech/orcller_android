package com.orcller.app.orcller.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.model.Comments;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.proxy.AlbumDataProxy;
import pisces.psuikit.event.SoftKeyboardEvent;
import pisces.psuikit.utils.AlertDialogUtils;
import pisces.psuikit.keyboard.SoftKeyboardUtils;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.ObjectUtils;
import pisces.psuikit.ext.PSLinearLayout;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.orcller.app.orcller.BuildConfig.DEBUG;
import static pisces.psfoundation.utils.Log.e;

/**
 * Created by pisces on 12/3/15.
 */
public class CommentInputView extends PSLinearLayout implements View.OnClickListener {
    public static final int COMMENT_TYPE_ALBUM = 1;
    public static final int COMMENT_TYPE_PAGE = 2;
    private int commentType = COMMENT_TYPE_ALBUM;
    private long id;
    private Comments model;
    private Delegate delegate;
    private EditText editText;
    private Button postButton;
    private ProgressDialog progressDialog;

    public CommentInputView(Context context) {
        super(context);
    }

    public CommentInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommentInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ================================================================================================
    //  Overridden: PSLinearLayout
    // ================================================================================================

    @Override
    protected void initProperties(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(context, R.layout.view_comment_input, this);

        editText = (EditText) findViewById(R.id.editText);
        postButton = (Button) findViewById(R.id.postButton);

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                postButton.setEnabled(!TextUtils.isEmpty(editText.getText().toString().trim()));
            }
        });

        postButton.setOnClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean invalidDataLoading() {
        boolean invalid = super.invalidDataLoading();

        if (!invalid)
            progressDialog = ProgressDialog.show(getContext(), null, getResources().getString(R.string.w_posting));

        return invalid;
    }

    @Override
    public void endDataLoading() {
        super.endDataLoading();

        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog = null;
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public int getCommentType() {
        return commentType;
    }

    public void setCommentType(int commentType) {
        this.commentType = commentType;
    }

    public Comments getModel() {
        return model;
    }

    public void setModel(Comments model, long id) {
        if (ObjectUtils.equals(model, this.model))
            return;

        this.model = model;
        this.id = id;

        if (model != null && id > 0)
            setVisibility(View.VISIBLE);
    }

    public Delegate getDelegate() {
        return delegate;
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public CharSequence getText() {
        return editText.getText();
    }

    public void clear() {
        editText.setText(null);
        clearFocus();
    }

    public void clearFocus() {
        editText.setCursorVisible(false);
        SoftKeyboardUtils.hide(this);
    }

    public void setFocus() {
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        SoftKeyboardUtils.show(editText);
    }

    // ================================================================================================
    //  Listener
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(Object event) {
        if (event instanceof SoftKeyboardEvent) {
            SoftKeyboardEvent casted = (SoftKeyboardEvent) event;

            if (casted.getType().equals(SoftKeyboardEvent.SHOW)) {
                editText.setCursorVisible(true);
            } else if (casted.getType().equals(SoftKeyboardEvent.HIDE)) {
                editText.setCursorVisible(false);
            }
        }
    }

    /**
     * View.OnClickListener
     */
    public void onClick(final View v) {
        Call<ApiAlbum.CommentsRes> call = createCall();

        if (call == null || invalidDataLoading())
            return;

        clearFocus();

        final CommentInputView target = this;
        final Runnable error = new Runnable() {
            @Override
            public void run() {
                endDataLoading();
                AlertDialogUtils.retry(R.string.m_fail_comment, new Runnable() {
                    @Override
                    public void run() {
                        onClick(v);
                    }
                });
            }
        };

        AlbumDataProxy.getDefault().enqueueCall(call, new Callback<ApiAlbum.CommentsRes>() {
            @Override
            public void onResponse(final Response<ApiAlbum.CommentsRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    clear();
                    model.synchronize(response.body().entity, new Runnable() {
                        @Override
                        public void run() {
                            endDataLoading();

                            if (delegate != null)
                                delegate.onCompletePost(target, response.body().entity);
                        }
                    }, true);
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
    //  Private
    // ================================================================================================

    private Call<ApiAlbum.CommentsRes> createCall() {
        String message = getText().toString().trim();
        if (commentType == COMMENT_TYPE_ALBUM)
            return AlbumDataProxy.getDefault().service().comment(id, message);
        if (commentType == COMMENT_TYPE_PAGE)
            return AlbumDataProxy.getDefault().service().commentOfPage(id, message);
        return null;
    }

    // ================================================================================================
    //  Interface: Delegate
    // ================================================================================================

    public interface Delegate {
        void onCompletePost(CommentInputView commentInputView, Comments comments);
    }
}

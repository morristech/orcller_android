package pisces.instagram.sdk.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.net.URI;
import java.net.URL;
import java.util.List;

import de.greenrobot.event.EventBus;
import pisces.android.instagramsdk.R;
import pisces.instagram.sdk.model.OAuth2;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.MapUtils;
import pisces.psuikit.ext.PSActionBarActivity;

/**
 * Created by pisces on 11/13/15.
 */
public class InstagramLoginActivity extends PSActionBarActivity {
    private static final String AUTHORIZE_URL = "https://api.instagram.com/oauth/authorize";
    private Toolbar toolbar;
    private WebView webView;
    private OAuth2 resource;

    // ================================================================================================
    //  Overridden: Activity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_instagram_login);

        resource = (OAuth2) getIntent().getSerializableExtra("resource");
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Instagram");

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new InstagramLoginWebViewClient(this));
        webView.loadUrl(getAuthorizeUrl());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        toolbar = null;
        webView = null;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private String getAuthorizeUrl() {
        return AUTHORIZE_URL + "?" + MapUtils.toQueryString(resource.getParameters(OAuth2.InstagramParametersType.Login));
    }

    // ================================================================================================
    //  Class: WebViewClient
    // ================================================================================================

    private class InstagramLoginWebViewClient extends WebViewClient {
        private Context context;
        private ProgressDialog progressBarDialog;

        public InstagramLoginWebViewClient(Context context) {
            super();

            this.context = context;
            progressBarDialog = new ProgressDialog(context);
            progressBarDialog.setMessage("Loading ...");
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(resource.getRedirectURI())) {
                EventBus.getDefault().post(
                        new InstagramLoginComplete((Activity) context, Uri.parse(url).getQueryParameter("code")));
                return false;
            }

            view.loadUrl(url);
            return true;
        }

        public void onLoadResource(WebView view, String url) {
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (!progressBarDialog.isShowing()) {
                progressBarDialog.show();
            }
        }

        public void onPageFinished(WebView view, String url) {
            progressBarDialog.hide();
            getSupportActionBar().setTitle(view.getTitle());
        }
    }

    public static class InstagramLoginComplete {
        private Activity activity;
        private String code;

        public InstagramLoginComplete(Activity activity, String code) {
            this.activity = activity;
            this.code = code;
        }

        public Activity getActivity() {
            return activity;
        }

        public String getCode() {
            return code;
        }
    }
}

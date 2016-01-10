package pisces.instagram.sdk.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.greenrobot.event.EventBus;
import pisces.android.instagramsdk.R;
import pisces.instagram.sdk.model.OAuth2;
import pisces.psfoundation.event.Event;
import pisces.psfoundation.utils.MapUtils;
import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/13/15.
 */
public class InstagramLoginActivity extends PSActionBarActivity {
    private static final String AUTHORIZE_URL = "https://api.instagram.com/oauth/authorize";
    private WebView webView;
    private OAuth2 resource;

    // ================================================================================================
    //  Overridden: PSActionBarActivity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_instagram_login);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.title_login));

        resource = (OAuth2) getIntent().getSerializableExtra("resource");
        webView = (WebView) findViewById(R.id.webView);

        webView.setWebViewClient(new InstagramLoginWebViewClient(this));
        webView.loadUrl(getAuthorizeUrl());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        EventBus.getDefault().post(new LoginEvent(LoginEvent.CANCEL, this));

        super.onBackPressed();
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

        public InstagramLoginWebViewClient(Context context) {
            super();

            this.context = context;
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(resource.getRedirectURI())) {
                EventBus.getDefault().post(
                        new LoginEvent(LoginEvent.COMPLETE, context, Uri.parse(url).getQueryParameter("code")));
                return false;
            }

            view.loadUrl(url);
            return true;
        }

        public void onLoadResource(WebView view, String url) {
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            ProgressBarManager.show();
        }

        public void onPageFinished(WebView view, String url) {
            ProgressBarManager.hide();
        }
    }

    // ================================================================================================
    //  Class: LoginEvent
    // ================================================================================================

    public static class LoginEvent extends Event {
        public static final String CANCEL = "cancel";
        public static final String COMPLETE = "complete";

        public LoginEvent(String type, Object target) {
            super(type, target);
        }

        public LoginEvent(String type, Object target, Object object) {
            super(type, target, object);
        }

        public Activity getActivity() {
            return (Activity) getTarget();
        }

        public String getCode() {
            return (String) getObject();
        }
    }
}

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
        getSupportActionBar().setTitle("Instagram");

        resource = (OAuth2) getIntent().getSerializableExtra("resource");
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new InstagramLoginWebViewClient(this));
        webView.loadUrl(getAuthorizeUrl());
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                        new InstagramLoginComplete((Activity) context, Uri.parse(url).getQueryParameter("code")));
                return false;
            }

            view.loadUrl(url);
            return true;
        }

        public void onLoadResource(WebView view, String url) {
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            ProgressBarManager.show((Activity) context);
        }

        public void onPageFinished(WebView view, String url) {
            ProgressBarManager.hide((Activity) context);
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

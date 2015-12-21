package com.orcller.app.orcllermodules.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.orcller.app.orcllermodules.R;

import pisces.psuikit.ext.PSActionBarActivity;
import pisces.psuikit.manager.ProgressBarManager;

/**
 * Created by pisces on 11/12/15.
 */
public class WebViewActivity extends PSActionBarActivity {
    private boolean titleEnabled;
    private String title;
    private String url;
    private WebView webView;

    // ================================================================================================
    //  Overridden: Activity
    // ================================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        setToolbar((Toolbar) findViewById(R.id.toolbar));
        setProperties();

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient(this));

        if (!titleEnabled)
            getSupportActionBar().setTitle(title);

        if (url != null)
            webView.loadUrl(url);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void setProperties() {
        try {
            Uri uri = getIntent().getData();
            url = uri.getQueryParameter("url");
            title = uri.getQueryParameter("title");
            titleEnabled = Boolean.valueOf(uri.getQueryParameter("titleEnabled"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class WebViewClient extends android.webkit.WebViewClient {
        private Context context;

        public WebViewClient(Context context) {
            super();

            this.context = context;
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            ProgressBarManager.show((Activity) context);
        }

        public void onPageFinished(WebView view, String url) {
            ProgressBarManager.hide((Activity) context);

            if (titleEnabled)
                getSupportActionBar().setTitle(view.getTitle());
        }
    }
}

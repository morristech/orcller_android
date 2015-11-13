package com.orcller.app.orcllermodules.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.orcller.app.orcllermodules.R;

import pisces.psuikit.ext.PSActionBarActivity;

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
        webView.setWebChromeClient(new WebViewClient());

        if (!titleEnabled)
            getSupportActionBar().setTitle(title);

        if (url != null)
            webView.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        title = null;
        url = null;
        webView = null;
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

    private class WebViewClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);

            if (titleEnabled)
                getSupportActionBar().setTitle(title);
        }
    }
}

package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.webkit.WebView;

/**
 * Created by rbanks on 11/14/16.
 */

public class PhotoPageActivity extends SingleFragmentActivity {

    private WebView m_webView;

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        PhotoPageFragment fragment = PhotoPageFragment.newInstance(getIntent().getData());
        return fragment;
    }

    @Override
    public void onBackPressed() {
        //how would we reference the current webview instance in order to find out if we can go back?
        if(m_webView != null && m_webView.canGoBack())
            m_webView.goBack();
        else
            super.onBackPressed();
    }

    public void setWebView (WebView webView) {
        m_webView = webView;
    }
}

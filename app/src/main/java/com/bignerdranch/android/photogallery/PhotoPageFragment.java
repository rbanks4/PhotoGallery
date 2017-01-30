package com.bignerdranch.android.photogallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by rbanks on 11/14/16.
 */

public class PhotoPageFragment extends VisibleFragment {
    private static final String ARG_URI = "photo_page_url";

    private Uri m_uri;
    private WebView m_webView;
    private ProgressBar m_progressBar;

    public static PhotoPageFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);

        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_uri = getArguments().getParcelable(ARG_URI);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_page, container, false);

        m_progressBar = (ProgressBar)v.findViewById(R.id.fragment_photo_page_progress_bar);
        m_progressBar.setMax(100); //WebChromeClient reports in range 0 to 100

        m_webView = (WebView) v.findViewById(R.id.fragment_photo_page_web_view);
        m_webView.getSettings().setJavaScriptEnabled(true);
        m_webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int newProgress) {
                if (newProgress == 100)
                    m_progressBar.setVisibility(View.GONE);
                else {
                    m_progressBar.setVisibility(View.VISIBLE);
                    m_progressBar.setProgress(newProgress);
                }
            }
            public void onReceivedTitle(WebView webView, String title) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                activity.getSupportActionBar().setSubtitle(title);
            }
        });
        m_webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        m_webView.loadUrl(m_uri.toString());

        //we want to make sure that we pass the webview back to the activity if the activity is a photoPageActivity
        if (getActivity() instanceof PhotoPageActivity) {
            PhotoPageActivity activity = (PhotoPageActivity) getActivity();
            activity.setWebView(m_webView);
        }

        return v;
    }

    public WebView getWebView() {
        return m_webView;
    }

}

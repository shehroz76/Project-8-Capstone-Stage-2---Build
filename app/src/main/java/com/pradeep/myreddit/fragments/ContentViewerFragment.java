package com.pradeep.myreddit.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pradeep.myreddit.R;
import com.pradeep.myreddit.events.HideContentViewerEvent;
import com.pradeep.myreddit.events.OauthCallbackEvent;
import com.pradeep.myreddit.network.AuthManager;
import com.pradeep.myreddit.utils.Helpers;
import com.pradeep.myreddit.utils.OnBackPressedListener;

import java.util.Map;

import de.greenrobot.event.EventBus;

public class ContentViewerFragment extends Fragment implements OnBackPressedListener {

    public static final String TAG = ContentViewerFragment.class.getSimpleName();
    public static final String URL_BUNDLE_KEY = "url_key";
    public static final String CONTENT_TITLE_BUNDLE_KEY = "content_title_key";
    private Toolbar toolbar;
    private WebView webView;
    private String contentTitle;
    private String contentUrl;
    private boolean isVisible;
    private static final String IS_VISIBLE_BUNDLE_KEY = "is_visible_key";

    public static ContentViewerFragment newInstance(String contentTitle, String url) {
        Bundle bundle = new Bundle();
        bundle.putString(CONTENT_TITLE_BUNDLE_KEY, contentTitle);
        bundle.putString(URL_BUNDLE_KEY, url);
        ContentViewerFragment contentViewerFragment = new ContentViewerFragment();
        contentViewerFragment.setArguments(bundle);

        return contentViewerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_viewer, container, false);

        /**
         * Find views
         */

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        webView = (WebView) view.findViewById(R.id.webview);

        /**
         * Setup toolbar
         */

        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        /**
         * Configure browser settings
         */

        WebSettings settings = webView.getSettings();
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptEnabled(true);
        // fit content to screen
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        // enable pinch to zoom
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        // enable local storage
        settings.setDomStorageEnabled(true);
        // set dark background color
        webView.setBackgroundColor(getResources().getColor(R.color.background_material_dark));
        // force to load url in the webview itself instead of opening default browser
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(AuthManager.REDIRECT_URI)) {
                    Map<String, String> params = Helpers.parseUrlQueryParams(url);

                    EventBus.getDefault().post(new OauthCallbackEvent(
                            params.get("code"),
                            params.get("state"),
                            params.get("error")
                    ));

                    onBackPressed();
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (contentTitle == null) {
                    toolbar.setTitle(contentUrl);
                    toolbar.setSubtitle(null);
                } else {
                    toolbar.setTitle(contentTitle);
                    toolbar.setSubtitle(contentUrl);
                }
            }
        });

        /**
         * Retrieve content info and load URL
         */

        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            contentTitle = bundle.getString(CONTENT_TITLE_BUNDLE_KEY);
            contentUrl = bundle.getString(URL_BUNDLE_KEY);
        } else {
            contentTitle = savedInstanceState.getString(CONTENT_TITLE_BUNDLE_KEY);
            contentUrl = savedInstanceState.getString(URL_BUNDLE_KEY);
        }

        loadContent(contentTitle, contentUrl);

        /**
         * Restore isVisible from saved instance state and hide this fragment depending if its value is false. This
         * needs to be done because for some reason the fragment manager shows all hidden fragments on configuration
         * change. isVisible is set to true initially since onHiddenChanged only starts getting called after the fragment
         * is hidden for the first time, i.e. when savedInstanceState != null.
         */

        isVisible = savedInstanceState == null || savedInstanceState.getBoolean(IS_VISIBLE_BUNDLE_KEY);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // event needs to be posted here because activity registers the eventbus in its onResume() method
        if (!isVisible) {
            EventBus.getDefault().post(new HideContentViewerEvent());
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        isVisible = !hidden;
    }

    public void loadContent(String contentTitle, String contentUrl) {
        this.contentTitle = contentTitle;
        this.contentUrl = contentUrl;

        toolbar.setTitle(R.string.loading);
        toolbar.setSubtitle(contentUrl);

        webView.loadUrl(contentUrl);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_VISIBLE_BUNDLE_KEY, isVisible);
        outState.putString(CONTENT_TITLE_BUNDLE_KEY, contentTitle);
        outState.putString(URL_BUNDLE_KEY, contentUrl);
    }

    @Override
    public void onBackPressed() {
        webView.loadUrl("about:blank");

        EventBus.getDefault().post(new HideContentViewerEvent());

        isVisible = false;
    }
}

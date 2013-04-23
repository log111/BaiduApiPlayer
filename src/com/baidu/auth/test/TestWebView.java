package com.baidu.auth.test;

import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TestWebView extends WebViewClient {
	
	private static final String TAG = "TestWebView";
	
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		Log.d(TAG, "url="+url);
		return super.shouldOverrideUrlLoading(view, url);
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		Log.d(TAG, "url="+url+" finish loading");
		super.onPageFinished(view, url);
	}
}

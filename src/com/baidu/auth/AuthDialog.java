package com.baidu.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.baidu.auth.test.Debug;

public class AuthDialog extends Activity{
	private static final String TAG = "AuthDialog";
	private static final Debug debug = new Debug(TAG);

	//data received for this Activity by Intent
	public static final String REDIRECT_URL = "redirectUrl";
	public static final String REQUEST_URL = "requestUrl";
	
	private String mTaskId;
	private LocalBroadcastManager mgr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mgr = LocalBroadcastManager.getInstance(this);
		
		Intent task = getIntent();
		mTaskId = task.getAction();
		String redirectUrl = task.getStringExtra(REDIRECT_URL);
		String requestUrl =  task.getStringExtra(REQUEST_URL);
		
		if(UrlParser.isEmptyOrNull(requestUrl) ||
				UrlParser.isEmptyOrNull(redirectUrl))
		{
			finish();
		}else{
			AuthWebView authView = new AuthWebView(
					this,
					redirectUrl);
        	setContentView(authView);
        	authView.loadUrl(requestUrl);
		}
	}
	
	private class AuthWebView extends WebView {
		
		public AuthWebView(Context context, String redirectUrl) {
			super(context);
			setWebViewClient(new AuthClient(redirectUrl));
			
			WebSettings settings = getSettings();
			settings.setJavaScriptEnabled(true);
		}
	}
	
	private class AuthClient extends WebViewClient{
		
		private String mRedirectUrl;
		
		public AuthClient(String redirectUrl){
			mRedirectUrl = redirectUrl;
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, "shouldOverrideUrlLoading ent");
			Log.d(TAG, "url = " + url);
			
			if(url.startsWith(mRedirectUrl)){//now OAth return the result.
			
				Log.d(TAG, "got result");
				
				Bundle vals = UrlParser.decodeURLParams(url);
				Intent intent = new Intent("InteractionManager")
							.putExtra("id", mTaskId)
							.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
	
				if(! vals.isEmpty()){
					Log.d(TAG, "query string not null");
					debug.printBundle(vals);
					
					intent.putExtra("ret", vals);
					
	        		String error = vals.containsKey("error") 
	        				? vals.getString("error") 
	        				: "";
	        		if(UrlParser.isEmptyOrNull(error)){
	        			
	        			String authCodeLabel = "code";
						if(vals.containsKey(authCodeLabel)){
							String authCode = vals.getString(authCodeLabel);
							intent.putExtra("isSuccess", true)
								  .putExtra("authCode", authCode);
						}
		            }else{
	                	String errDesp = vals.containsKey("error_description") 
	                				? vals.getString("error_description") 
	                				: "";
	    				intent.putExtra("isSuccess", false)
	    					  .putExtra("error", error)
	    					  .putExtra("errDesp", errDesp);
		            }
	        	}
        		mgr.sendBroadcast(intent);
        		Log.d(TAG, "intent sent");
        		finish();//close the AuthDialog
			}
			Log.d(TAG, "shouldOverrideUrlLoading ret");
            return false;
        }
	}

}

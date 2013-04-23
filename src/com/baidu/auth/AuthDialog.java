package com.baidu.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthDialog extends Activity{
	private static final String TAG = "AuthDialog";
	private static final Debug debug = new Debug(TAG);

	//data received for this Activity by Intent
	public static final String REDIRECT_URL = "redirectUrl";
	public static final String REQUEST_URL = "requestUrl";
	
	private Uri bdOAuthReceiverUri;
	private String mMethod;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		//mMethod = intent.getAction();
		String redirectUrl = intent.getStringExtra(REDIRECT_URL);
		String requestUrl =  intent.getStringExtra(REQUEST_URL);
		String intentScheme = intent.getStringExtra(BaiduOAuth.BAIDU_OAUTH_INTENT_SCHEME);
		bdOAuthReceiverUri = new Uri.Builder()
					.scheme(intentScheme)
					.build();
		
		if(UrlParser.isEmptyOrNull(requestUrl) ||
				UrlParser.isEmptyOrNull(redirectUrl))
		{
			finish();
		}else{
			AuthWebView authView = new AuthWebView(
					this.getApplicationContext(), 
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
			
			if(url.startsWith(mRedirectUrl)){//now OAth return the access token.
			
				Bundle vals = UrlParser.decodeURLParams(url);
				Context ctx = view.getContext();
	        	
				Intent intent = new Intent()
							.setData(bdOAuthReceiverUri)
	        				.putExtra("api", mMethod);
				
				if(! vals.isEmpty()){
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
	        		ctx.sendBroadcast(intent);
	        		AuthDialog.this.finish();
	        	}
			}
            return false;
        }
	}

}

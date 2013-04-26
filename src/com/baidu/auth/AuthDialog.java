package com.baidu.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

@SuppressLint("SetJavaScriptEnabled")
public class AuthDialog extends Activity{
	private static final String TAG = "AuthDialog";
	//private static final Debug debug = new Debug(TAG);

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
		
		if(Util.isEmptyOrNull(requestUrl)){
			finish();
		}else{
			setContentView(R.layout.auth_dialog);
			
			Button confirmButton = (Button) findViewById(R.id.confirm);
			Button cancelButton = (Button) findViewById(R.id.cancel);
			final EditText verificationInput = 
					(EditText) findViewById(R.id.verificationInput);
			WebView wview = (WebView) findViewById(R.id.validatePage);
			
			if(Util.isEmptyOrNull(redirectUrl)){
			
				confirmButton.setOnClickListener(new OnClickListener(){	
					@Override
					public void onClick(View arg0) {
						
						String verify = verificationInput.getText()
									.toString()
									.trim();
						Bundle vals = new Bundle();
						vals.putString("code", verify);
						Intent intent = new Intent("InteractionManager")
							.putExtra("id", mTaskId)
							.putExtra("ret", vals)
							.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
						mgr.sendBroadcast(intent);
						finish();
					}
				});
				cancelButton.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						finish();
					}
				});
			}else{
				confirmButton.setVisibility(View.INVISIBLE);
				cancelButton.setVisibility(View.INVISIBLE);
			}
			
			wview.setWebViewClient(new AuthClient(redirectUrl));
			WebSettings settings = wview.getSettings();
			settings.setJavaScriptEnabled(true);
			
			wview.loadUrl(requestUrl);
			wview.requestFocus();
		}
	}
	
	private class AuthClient extends WebViewClient{
		
		private String mRedirectUrl;
		
		public AuthClient(String redirectUrl){
			mRedirectUrl = redirectUrl;
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			Log.d(TAG, "code="+errorCode+" desp="+description+" fail="+failingUrl);
		}
		
		@Override
		public void onReceivedLoginRequest(WebView view, String realm,
				String account, String args) {
			Log.d(TAG, "realm="+realm + " account="+account);
		}
		
		@Override
		public void onReceivedHttpAuthRequest(WebView view,
				HttpAuthHandler handler, String host, String realm) {
			Log.d(TAG, "host=" + host + " realm=" + realm);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, "shouldOverrideUrlLoading ent");
			Log.d(TAG, "url = " + url);
			
			if(mRedirectUrl != null && url.startsWith(mRedirectUrl)){//now OAth return the result.
			
				Bundle vals = Util.decodeURLParams(url);
				Intent intent = new Intent("InteractionManager")
							.putExtra("id", mTaskId)
							.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
	
				if(! vals.isEmpty()){
					//debug.printBundle(vals);
					
					intent.putExtra("ret", vals);
	        	}
        		mgr.sendBroadcast(intent);
        		//Log.d(TAG, "intent sent");
        		finish();//close the AuthDialog
			}
			Log.d(TAG, "shouldOverrideUrlLoading ret");
            return false;
        }
	}

}

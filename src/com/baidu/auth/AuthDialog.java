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

import com.baidu.auth.test.Debug;

@SuppressLint("SetJavaScriptEnabled")
public class AuthDialog extends Activity{
	private static final String TAG = "AuthDialog";
	private static final Debug debug = new Debug(TAG);

	//data received for this Activity by Intent
	public static final String REDIRECT_URL = "redirectUrl";
	public static final String REQUEST_URL = "requestUrl";
	
	private static final String DEFAULT_REDIRECT_URL="http://openapi.baidu.com/oauth/2.0/login_success";
	
	private String mTaskId;
	private LocalBroadcastManager mgr;
	private EditText verificationInput;
	private String user_code = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mgr = LocalBroadcastManager.getInstance(this);
		
		Intent task = getIntent();
		mTaskId = task.getAction();
		String redirectUrl = task.getStringExtra(REDIRECT_URL);
		String requestUrl =  task.getStringExtra(REQUEST_URL);
		Bundle params = task.getBundleExtra("params");
		user_code = params.containsKey("user_code") ? params.getString("user_code") : "";
		
		if(Util.isEmptyOrNull(requestUrl)){
			finish();
		}else{
			setContentView(R.layout.auth_dialog);
			
			Button confirmButton = (Button) findViewById(R.id.confirm);
			Button cancelButton = (Button) findViewById(R.id.cancel);
			//final EditText verificationInput = 
			verificationInput =
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
				verificationInput.setVisibility(View.GONE);
				confirmButton.setVisibility(View.GONE);
				cancelButton.setVisibility(View.GONE);
			}
			
			wview.setWebViewClient(new AuthClient(redirectUrl));
			wview.addJavascriptInterface(new PageParser(), "pageParser");
			WebSettings settings = wview.getSettings();
			settings.setJavaScriptEnabled(true);
			
			wview.loadUrl(requestUrl);
			wview.requestFocus();
		}
	}
	
	final Activity me = this;
	private class PageParser{
		public void print(String data){
			int len = data.length();
			int start=0;
			int end=start+1;
			for(;end<len;end++){
				if(data.charAt(end) == '\n'){
					Log.d(TAG, data.substring(start, end));
					start = end+1;
				}
			}
		}
		
		public void stopHostActivity(){
			
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
			Log.d(TAG, "onPageFinished ent");
			
			//view.loadUrl("javascript:window.pageParser.print('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
			
			if(url.endsWith("device")){//determined by url
				String script = "var usercode = document.getElementById('code');" +
						"usercode.setAttribute('value', '"+ user_code +"');" +
						"var submit = document.getElementById('pass_fillinusername_submit_input');" +
						"submit.click();";
				view.loadUrl("javascript:" + script);
			}
			
			Log.d(TAG, "onPageFinished ret");
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, "shouldOverrideUrlLoading ent");
			Log.d(TAG, "url = " + url);
			
			if( (mRedirectUrl != null && url.startsWith(mRedirectUrl))
					|| (url.startsWith(DEFAULT_REDIRECT_URL)) )
			{//OAth return the result to redirect_url
			
				Bundle vals = Util.decodeURLParams(url);
				
				Intent intent = new Intent("InteractionManager")
							.putExtra("id", mTaskId)
							.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
				if(! vals.isEmpty()){
					user_code = vals.containsKey("user_code") 
							? vals.getString("user_code")
							: "";
					debug.printBundle(vals);
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

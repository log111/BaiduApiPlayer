package com.baidu.auth;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class BaiduOAuth {
	private static final String TAG = "BaiduOAuth";
	
	private static final String openAPIURL = "https://openapi.baidu.com";
	private static final String oauthURL = openAPIURL + "/oauth/2.0";
	
	private Context mCtx;
	private OAuthReceiver mReceiver;
	private String mIntentScheme;
	private ConcurrentMap<String, List<Callback>> mCallbackMap = null;
	
	public static final String BAIDU_OAUTH_INTENT_SCHEME = "intentScheme";
	
	private static class OAuthReceiver extends BroadcastReceiver{

		private BaiduOAuth mOauth;
		
		public OAuthReceiver(BaiduOAuth oauth){
			mOauth = oauth;
		}
		
		@Override
		public void onReceive(Context arg0, Intent intent) {
			Log.d(TAG, "broadcast received");
			boolean isSuccess = intent.getBooleanExtra("isSuccess", false);
			
			String api = intent.getAction();
			Log.d(TAG, "api="+api);
			if("getAuthCode".equals(api)){
				List<Callback> cbList = mOauth.mCallbackMap.get(api);
				Log.d(TAG, "size=" + cbList.size());
				
				if(isSuccess){
					String authCode = intent.getStringExtra("authCode");
					if(UrlParser.isEmptyOrNull(authCode)){//impossible, server error if happened.
						Log.e(TAG, "empty auth code");
					}else{
						for(Callback cb : cbList){
							cb.onSuccess(authCode);
						}
					}
				}else{
					String errCode = intent.getStringExtra("error");
					String errMsg = intent.getStringExtra("errDesp");
					for(Callback cb : cbList){
						cb.onFail(errCode, errMsg);
					}
				}
				mOauth.mCallbackMap.remove(api);
			}
		}
		
	}
	
	public static interface Callback{
		void onSuccess(String result);
		void onFail(String errCode, String errMsg);
	}
	
	public BaiduOAuth(Context ctx){
		mCallbackMap = new ConcurrentHashMap<String, List<Callback>>();
		mCtx = ctx;
		mReceiver = new OAuthReceiver(this);
		mIntentScheme = toString();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("getAuthCode");
		
		LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(ctx);
		mgr.registerReceiver(mReceiver, filter);
	}
	
	@Override
	protected void finalize() throws Throwable {
		
		if(mCtx != null && mReceiver != null){
			LocalBroadcastManager mgr = LocalBroadcastManager.getInstance(mCtx);
			mgr.unregisterReceiver(mReceiver);
		}
		super.finalize();
	}
	
	public void getAuthCode(String apiKey, 
			String redirectUrl, 
			Callback cb)
	{
		Bundle params = new Bundle();
		params.putString("client_id", apiKey);
		params.putString("response_type", "code");
		params.putString("redirect_uri", redirectUrl);
		params.putString("state", "");
		params.putString("scope", "basic");
		params.putString("display", "touch");

		String authAPI = oauthURL + "/authorize";
		URL requestUrl = UrlParser.encodeURLParams(authAPI, params);
		
		String api = "getAuthCode";
		if(! mCallbackMap.containsKey(api)){
			mCallbackMap.put(api, new ArrayList<Callback>());
		}
		List<Callback> cbList = mCallbackMap.get(api);
		cbList.add(cb);
		
		Intent i = new Intent("getAuthCode")
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				.setComponent(new ComponentName(mCtx, AuthDialog.class))
				.putExtra(BAIDU_OAUTH_INTENT_SCHEME, mIntentScheme)
				.putExtra(AuthDialog.REDIRECT_URL, redirectUrl)
				.putExtra(AuthDialog.REQUEST_URL, requestUrl.toString());
		mCtx.startActivity(i);
	}
	
	public static interface TokenCallback{
		void onSuccess(String access_token, 
				long expires_in, 
				String refresh_token,
				String scope,
				String session_key,
				String session_secret);
		void onFail(String errCode, String errMsg);
	}
	
	public void getAccessTokenByAuthCode(String apiKey,
			String secretKey, 
			String authCode,
			String redirectUrl,
			TokenCallback cb)
	{
		Bundle params = new Bundle();
		params.putString("client_id", apiKey);
		params.putString("grant_type", "authorization_code");
		params.putString("client_secret", secretKey);
		params.putString("redirect_uri", redirectUrl);
		params.putString("code", authCode);
		
		String tokenUrl = oauthURL + "/token";
		final URL requestUrl = UrlParser.encodeURLParams(tokenUrl, params);
		
		final TokenCallback tcb = cb;
		AuthTask t = new AuthTask(requestUrl, new AuthTask.Callback() {
			
			@Override
			public void onSuccess(JSONObject ret) {
				try{
					String access_token = ret.has("access_token") 
							? ret.getString("access_token") : "";
					long expires_in = ret.has("expires_in") 
							? ret.getLong("expires_in") : -1;
					String refresh_token = ret.has("refresh_token") 
							? ret.getString("refresh_token") : "";
					String scope = ret.has("scope") 
							? ret.getString("scope") : "";
					String session_key = ret.has("session_key") 
							? ret.getString("session_key") : "";
					String session_secret = ret.has("session_secret")
							? ret.getString("session_secret") : "";
					
					tcb.onSuccess(access_token, 
							expires_in, 
							refresh_token, 
							scope, 
							session_key, 
							session_secret);
				}catch(JSONException e){//unless server returns a bad reply, which is impossible.
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFail(JSONObject err, Exception localException) {
				try{
					String error = err.has("error") ? err.getString("error") : "";
					String errDesp = err.has("error_description") ? err.getString("error_description") : "";
					tcb.onFail(error, errDesp);
				}catch(JSONException e){//unless server returns a bad reply, which is impossible.
					e.printStackTrace();
				}
			}
		});
		t.execute((Void)null);
		
	}
}

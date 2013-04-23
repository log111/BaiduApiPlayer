package com.baidu.auth;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
import android.util.Log;

public class BaiduOAuth {
	private static final String TAG = "BaiduOAuth";
	
	private static final String openAPIURL = "https://openapi.baidu.com";
	private static final String oauthURL = openAPIURL + "/oauth/2.0";	
	private static final String DATA_SCHEME = "com.baidu.auth.BaiduOAuth";
	
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
			
			String api = intent.getStringExtra("api");
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
		mCtx = ctx.getApplicationContext();
		mReceiver = new OAuthReceiver(this);
		mIntentScheme = toString();
		
		IntentFilter filter = new IntentFilter();
		filter.addDataScheme(mIntentScheme);
		mCtx.registerReceiver(mReceiver, filter);
	}
	
	@Override
	protected void finalize() throws Throwable {
		
		if(mCtx != null && mReceiver != null){
			mCtx.unregisterReceiver(mReceiver);
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
	
	public void getAccessTokenByAuthCode(String apiKey,
			String secretKey, 
			String authCode,
			String redirectUrl,
			Callback cb)
	{
		Bundle params = new Bundle();
		params.putString("client_id", apiKey);
		params.putString("grant_type", "authorization_code");
		params.putString("redirect_uri", redirectUrl);
		params.putString("code", authCode);
		
		String tokenUrl = oauthURL + "/token";
		URL requestUrl = UrlParser.encodeURLParams(tokenUrl, params);
		
		List<Callback> cbList = mCallbackMap.get("getAccessTokenByAuthCode");
		if(null == cbList){
			mCallbackMap.put("getAccessTokenByAuthCode", new ArrayList<Callback>());
			cbList = mCallbackMap.get("getAuthCode");
		}
		cbList.add(cb);
		
		HttpURLConnection conn = null;
		try{
			conn = (HttpURLConnection)requestUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			int respCode = conn.getResponseCode();
			InputStreamReader reader = null;
			
			boolean isError = false;
			if(HttpURLConnection.HTTP_OK == respCode){
				reader = new InputStreamReader(conn.getInputStream());
				isError = true;
			}else{
				reader = new InputStreamReader(conn.getErrorStream());
			}
			int bufLen = 2048;
			char[] buffer = new char[bufLen];
			StringBuilder sb = new StringBuilder();
			int len = reader.read(buffer, 0, bufLen);
			if(len > 0){
				sb.append(buffer, 0, len);
				len = reader.read(buffer, 0, bufLen);
			}
			reader.close();
			
			try{
				JSONObject obj = new JSONObject(sb.toString());
				Log.d(TAG, obj.toString());
			}catch(JSONException e){
				e.printStackTrace();
			}
			//json body
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}
		Intent i = new Intent("getAccessTokenByAuthCode")
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				.setComponent(new ComponentName(mCtx, AuthDialog.class))
				.putExtra(BAIDU_OAUTH_INTENT_SCHEME, mIntentScheme)
				.putExtra(AuthDialog.REDIRECT_URL, redirectUrl)
				.putExtra(AuthDialog.REQUEST_URL, requestUrl.toString());
		mCtx.startActivity(i);
	}
}

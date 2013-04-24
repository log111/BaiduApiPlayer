package com.baidu.auth;

import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

public class BaiduOAuth {
	private static final String openAPIURL = "https://openapi.baidu.com";
	private static final String oauthURL = openAPIURL + "/oauth/2.0";
	
	private Context mCtx;
		public static interface Callback{
		void onSuccess(String result);
		void onFail(String errCode, String errMsg);
	}

	public BaiduOAuth(Context ctx){
		mCtx = ctx;
	}
	
	public void getAuthCode(String apiKey, 
			URL redirectUrl,
			String scope,
			InteractionManager.Callback cb)
	{
		Bundle params = new Bundle();
		params.putString("client_id", apiKey);
		params.putString("response_type", "code");
		params.putString("redirect_uri", redirectUrl.toString());
		params.putString("state", "");
		params.putString("scope", scope);
		params.putString("display", "touch");

		String authAPI = oauthURL + "/authorize";
		URL requestUrl = UrlParser.encodeURLParams(authAPI, params);
		InteractionManager.getInstance(mCtx)
						  .send(requestUrl, redirectUrl, cb);
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
		MuteTask t = new MuteTask(requestUrl, new MuteTask.Callback() {
			
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
		t.runAsync();
	}
	
	public void getTokenByAuthorizationCode(
			String apiKey,
			String secretKey,
			URL redirectUrl,
			String scope,
			String state,
			TokenCallback cb)
	{
		final TokenCallback myCb = cb;
		final String clientId = apiKey;
		final String sk = secretKey;
		final String acceptUrl = redirectUrl.toString();
		
		InteractionManager.Callback mcb = new InteractionManager.Callback() {
			
			@Override
			public void onSuccess(String authCode) {
				getAccessTokenByAuthCode(clientId,
					sk, 
					authCode,
					acceptUrl,
					myCb);
			}
			
			@Override
			public void onFail(String errCode, String errMsg) {
				myCb.onFail(errCode, errMsg);
			}
		};
		
		getAuthCode(apiKey, redirectUrl, scope, mcb);
	}
}

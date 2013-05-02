package com.baidu.auth;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

@SuppressLint("NewApi")
public final class BaiduOAuth {
	private static final String TAG = "BaiduOAuth";
	private static final String openAPIURL = "https://openapi.baidu.com";
	private static final String oauthURL = openAPIURL + "/oauth/2.0";
	
	private Context mCtx;
	
	public static interface TokenCallback{
		void onSuccess(String access_token, 
				long expires_in, 
				String refresh_token,
				String scope,
				String session_key,
				String session_secret);
		void onFail(String... ret);
	}
	
	public BaiduOAuth(Context ctx){
		mCtx = ctx;
	}
	
	private void getAuthCode(String apiKey, 
			URL redirectUrl,
			String scope,
			InteractionManager.Callback cb)
	{
		Bundle params = new Bundle();
		params.putString("client_id", apiKey);
		params.putString("redirect_uri", (redirectUrl!=null ? redirectUrl.toString() : "oob") );
		params.putString("scope", (scope!=null ? scope : ""));
		params.putString("state", "");
		params.putString("response_type", "code");
		params.putString("display", "mobile");
		//If user is login, supply an option to use it, or change to another one.
		params.putString("confirm_login", "1");
				
		
		String authAPI = oauthURL + "/authorize";
		URL requestUrl = Util.encodeURLParams(authAPI, params);
		InteractionManager.getInstance(mCtx)
						  .send(requestUrl, redirectUrl, cb);
	}
	
	private void getTokenByAuthCode(String apiKey,
			String secretKey, 
			String authCode,
			String redirectUrl,
			TokenCallback cb)
	{
		Bundle params = new Bundle();
		params.putString("client_id", apiKey);
		params.putString("grant_type", "authorization_code");
		params.putString("client_secret", secretKey);
		if(redirectUrl != null){
			params.putString("redirect_uri", redirectUrl.toString());
		}else{
			params.putString("redirect_uri", "oob");
		}
		params.putString("code", authCode);
		
		String tokenUrl = oauthURL + "/token";
		final URL requestUrl = Util.encodeURLParams(tokenUrl, params);
		
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
	
	public void validateByAuthCode(
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
		final String acceptUrl = (redirectUrl != null) 
						? redirectUrl.toString()
						: null;
		
		InteractionManager.Callback mcb = new InteractionManager.Callback() {
			
			@Override
			public void onSuccess(Bundle vals) {
				
				String authCode = vals.containsKey("code")
						? vals.getString("code")
						: "";
				getTokenByAuthCode(clientId,
					sk, 
					authCode,
					acceptUrl,
					myCb);
			}
			
			@Override
			public void onFail(String errCode, String errMsg, String state) {
				myCb.onFail(errCode, errMsg);
			}
		};
		
		getAuthCode(apiKey, redirectUrl, scope, mcb);
	}
	
	public void refreshToken(
			String apiKey,
			String secretKey,
			String refreshToken,
			String scope,
			TokenCallback cb)
	{
		Bundle params = new Bundle();
		params.putString("client_id", apiKey);
		params.putString("grant_type", "refresh_token");
		params.putString("client_secret", secretKey);
		params.putString("refresh_token", refreshToken);
		params.putString("scope", scope);
		
		String tokenUrl = oauthURL + "/token";
		final URL requestUrl = Util.encodeURLParams(tokenUrl, params);
		
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
	
	public void validateByImplicitGrant(
			String apiKey,
			String secretKey,
			URL redirectUrl,
			String scope,
			TokenCallback cb)
	{
		Bundle params = new Bundle();
		params.putString("client_id", apiKey);
		params.putString("client_secret", secretKey);
		params.putString("scope", (scope!=null ? scope : ""));
		params.putString("redirect_uri", (redirectUrl != null ? redirectUrl.toString() : "oob") );
		params.putString("response_type", "token");
		params.putString("display", "touch");
		//If user is login, supply an option to use it, or change to another one.
		params.putString("confirm_login", "1");
		
		String tokenUrl = oauthURL + "/authorize";
		final URL requestUrl = Util.encodeURLParams(tokenUrl, params);
		final TokenCallback tcb = cb;
		
		InteractionManager.Callback mcb = new InteractionManager.Callback() {
			
			@Override
			public void onSuccess(Bundle ret) {
				String access_token = ret.containsKey("access_token") 
						? ret.getString("access_token") : "";
				long expires_in = ret.containsKey("expires_in") 
						? Long.parseLong(ret.getString("expires_in"))
						: -1;
				String refresh_token = ret.containsKey("refresh_token") 
						? ret.getString("refresh_token") : "";
				String scope = ret.containsKey("scope") 
						? ret.getString("scope") : "";
				String session_key = ret.containsKey("session_key") 
						? ret.getString("session_key") : "";
				String session_secret = ret.containsKey("session_secret")
						? ret.getString("session_secret") : "";
				
				tcb.onSuccess(access_token, 
						expires_in, 
						refresh_token, 
						scope, 
						session_key, 
						session_secret);
			}
			
			@Override
			public void onFail(String errCode, String errMsg, String state) {
				tcb.onFail(errCode, errMsg, state);
			}
		};
		InteractionManager
			.getInstance(mCtx)
			.send(requestUrl, redirectUrl, mcb);
	}
	
	public void validateByCredential(
			String apiKey,
			String secretKey,
			String scope,
			TokenCallback cb){
		
		Bundle params = new Bundle();
		params.putString("client_id", apiKey);
		params.putString("grant_type", "client_credentials");
		params.putString("client_secret", secretKey);
		params.putString("scope", scope);
		
		String tokenUrl = oauthURL + "/token";
		final URL requestUrl = Util.encodeURLParams(tokenUrl, params);
		
		final TokenCallback tcb = cb;
		MuteTask t = new MuteTask(requestUrl, 
				new MuteTask.Callback() {
					
					@Override
					public void onSuccess(JSONObject ret) {
						try{
							String access_token = ret.has("access_token") 
									? ret.getString("access_token") : "";
							long expires_in = ret.has("expires_in") 
									? ret.getLong("expires_in") : -1;
							String session_key = ret.has("session_key") 
									? ret.getString("session_key") : "";
							String session_secret = ret.has("session_secret")
									? ret.getString("session_secret") : "";
							
							tcb.onSuccess(access_token, 
									expires_in, 
									"",
									"",
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
	
	private void getDeviceUserCode(
			String apiKey, 
			String scope,
			TokenCallback cb){
		
		Bundle params = new Bundle();
		params.putString("client_id", apiKey);
		params.putString("scope", scope);
		params.putString("response_type", "device_code");
		
		URL url = Util.encodeURLParams(oauthURL + "/device/code", params);
		final TokenCallback tcb = cb;
		MuteTask t = new MuteTask(
				url, 
				new MuteTask.Callback() {
					
					@Override
					public void onSuccess(JSONObject ret) {
						try{
							String deviceCode = ret.has("device_code")
									? ret.getString("device_code")
									: "";
							String userCode = ret.has("user_code")
									? ret.getString("user_code")
									: "";
							String vfUrl = ret.has("verification_url")
									? ret.getString("verification_url")
									: "";
							String qcUrl = ret.has("qrcode_url")
									? ret.getString("qrcode_url")
									: "";
							String expired = ret.has("expires_in")
									? ret.getString("expires_in")
									: "";
							String interval = ret.has("interval")
									? ret.getString("interval")
									: "";
							Log.d(TAG,  "vfUrl = " + vfUrl);
							Log.d(TAG,  "qcUrl = " + qcUrl);
							Log.d(TAG,  "userCode = " + userCode);
							
							NotificationManager nmgr 
								= (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);
							Notification noti = null;
							/*
							if(Build.VERSION.SDK_INT >= 11 
									&& Build.VERSION.SDK_INT < 16){
								noti = new Notification.Builder(mCtx)
									.setContentTitle("User Code")
									.setContentText(userCode)
									.getNotification();
							}else if(Build.VERSION.SDK_INT > 16){
							*/	
							noti = new Notification.Builder(mCtx)
									.setContentTitle(userCode)
									.setContentText("User Code")
									.setSmallIcon(R.drawable.ic_launcher)
									.getNotification();
								Log.d(TAG, "notification="+noti.toString());
							/*
							}else{
								//TODO
							}*/
							nmgr.notify(noti.hashCode(), noti);
							verifyUserCode(userCode, vfUrl, qcUrl, tcb);							
						}catch(JSONException e){
							e.printStackTrace();
						}
					}
					
					@Override
					public void onFail(JSONObject err, Exception localException) {
						if(err != null){
							try{
								String errCode = err.has("error")
										? err.getString("error")
										: "";
								String errMsg = err.has("error_description")
										? err.getString("error_description")
										: "";
								tcb.onFail(errCode, errMsg);
								
							}catch(JSONException e){
								e.printStackTrace();
							}
						}else{
							//
						}
					}
				});
		t.runAsync();
	}
	
	private void verifyUserCode(
			String userCode, String verifyUrl, /* input userCode into the verifyUrl */ 
			String qcodeUrl, /* scan the qcode in the qcodeUrl */
			TokenCallback cb){
		
		try{
			URL requestUrl = new URL(verifyUrl);
			URL mockUrl = new URL("http://localhost");
			InteractionManager.getInstance(mCtx)
				.send(requestUrl, 
					mockUrl, 
					new InteractionManager.Callback() {
						
						@Override
						public void onSuccess(Bundle result) {
							Log.d(TAG, "verify success");
						}
						
						@Override
						public void onFail(String errCode, String errMsg, String state) {
							Log.d(TAG, "fail to verify");
						}
					});
		}catch(MalformedURLException e){
			e.printStackTrace();
		}
	}
	
	private void getTokenByDeviceCode(
			String apiKey, 
			String secretKey, 
			String deviceCode)
	{
		//TODO
	}
	
	public void validateByDevice(
			String apiKey, 
			String secretKey,
			String scope, 
			TokenCallback cb){
		getDeviceUserCode(apiKey, scope, cb);
	}
}

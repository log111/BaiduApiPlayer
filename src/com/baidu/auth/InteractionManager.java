package com.baidu.auth;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class InteractionManager {

	private static final String TAG = "InteractionManager";
	
	private static InteractionManager mInstance;
	private static Object mLock = new Object();
	private LocalBroadcastManager mBroadcastMgr;
	private OAuthReceiver mReceiver;
	private ConcurrentMap<String, Callback> mCallbackMap = null;
	private Context mCtx;
	
	private class OAuthReceiver extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context arg0, Intent intent) {
			Log.d(TAG, "broadcast received");
			boolean isSuccess = intent.getBooleanExtra("isSuccess", false);
			
			String action = intent.getAction();
			if(! "InteractionManager".equals(action)){
				return;
			}
			
			String taskId = intent.getStringExtra("id");
			Log.d(TAG, "task="+taskId);
			
			Callback cb = mCallbackMap.get(taskId);
			if(isSuccess){
				String authCode = intent.getStringExtra("authCode");
				if(UrlParser.isEmptyOrNull(authCode)){//impossible, server error if happened.
					Log.e(TAG, "empty auth code");
				}else{
					cb.onSuccess(authCode);
				}
			}else{
				String errCode = intent.getStringExtra("error");
				String errMsg = intent.getStringExtra("errDesp");
				cb.onFail(errCode, errMsg);
			}
			mCallbackMap.remove(taskId);
		}	
	}
	
	private InteractionManager(Context ctx){
		mCtx = ctx;
		mCallbackMap = new ConcurrentHashMap<String, Callback>();
		mReceiver = new OAuthReceiver();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("InteractionManager");
		
		mBroadcastMgr = LocalBroadcastManager.getInstance(ctx);
		mBroadcastMgr.registerReceiver(mReceiver, filter);
	}
	
	@Override
	protected void finalize() throws Throwable {
		
		if(mBroadcastMgr != null && mReceiver != null){
			mBroadcastMgr.unregisterReceiver(mReceiver);
		}
		super.finalize();
	}
	
	public static InteractionManager getInstance(Context ctx){
		synchronized(mLock){
			if(null == mInstance){
				mInstance = new InteractionManager(ctx);
			}
			return mInstance;
		}
	}
	
	public static interface Callback{
		void onSuccess(String result);
		void onFail(String errCode, String errMsg);
	}
	
	public void send(URL requestUrl, URL mAcceptRetUrl, Callback cb){
		
		String taskId = cb.toString();
		mCallbackMap.put(taskId, cb);
		
		Intent i = new Intent(taskId)
			.setComponent(new ComponentName(mCtx, AuthDialog.class))
			.putExtra(AuthDialog.REDIRECT_URL, mAcceptRetUrl.toString())
			.putExtra(AuthDialog.REQUEST_URL, requestUrl.toString())
			.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.startActivity(i);
	}
}

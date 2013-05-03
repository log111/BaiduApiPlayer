package com.baidu.auth;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class InteractionManager {

	private static final String TAG = "InteractionManager";
	
	public static interface Callback{
		void onSuccess(Bundle result);
		void onFail(String errCode, String errMsg, String state);
	}
	
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
			
			String action = intent.getAction();
			if(! "InteractionManager".equals(action)){
				return;
			}
			
			String taskId = intent.getStringExtra("id");
			Bundle vals = intent.getBundleExtra("ret");
			Log.d(TAG, "task="+taskId);
			
			String error = vals.containsKey("error") 
    				? vals.getString("error") 
    				: "";
    		boolean successful = true;
    		if(! Util.isEmptyOrNull(error)){
    			successful = false;
    		}
			
			Callback cb = mCallbackMap.get(taskId);
			if(successful){
				cb.onSuccess(vals);
			}else{
				String errCode = vals.containsKey("error") 
						? vals.getString("error")
						: "";
				String errMsg = vals.containsKey("error_description") 
						? vals.getString("error_description")
						: "";
				String state = vals.containsKey("state") 
						? vals.getString("state")
						: "";
				cb.onFail(errCode, errMsg, state);
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
	
	public void send(URL requestUrl, URL mAcceptRetUrl, Bundle params, Callback cb){
		
		String taskId = cb.toString();
		mCallbackMap.put(taskId, cb);
		
		Intent i = new Intent(taskId)
			.setComponent(new ComponentName(mCtx, AuthDialog.class))
			.putExtra(AuthDialog.REQUEST_URL, requestUrl.toString())
			.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if(mAcceptRetUrl != null){
			i.putExtra(AuthDialog.REDIRECT_URL, mAcceptRetUrl.toString());
		}
		if(params != null){
			i.putExtra("params", params);
		}
		mCtx.startActivity(i);
	}
	
	public void send(URL requestUrl, URL mAcceptRetUrl, Callback cb){
		send(requestUrl, mAcceptRetUrl, null, cb);
	}
	
	public void send(URL requestUrl, Callback cb){
		send(requestUrl, null, null, cb);
	}
}

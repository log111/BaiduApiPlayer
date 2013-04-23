package com.baidu.auth;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class AuthTask extends AsyncTask<Void, Void, JSONObject> {
	
	private static final String TAG = "AuthTask";

	private URL mUrl;
	private Callback mCallback;
	private boolean remoteErrorOccurred;
	private Exception localExp;
	
	public static interface Callback{
		void onSuccess(JSONObject ret);
		void onFail(JSONObject err, Exception localException);
	}
	
	public AuthTask(URL url, Callback cb){
		mUrl = url;
		mCallback = cb;
		remoteErrorOccurred = false;
	}
	
	@Override
	protected JSONObject doInBackground(Void... params) {
		HttpURLConnection conn = null;
		try{
			conn = (HttpURLConnection)mUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			int respCode = conn.getResponseCode();
			InputStreamReader reader = null;
			
			if(HttpURLConnection.HTTP_OK == respCode){
				reader = new InputStreamReader(conn.getInputStream());
			}else{
				remoteErrorOccurred = true;
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
				
				return obj;
			}catch(JSONException e){
				e.printStackTrace();
				localExp = e;
			}
		}catch(IOException e){
			e.printStackTrace();
			localExp = e;
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		if(result != null){
			if(! remoteErrorOccurred){
				mCallback.onSuccess(result);
			}else{
				mCallback.onFail(result, null);
			}
		}else{
			mCallback.onFail(null, localExp);
		}
	}

}

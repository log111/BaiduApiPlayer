package com.baidu.openapi.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class MuteTask extends AsyncTask<Void, Void, JSONObject> {
	
	private static final String TAG = "MuteTask";

	private URL mUrl;
	private Callback mCallback;
	private boolean remoteErrorOccurred;
	private Exception localExp;
	private WriteHook mWriteHook;
	private String contentType;
	private HttpURLConnection mConn;
	
	public static interface Callback{
		void onSuccess(JSONObject ret);
		void onFail(JSONObject err, Exception localException);
	}
	
	public static interface WriteHook{
		void writeHttpBody(OutputStream out) throws IOException;
	}
	
	public MuteTask(URL url, Callback cb){
		this(url, cb, "application/octet-stream", null);
	}
	
	public MuteTask(URL url, Callback cb, String bodyMimeType){
		this(url, cb, bodyMimeType, null);
	}
	
	public MuteTask(URL url, Callback cb, String bodyMimeType, WriteHook hook){
		mUrl = url;
		mCallback = cb;
		remoteErrorOccurred = false;
		mWriteHook = hook;
		contentType = bodyMimeType;
	}
	
	public MuteTask(HttpURLConnection conn, Callback cb, String bodyMimeType, WriteHook hook){
		mConn = conn;
		mCallback = cb;
		remoteErrorOccurred = false;
		mWriteHook = hook;
		contentType = bodyMimeType;
	}
	
	public void runAsync(){
		execute((Void)null);
	}
	
	private String processStream(InputStream is) throws IOException{
		
		InputStreamReader reader = new InputStreamReader(is);
		
		int bufLen = 2048;
		char[] buffer = new char[bufLen];
		StringBuilder sb = new StringBuilder();
		int len = reader.read(buffer, 0, bufLen);
		if(len != -1){
			sb.append(buffer, 0, len);
			len = reader.read(buffer, 0, bufLen);
		}
		reader.close();
		return sb.toString();
	}
	
	@Override
	protected JSONObject doInBackground(Void... params) {
		
		//HttpClient hc = new DefaultHttpClient();
		AndroidHttpClient client = AndroidHttpClient.newInstance("AppleWebKit/537.31");
		HttpGet req = new HttpGet(mUrl.toString());
		try{
			HttpResponse resp = client.execute(req);
			HttpEntity entity = resp.getEntity();
			if(entity != null){
				InputStream is = AndroidHttpClient.getUngzippedContent(entity);
				try{
					String json = processStream(is);
					Log.d("Place", json);
					return new JSONObject(json);
				}finally{
					is.close();
				}
			}
		}catch(ClientProtocolException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch(JSONException e){
			e.printStackTrace();
		}
		/*
		HttpURLConnection conn = mConn;
		try{
			if(null == conn){
				conn = (HttpURLConnection)mUrl.openConnection();
				conn.setDoInput(true);
				
				conn.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
				conn.setRequestProperty("Connection", "keep-alive");
				
				if(mWriteHook != null){
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
				}else{
					conn.setRequestMethod("GET");
				}
			}
			conn.setRequestProperty("Content-Type", contentType);
			
			if(mWriteHook != null){
				OutputStream out = conn.getOutputStream();
				mWriteHook.writeHttpBody(out);
			}
			
			int respCode = conn.getResponseCode();
			InputStream is = null;
			
			if(HttpURLConnection.HTTP_OK == respCode){
				
				is = conn.getInputStream();
			}else{
				remoteErrorOccurred = true;
				is = conn.getErrorStream();
			}
			
			String encoding = conn.getContentEncoding();
			String respBody = null;
			
			conn.getContentType();
			
			if("gzip".equals(encoding)){
			
				GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(is));
				try {
				    respBody = processStream(zis);
				}finally {
				     zis.close();
				}
			}else{
				respBody = processStream(is);
			}
			try{
				if(respBody != null){
					JSONObject obj = new JSONObject(respBody);
					Log.d(TAG, respBody);
					return obj;
				}
			}catch(JSONException e){
				Log.d(TAG, respBody);
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
		*/
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

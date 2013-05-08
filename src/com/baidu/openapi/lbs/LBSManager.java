package com.baidu.openapi.lbs;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.baidu.openapi.auth.MuteTask;
import com.baidu.openapi.util.URLBuilder;

public class LBSManager {
	
	private static final String TAG = "LBSManager";
	
	private static final String lbsAPI = "http://api.map.baidu.com/geodata";
	private static final String boxAPI = lbsAPI + "/databox";
	private static final String poiAPI = lbsAPI + "/poi";
	
	private String ak;
	private String sn;
	
	public LBSManager(String apiKey){
		if(apiKey == null){
			throw new IllegalArgumentException("apiKey is null");
		}
		ak = apiKey;
		sn = "";
	}
	
	public LBSManager(String apiKey, String signature){
		if(apiKey == null){
			throw new IllegalArgumentException("apiKey is null");
		}
		if(signature == null){
			throw new IllegalArgumentException("signature is null");
		}
		ak = apiKey;
		sn = signature;
	}
	
	public void saveBoxAsync(DataBox box){
		
		URLBuilder builder = new URLBuilder()
				.setBaseUrl(boxAPI)
				.appendQuery("method", "create");
		builder.appendQuery("name", box.chineseName)
			.appendQuery("code", box.englishName)
			.appendQuery("ak", ak);
		
		switch(box.elementType){
			case POINT:
				builder.appendQuery("geotype", "1");
				break;
			case LINE:
				builder.appendQuery("geotype", "2");
				break;
			case AREA:
				builder.appendQuery("geotype", "3");
				break;
			default:
				break;
		}
		
		if(!sn.trim().isEmpty()){
			builder.appendQuery("sn", sn);
		}
		try{
			URL url = builder.build();
			
			MuteTask task = new MuteTask(url, new MuteTask.Callback(){

				@Override
				public void onSuccess(JSONObject ret) {
					try{
						int status = ret.has("status") ? ret.getInt("status") : -1;
						String msg = ret.has("message") ? ret.getString("message") : "";
						Log.d(TAG, "status code: " + status + " message: " + msg);
					}catch(JSONException e){
						e.printStackTrace();
					}
				}

				@Override
				public void onFail(JSONObject err, Exception localException) {
					try{
						Log.e(TAG, err.getString("status"));
						Log.e(TAG, err.getString("message"));
					}catch(JSONException e){
						e.printStackTrace();
					}
				}
				
			});
			task.runAsync();
		}catch(MalformedURLException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void updateBoxAsync(DataBox box){
		//TODO
	}
	
	public static void removeBoxAsync(DataBox box){
		//TODO
	}
	
	public static void saveBoxAsync(DataBox[] boxes){
		//TODO
	}
}

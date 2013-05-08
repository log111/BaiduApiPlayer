package com.baidu.openapi.lbs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.baidu.openapi.auth.MuteTask;

public class LBSManager {
	
	private static final String TAG = "LBSManager";
	
	private static final String lbsAPI = "http://api.map.baidu.com/geodata";
	private static final String boxAPI = lbsAPI + "/databox";
	private static final String poiAPI = lbsAPI + "/poi";
	
	private String ak;
	private String sk;
	
	public LBSManager(String apiKey, String secretKey){
		if(apiKey == null){
			throw new IllegalArgumentException("apiKey is null");
		}
		if(secretKey == null){
			throw new IllegalArgumentException("signature is null");
		}
		ak = apiKey;
		sk = secretKey;
	}
	
	public void saveBoxAsync(DataBox box){
		
		Bundle params = new Bundle();
		params.putString("method", "create");
		params.putString("name", box.chineseName);
		params.putString("ak", ak);
		/*
		URLBuilder builder = new URLBuilder()
				.setBaseUrl(boxAPI)
				.appendQuery("method", "create");
		*/
		try{
			StringBuilder sb = new StringBuilder();
			sb.append("/geodata/databox?method=create");
			for(String key : params.keySet()){
				String val = params.getString(key);
				sb.append(key)
					.append("=")
					.append(URLEncoder.encode(val, "UTF-8"));
			}
			sb.append(sk);
			
			String msg = URLEncoder.encode(sb.toString(), "UTF-8");
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(msg.getBytes());
			
			String sn = new BigInteger(1, md.digest()).toString(16);
			params.putString("sn", sn);
			
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		/*
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
		}*/
		
		try{
			//URL url = builder.build();
			//URL url = new URL(boxAPI + "?method=create");
			URL url = new URL(boxAPI);
			/*
			StringBuilder builder = new StringBuilder();
			boolean isFirst = true;
			for(String key : params.keySet()){
				String val = params.getString(key);
				if(isFirst){
					isFirst = false;
				}else{
					builder.append("&");
				}
				builder.append(URLEncoder.encode(key, "UTF-8"))
					.append("=")
					.append(URLEncoder.encode(val, "UTF-8"));
			}
			*/
			//URL url = new URL(boxAPI + "?method=create&" + builder.toString());
			
			Log.d(TAG, "url="+url.toString());
			//final DataBox b = box;
			
			final Bundle p = params;
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
					if(err != null){
						try{
							Log.e(TAG, err.getString("status"));
							Log.e(TAG, err.getString("message"));
						}catch(JSONException e){
							e.printStackTrace();
						}
					}else{
						localException.printStackTrace();
					}
				}
				
			}, 
			//"applicatoin/json",
			null,
			//null
			new MuteTask.WriteHook(){

				@Override
				public void writeHttpBody(OutputStream out) throws IOException {
					StringBuilder builder = new StringBuilder();
					boolean isFirst = true;
					for(String key : p.keySet()){
						String val = p.getString(key);
						if(isFirst){
							isFirst = false;
						}else{
							builder.append("&");
						}
						builder.append(URLEncoder.encode(key, "UTF-8"))
							.append("=")
							.append(URLEncoder.encode(val, "UTF-8"));
					}
					String body = builder.toString();
					Log.d(TAG, body);
					out.write(body.getBytes());
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

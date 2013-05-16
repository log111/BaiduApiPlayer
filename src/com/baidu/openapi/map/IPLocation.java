package com.baidu.openapi.map;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

import com.baidu.openapi.util.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class IPLocation {
	private static final String TAG = "IPLocation";
	private static final String ipLocateAPI = "http://api.map.baidu.com/location/ip";
	
	public static interface Callback{
		void onSuccess(String address, JSONObject detail);
		void onFail(int errorCode, String errorMsg);
	}
	
	private String ak;
	
	public IPLocation(String appKey){
		ak = appKey;
	}
	
	public void locate(Callback cb){
		locate(null, cb);
	}
	
	public void locate(InetAddress ip, Callback cb){
		//TODO
		StringBuilder queryString = new StringBuilder("ak=").append(ak);
		if(ip != null){
			byte[] ipBytes = ip.getAddress();
			StringBuilder buf = new StringBuilder();
			for(int i=0;i<ipBytes.length;i++){
				buf.append(Byte.toString(ipBytes[i]));
				if(i< ipBytes.length-1){
					buf.append(".");
				}
			}
			queryString.append("&ip=").append(buf.toString());
		}
		try{
			URL url = new URL(ipLocateAPI + "?" + queryString.toString());
			Log.d(TAG, url.toString());
			final Callback myCB = cb;
			
			HttpRequest t = new HttpRequest(url,
					new HttpRequest.Callback() {
				
						@Override
						public void onSuccess(JSONObject ret) {
							try{
								int status = ret.has("status") ? ret.getInt("status") : 0;
								if(0 == status){
									String addrCode = ret.has("address") ? ret.getString("address") : "";
									JSONObject addrDetail = ret.has("content") ? ret.getJSONObject("content") : null;
									myCB.onSuccess(addrCode, addrDetail);
								}else{
									myCB.onFail(status, StatusCode.getMessage(status));
								}
							}catch(JSONException e){
								e.printStackTrace();
							}
						}
						
						@Override
						public void onFail(JSONObject err, Exception localException) {
							//no need
						}
					}
			);
			
			t.runAsync();
		
		}catch(MalformedURLException e){
			e.printStackTrace();
		}
	}
}

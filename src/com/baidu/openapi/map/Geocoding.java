package com.baidu.openapi.map;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.openapi.auth.MuteTask;
import com.baidu.openapi.map.Place.Callback;

public class Geocoding {
	private static final String geocoderAPI = "http://api.map.baidu.com/geocoder/v2";
	
	public static interface Callback{
		void onSuccess(JSONObject ret);
		void onSuccess(JSONArray ret);
		void onFail(int errorCode, String errorMsg);
	}
	
	private String ak;
	
	public Geocoding(String apiKey){
		ak = apiKey;
	}
	
	public void search(String address, 
			String city,
			Callback cb)
	{
		StringBuilder queryString = new StringBuilder("ak=").append(ak);
		
		try{
			String charsetName = "UTF-8";
			queryString.append("&address=").append(URLEncoder.encode(address, charsetName))
				.append("&city=").append(URLEncoder.encode(city, charsetName))
				.append("&output=json");
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		try{
			URL url = new URL(geocoderAPI + "/search?" + queryString.toString());
			final Callback myCB = cb;
			
			MuteTask t = new MuteTask(url, 
					new MuteTask.Callback() {
				
						@Override
						public void onSuccess(JSONObject ret) {
							try{
								int status = ret.has("status") ? ret.getInt("status") : 0;
								if(0 == status){
									JSONArray arr = ret.has("results") ? ret.getJSONArray("results") : null;
									myCB.onSuccess(arr);
								}else{
									String errorMsg = ret.has("message") ? ret.getString("message") : "";
									/*
									if(status > 10){
										if(status/100 == 2){
											errorMsg = statusToMessage[6];
										}else if(status/100 == 3){
											errorMsg = statusToMessage[7];
										}
									}*/
									myCB.onFail(status, errorMsg);
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

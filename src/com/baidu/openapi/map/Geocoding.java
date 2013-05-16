package com.baidu.openapi.map;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import com.baidu.openapi.util.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Geocoding {
	private static final String TAG = "Geocoding";
	private static final String geocoderAPI = "http://api.map.baidu.com/geocoder/v2/";
	
	public static interface Callback{
		void onSuccess(JSONObject ret);
		void onFail(int errorCode, String errorMsg);
	}
	
	public static enum GeoLocationType{
		BAIDU_MERCATOR_COORDINATION,
		CHINA_MERCATOR_COORDINATION,
		GEOGRAPHY_COORDINATION
	}
	
	private String ak;
	
	public Geocoding(String apiKey){
		ak = apiKey;
	}
	
	public void addressToGeolocation(
			String address, 
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
			URL url = new URL(geocoderAPI + "?" + queryString.toString());
			Log.d(TAG, url.toString());
			final Callback myCB = cb;
			
			HttpRequest t = new HttpRequest(url,
					new HttpRequest.Callback() {
				
						@Override
						public void onSuccess(JSONObject ret) {
							try{
								int status = ret.has("status") ? ret.getInt("status") : 0;
								if(0 == status){
									JSONObject obj = ret.has("result") ? ret.getJSONObject("result") : null;
									myCB.onSuccess(obj);
								}else{
									/*
									if(status > 10){
										if(status/100 == 2){
											errorMsg = statusToMessage[6];
										}else if(status/100 == 3){
											errorMsg = statusToMessage[7];
										}
									}*/
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
	
	public void geolocationToAddress(
			float latitude, 
			float longitude, 
			Callback cb){
		geolocationToAddress(latitude, longitude, cb, GeoLocationType.BAIDU_MERCATOR_COORDINATION, false);
	}
	
	public void geolocationToAddress(
			float latitude, 
			float longitude, 
			Callback cb,
			GeoLocationType type,
			boolean showPoi)
	{
		StringBuilder queryString = new StringBuilder("ak=").append(ak);
		try{
			String charsetName = "UTF-8";
			queryString.append("&location=")
				.append(URLEncoder.encode(latitude + "," + longitude, charsetName))
				.append("&output=json")
				.append("&pois=")
				.append(showPoi ? 1 : 0);
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		switch(type){
			case BAIDU_MERCATOR_COORDINATION:
				queryString.append("&coordtype=bd09ll");
				break;
			case CHINA_MERCATOR_COORDINATION:
				queryString.append("&coordtype=gcj02ll");
				break;
			case GEOGRAPHY_COORDINATION:
				queryString.append("&coordtype=wgs84ll");
				break;
		}
		try{
			URL url = new URL(geocoderAPI + "?" + queryString.toString());
			Log.d(TAG, url.toString());
			final Callback myCB = cb;
			
			HttpRequest t = new HttpRequest(url,
					new HttpRequest.Callback() {
				
						@Override
						public void onSuccess(JSONObject ret) {
							try{
								int status = ret.has("status") ? ret.getInt("status") : 0;
								if(0 == status){
									JSONObject obj = ret.has("result") ? ret.getJSONObject("result") : null;
									myCB.onSuccess(obj);
								}else{
									myCB.onFail(status, "");
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

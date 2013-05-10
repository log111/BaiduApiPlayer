package com.baidu.openapi.map;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

import com.baidu.openapi.auth.MuteTask;
import com.baidu.openapi.map.Place.Filter.Order;

public class Place {
	private static final String placeAPI = "http://api.map.baidu.com/place/v2";
	
	public static enum ResultDetail{
		CORSE,
		DETAIL
	}
	
	public static class SearchRange{
		public SearchType type;
		public double[] squareBounds;//four 
		public String RegionName;
		public double[] circle;//0: center, 1: radius
	}
	
	public static enum SearchType{
		REGION, //search within a city
		BOUNDS,//square search
		LOCATION //circle search
	}
	
	public static class Filter{
		
		public enum Key{
			HOTEL_DEFAULT, 
			HOTEL_PRICE, 
			HOTEL_TOTAL_SCORE,
			HOTEL_LEVEL, 
			HOTEL_HEALTH_SCORE, 
			HOTEL_DISTANCE,	
			
			CATER_DEFAULT, 
			CATER_TASTE_RATING,
			CATER_PRICE, 
			CATER_OVERALL_RATING,
			CATER_SERVICE_RATING, 
			
			LIFE_DEFAULT, 
			LIFE_PRICE, 
			LIFE_OVERALL_RATING,
			LIFE_COMMENT_NUM,
			LIFE_DISTATNCE
		}
		
		public static enum Order{
			DESCENDING,
			ASCENDING
		}
		
		public Key filterKey;
		public Order sortOrder;
		public boolean hasGroupon;
		public boolean hasDiscount;
	}
	
	private String ak;
	
	public Place(String apiKey){
		ak = apiKey;
	}
	
	public void search(String query, SearchRange range, ResultDetail scope){
		search(query, range, scope, null, 10, 0);
	}
	
	public void search(String query, SearchRange range, ResultDetail scope, Filter filter){
		search(query, range, scope, filter, 10, 0);
	}
	
	public void search(String query, 
			SearchRange range,
			ResultDetail scope,
			Filter filter,
			int pageSize,
			int pageNumber)
	{
		StringBuilder postBody = new StringBuilder("ak=").append(ak);
		
		try{
			postBody.append("&q=").append(URLEncoder.encode(query, "UTF-8"));
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		
		switch(range.type){
			case REGION:
				postBody.append("&region=")
					.append(range.RegionName);
				break;
			case BOUNDS:
				postBody.append("&bounds=")
					.append(range.squareBounds[0])
					.append(",")
					.append(range.squareBounds[1])
					.append(",")
					.append(range.squareBounds[2])
					.append(",")
					.append(range.squareBounds[3]);
				break;
			case LOCATION:
				postBody.append("&location=")
					.append(range.circle[0])
					.append("&radius=")
					.append(range.circle[1]);
				break;
			default:break;
		}
		
		if(ResultDetail.DETAIL == scope && filter != null){
			StringBuilder filterBuf = new StringBuilder("filter=");
			
			switch(filter.filterKey){
				case HOTEL_DEFAULT:
					filterBuf.append("industry_type:hotel|sort_name:default");
					break;
				case HOTEL_DISTANCE:
					filterBuf.append("industry_type:hotel|sort_name:distance");
					break;
				case HOTEL_LEVEL:
					filterBuf.append("industry_type:hotel|sort_name:level");
					break;
				case HOTEL_HEALTH_SCORE:
					filterBuf.append("industry_type:hotel|sort_name:health_score");
					break;
				case HOTEL_PRICE:
					filterBuf.append("industry_type:hotel|sort_name:price");
					break;
				case HOTEL_TOTAL_SCORE:
					filterBuf.append("industry_type:hotel|sort_name:total_score");
					break;
				case CATER_DEFAULT:
					filterBuf.append("industry_type:cater|sort_name:default");
					break;
				case CATER_OVERALL_RATING:
					filterBuf.append("industry_type:cater|sort_name:overall_rating");
					break;
				case CATER_PRICE:
					filterBuf.append("industry_type:cater|sort_name:price");
					break;
				case CATER_SERVICE_RATING:
					filterBuf.append("industry_type:cater|sort_name:service_rating");
					break;
				case CATER_TASTE_RATING:
					filterBuf.append("industry_type:cater|sort_name:taste_rating");
					break;
				case LIFE_COMMENT_NUM:
					filterBuf.append("industry_type:life|sort_name:comment_num");
					break;
				case LIFE_DEFAULT:
					filterBuf.append("industry_type:life|sort_name:default");
					break;
				case LIFE_DISTATNCE:
					filterBuf.append("industry_type:life|sort_name:distance");
					break;
				case LIFE_OVERALL_RATING:
					filterBuf.append("industry_type:life|sort_name:overall_rating");
					break;
				case LIFE_PRICE:
					filterBuf.append("industry_type:life|sort_name:price");
					break;
				default:
					break;
			}
			
			if(filter.sortOrder == Order.ASCENDING){
				filterBuf.append("|sort_rule:1");
			}else{
				filterBuf.append("|sort_rule:0");
			}
			
			filterBuf.append(filter.hasGroupon ? "|groupon:1" : "|groupon:0");
			filterBuf.append(filter.hasDiscount ? "|discount:1" : "|discount:0");			
		
			postBody.append("&").append(filterBuf.toString());
		}
		
		if(scope == ResultDetail.CORSE){
			postBody.append("&scope=1");
		}else{
			postBody.append("&scope=2");
		}
		
		postBody.append("&output=json");
		postBody.append("&page_size=").append(pageSize);
		postBody.append("&page_number=").append(pageNumber);
		
		try{
			
			URL url = new URL(placeAPI + "/search");
			
			final String body = postBody.toString();
			
			MuteTask t = new MuteTask(url, new MuteTask.Callback() {
				
				@Override
				public void onSuccess(JSONObject ret) {
					
				}
				
				@Override
				public void onFail(JSONObject err, Exception localException) {
					// TODO Auto-generated method stub
					
				}
			},
			"application/octet-stream",
			new MuteTask.WriteHook() {
				
				@Override
				public void writeHttpBody(OutputStream out) throws IOException {
					out.write(body.getBytes());
				}
			});
			
			t.runAsync();
		
		}catch(MalformedURLException e){
			e.printStackTrace();
		}
	}
	
	public void detail(String poiUid, ResultDetail detail){
		
		StringBuilder postBody = new StringBuilder();
		
		postBody.append("ak=").append(ak);
		
		try{
			postBody.append("&uid=").append(URLEncoder.encode(poiUid, "UTF-8"));
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		
		if(detail == ResultDetail.CORSE){		
			postBody.append("&scope=1");
		}else{
			postBody.append("&scope=2");
		}
		
		postBody.append("&output=json");
		
		try{
			URL url = new URL(placeAPI + "/detail");
			
			final String body = postBody.toString();
			
			MuteTask t = new MuteTask(url, new MuteTask.Callback() {
				
				@Override
				public void onSuccess(JSONObject ret) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onFail(JSONObject err, Exception localException) {
					// TODO Auto-generated method stub
					
				}
			},
			"application/octet-stream",
			new MuteTask.WriteHook(){

				@Override
				public void writeHttpBody(OutputStream out) throws IOException {
					out.write(body.getBytes());
				}
				
			});
			
			t.runAsync();
			
		}catch(MalformedURLException e){
			e.printStackTrace();
		}
	}
}

package com.baidu.openapi.map;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.openapi.auth.MuteTask;
import com.baidu.openapi.map.Place.Filter.Order;

public class Place {
	public static final String[] statusToMessage
		= new String[]{
		"OK", //status == 0 
		"", 
		"Request parameter invalide",//status == 2 
		"Request verify failure", //status == 3
		"Quota failure", //status == 4
		"App key not exist or illegal", //status == 5
		"No permission", //status == 2xx
		"Quota error" //status == 3xx
	};
	
	private static final String placeAPI = "http://api.map.baidu.com/place/v2";
	
	public static enum ResultDetail{
		CORSE,
		DETAIL
	}
	
	public static class SearchRange implements Parcelable {
		public SearchType type;
		public double[] squareBounds;//four 
		public String regionName;
		public double[] circle;//0: center, 1: radius
		
		public SearchRange(){
			type = SearchType.REGION;
			squareBounds = new double[4];
			regionName = "";
			circle = new double[2];
		}
		
		@Override
		public int describeContents() {
			return 0;
		}
		
		@Override
		public void writeToParcel(Parcel out, int arg1) {
			
			out.writeInt(type.ordinal());
			out.writeDoubleArray(squareBounds);
			out.writeString(regionName);
			out.writeDoubleArray(circle);
		}
		
		public static final Parcelable.Creator<SearchRange> CREATEOR
			= new Creator<Place.SearchRange>() {
				
				@Override
				public SearchRange[] newArray(int size) {
					return new SearchRange[size];
				}
				
				@Override
				public SearchRange createFromParcel(Parcel in) {
					
					SearchRange sr = new SearchRange();
					sr.type = SearchType.values()[in.readInt()];
					in.readDoubleArray(sr.squareBounds);
					sr.regionName = in.readString();
					in.readDoubleArray(sr.circle);
					
					return sr;
				}
			};
	}
	
	public static enum SearchType{
		REGION, //search within a city
		BOUNDS,//square search
		LOCATION //circle search
	}
	
	public static class Filter implements Parcelable{
		
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
		
		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel out, int flags) {
			out.writeInt(filterKey.ordinal());
			out.writeInt(sortOrder.ordinal());
			out.writeBooleanArray(new boolean[]{hasGroupon, hasDiscount});
		}
		
		public static final Parcelable.Creator<Filter> CREATOR
			= new Creator<Place.Filter>() {
				
				@Override
				public Filter[] newArray(int size) {
					return new Filter[size];
				}
				
				@Override
				public Filter createFromParcel(Parcel in) {
					
					Filter f = new Filter();
					f.filterKey = Key.values()[in.readInt()];
					f.sortOrder = Order.values()[in.readInt()];
					boolean[] ba = new boolean[2];
					in.readBooleanArray(ba);
					f.hasGroupon = ba[0];
					f.hasDiscount = ba[1];
					
					return f;
				}
			};
	}
	
	public static interface Callback{
		void onSuccess(JSONObject ret);
		void onSuccess(JSONArray ret);
		void onFail(int errorCode, String errorMsg);
	}
	
	private String ak;
	
	public Place(String apiKey){
		ak = apiKey;
	}
	
	public void search(String query, SearchRange range, ResultDetail scope, Callback cb){
		search(query, range, scope, null, 10, 0, cb);
	}
	
	public void search(String query, SearchRange range, ResultDetail scope, Filter filter, Callback cb){
		search(query, range, scope, filter, 10, 0, cb);
	}
	
	public void search(String query, 
			SearchRange range,
			ResultDetail scope,
			Filter filter,
			int pageSize,
			int pageNumber,
			Callback cb)
	{
		StringBuilder queryString = new StringBuilder("ak=").append(ak);
		
		try{
			queryString.append("&q=").append(URLEncoder.encode(query, "UTF-8"));
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		
		switch(range.type){
			case REGION:
				try{
					queryString.append("&region=")
						.append(URLEncoder.encode(range.regionName, "UTF-8"));
				}catch(UnsupportedEncodingException e){
					e.printStackTrace();
				}
				break;
			case BOUNDS:
				queryString.append("&bounds=")
					.append(range.squareBounds[0])
					.append(",")
					.append(range.squareBounds[1])
					.append(",")
					.append(range.squareBounds[2])
					.append(",")
					.append(range.squareBounds[3]);
				break;
			case LOCATION:
				queryString.append("&location=")
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
		
			queryString.append("&").append(filterBuf.toString());
		}
		
		if(scope == ResultDetail.CORSE){
			queryString.append("&scope=1");
		}else{
			queryString.append("&scope=2");
		}
		
		queryString.append("&output=json");
		queryString.append("&page_size=").append(pageSize);
		queryString.append("&page_number=").append(pageNumber);
		
		try{
			URL url = new URL(placeAPI + "/search?" + queryString.toString());
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
	
	public void detail(String poiUid, ResultDetail detail, Callback cb){
		
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
			final Callback myCB = cb;
			
			MuteTask t = new MuteTask(url, new MuteTask.Callback() {
				
				@Override
				public void onSuccess(JSONObject ret) {
					try{
						int status = ret.has("status") ? ret.getInt("status") : 0;
						if(0 == status){
							JSONObject obj = ret.has("result") ? ret.getJSONObject("result") : null;
							myCB.onSuccess(obj);
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
			},
			null,
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

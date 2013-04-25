package com.baidu.auth;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.os.Bundle;
import android.util.Log;

public class UrlParser {
	private static final String TAG = "UrlParser";
	
	private static final String charsetName = "UTF-8";
	
	public static boolean isEmptyOrNull(String s){
		return (null == s) || 0 == s.trim().length();
	}
	
	public static Bundle decodeURLParams(String url){
        Bundle ret = new Bundle();
        try {
            URL myUrl = new URL(url);
            String query = myUrl.getQuery();//the part after ? but before #
            String anchor = myUrl.getRef();//the part after #
            Log.d(TAG, "query=" + query + "anchor=" + anchor);
            
            String[] paramSource = {query, anchor};
            
            for(String source : paramSource){
	            if(! isEmptyOrNull(source)){
		            String[] kvArray = source.split("&");
		        	for(String kv : kvArray){
		        		String[] keyVal = kv.split("=");
		        		if(keyVal.length != 2){
		        			continue;
		        		}else{
		        			String key = URLDecoder.decode(keyVal[0], charsetName);
		        			String value = URLDecoder.decode(keyVal[1], charsetName);
		        			ret.putString(key, value);
		        		}
		        	}
	            }
            }
        }catch(MalformedURLException e){
        	e.printStackTrace();
        }catch(UnsupportedEncodingException e){
        	e.printStackTrace();
        }
        return ret;
	}
	
	public static URL encodeURLParams(String url, Bundle params){
		
		StringBuilder sb = new StringBuilder();
		try{
			boolean isFirst = true;

			for(String k : params.keySet()){
				String val = params.getString(k);
				if(isFirst){
					isFirst = false;
					sb.append('?');
				}else{
					sb.append('&');
				}
				sb.append(URLEncoder.encode(k, charsetName))
				  .append('=')
				  .append(URLEncoder.encode(val, charsetName));
			}
			return new URL(url + sb.toString());
		}catch(UnsupportedEncodingException e){
			Log.e(TAG, e.getMessage());
		}catch(MalformedURLException e){
			Log.e(TAG, e.getMessage());
		}
		return null;
	}
}

package com.baidu.openapi.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class URLBuilder {
	
	public static enum PROTOCOL{
		FILE,
		FTP,
		HTTP,
		HTTPS,
		JAR
	}
	
	private static URLBuilder builder;
	
	private String mProto; //
	private String mAuthority;
	private String mHost;
	private int mPort;
	private String mUserInfo;
	private String mUrl;
	private String mFile;
	private StringBuilder mPath;
	private StringBuilder mQuery;
	private String mRef;
	
	public URLBuilder(){
		mPath = new StringBuilder();
		mQuery = new StringBuilder();
	}
	
	public URLBuilder setProtocol(String protocol){
		mProto = protocol;
		return this;
	}
	
	public URLBuilder setAuthority(String authority){
		mAuthority = authority;
		return this;
	}
	
	public URLBuilder setUserInfo(String userInfo){
		mUserInfo = userInfo;
		return this;
	}
	
	public URLBuilder setBaseUrl(String url){
		mUrl = url;
		return this;
	}
	
	public URLBuilder apppendPath(String path){
		
		int len = mPath.length();
		
		if(path.charAt(0) == '/'){
			if(len != 0 && mPath.charAt(len-1) == '/'){
				mPath.deleteCharAt(len-1);
			}
		}else{
			if(len ==0 || (len != 0 && mPath.charAt(len-1) != '/')){
				mPath.append('/');
			}
		}
		mPath.append(path);
		return this;
	}
	
	private static final String charsetName = "UTF-8";
	
	public URLBuilder appendQuery(String key, String val){
		if(mQuery.length() > 0){
			mQuery.append('&');
		}
		try{
			mQuery.append(URLEncoder.encode(key, charsetName))
				.append('=')
				.append(URLEncoder.encode(val, charsetName));
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return this;
	}
	
	public URL build() throws MalformedURLException{
		
		StringBuilder url = new StringBuilder();
		
		if(mUrl!=null && !mUrl.trim().isEmpty()){
			url.append(mUrl);
		}
		if(mPath.length() > 0){
			url.append(mPath);
		}
		if(mQuery.length() > 0){
			url.append('?').append(mQuery);
		}
		
		return new URL(url.toString());
	}
	
	public String getUri(){
		if(mFile != null){
			return mFile;
		}else{
			return mPath.toString() + "?" + mQuery.toString();
		}
	}
}

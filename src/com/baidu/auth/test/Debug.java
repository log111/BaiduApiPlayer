package com.baidu.auth.test;

import android.os.Bundle;
import android.util.Log;

public class Debug {
	
	private String mTag = null;
	
	public Debug(String tag){
		mTag = tag;
	}
	
	public void printBundle(Bundle b){
		if(b.isEmpty()){
			Log.d(mTag, "Empty Bundle");
		}else{
			StringBuilder sb = new StringBuilder();
			for(String k : b.keySet()){
				sb.append(k).append(':').append(b.getString(k));
			}
			Log.d(mTag, sb.toString());
		}
	}
}

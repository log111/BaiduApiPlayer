package com.baidu.openapi.test;

import android.app.Activity;
import android.os.Bundle;

import com.baidu.openapi.R;
import com.baidu.openapi.lbs.DataBox;
import com.baidu.openapi.lbs.LBSManager;

public class LBSAPIDemo extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		LBSManager mgr = new LBSManager(
				getString(R.string.map_api_key),
				getString(R.string.map_secret_key)
			);
		
        DataBox box = new DataBox();
        box.chineseName = "笔记";
        mgr.saveBoxAsync(box);
	}
}

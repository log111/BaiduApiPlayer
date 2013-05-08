package com.baidu.openapi.test;

import android.app.Activity;
import android.os.Bundle;

import com.baidu.openapi.R;
import com.baidu.openapi.lbs.DataBox;
import com.baidu.openapi.lbs.LBSManager;

public class LBSTestActivity extends Activity {
	
	private LBSManager mgr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.lbs_test_activity);
		
		mgr = new LBSManager(getString(R.string.api_key));
        
        DataBox box = new DataBox();
        box.chineseName = "百度";
        box.englishName = "baidu";
        mgr.saveBoxAsync(box);
	}
}

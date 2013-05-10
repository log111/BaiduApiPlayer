package com.baidu.openapi.test;

import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.baidu.openapi.R;

public class MapAPIDemo extends ListActivity {
	
	private Map<String, Intent> intentMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initIntent();
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this,
				R.array.baiduMapAPI, 
				android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
		
		getListView().setTextFilterEnabled(true);
	}
	
	private void initIntent(){
		
		intentMap = new HashMap<String, Intent>();
		
		String[] openAPI = getResources().getStringArray(R.array.baiduMapAPI);
		
		Context appCtx = getApplicationContext();
		
		Intent i = new Intent();
		i.setClass(appCtx, PlaceAPIDemo.class);
		intentMap.put(openAPI[0], i);
		
		i = new Intent();
		i.setClass(appCtx, LBSAPIDemo.class);
		intentMap.put(openAPI[1], i);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		String label = (String) l.getItemAtPosition(position);
		Intent i = intentMap.get(label);
		if(i != null){
			startActivity(i);
		}
	}
}

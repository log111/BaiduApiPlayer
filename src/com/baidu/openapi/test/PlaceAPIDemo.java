package com.baidu.openapi.test;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.baidu.openapi.R;
import com.baidu.openapi.map.Place;
import com.baidu.openapi.map.Place.Filter;
import com.baidu.openapi.map.Place.ResultDetail;
import com.baidu.openapi.map.Place.SearchRange;
import com.baidu.openapi.map.Place.SearchType;

public class PlaceAPIDemo extends Activity{
	
	private Place placeAPI;
	
	private String mKeyword;
	private SearchRange mRange;
	private ResultDetail mScope;
	private Filter mFilter;
	private int pageNumber;
	private int pageSize;
	
	private EditText searchBox;
	private EditText recordNumberBox;
	private EditText pageNumberBox;
	
	private Map<String, SearchType> searchTypeMap;
	private Map<String, ResultDetail> retDetailMap;
	
	private void getData(){
		mKeyword = searchBox.getText().toString().trim();
		pageNumber = Integer.valueOf(pageNumberBox.getText().toString());
		pageSize = Integer.valueOf(recordNumberBox.getText().toString());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String ak = getString(R.string.map_api_key);
		placeAPI = new Place(ak);
		
		setContentView(R.layout.place_api_demo);
		
		searchBox = (EditText) findViewById(R.id.searchBox);
		
		Spinner searchRangeSpinner = 
				(Spinner) findViewById(R.id.searchRangeSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this,
				R.array.searchRangeArray, 
				android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchRangeSpinner.setAdapter(adapter);
        searchRangeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String label = (String) arg0.getSelectedItem();
				mRange = new SearchRange();
				mRange.type = searchTypeMap.get(label);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        	
		});
        
        Resources res = getResources();
        
        SearchType[] searchTypeVal = 
        		new SearchType[]{ 
        			SearchType.REGION, 
        			SearchType.BOUNDS, 
        			SearchType.LOCATION
        		};
        String[] searchTypeLabel = res.getStringArray(R.array.searchRangeArray);
        
        searchTypeMap = new HashMap<String, SearchType>();
        for(int i=0;i<searchTypeLabel.length;i++){
        	searchTypeMap.put(searchTypeLabel[i], searchTypeVal[i]);
        }
        
        Spinner resultDetailSpinner = (Spinner) findViewById(R.id.resultDetailSpinner);
		ArrayAdapter<CharSequence> resultDetailAdapter = ArrayAdapter.createFromResource(
				this,
				R.array.resultDetail, 
				android.R.layout.simple_spinner_item);
		resultDetailAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resultDetailSpinner.setAdapter(resultDetailAdapter);
        resultDetailSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String label = (String) arg0.getSelectedItem();
				mScope = retDetailMap.get(label);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        	
		});
        
        ResultDetail[] retDetailVal = 
        		new ResultDetail[]{ 
        			ResultDetail.CORSE, 
        			ResultDetail.DETAIL
        		};
        String[] retDetailLabel = res.getStringArray(R.array.resultDetail);
        
        retDetailMap = new HashMap<String, ResultDetail>();
        for(int i=0;i<retDetailLabel.length;i++){
        	retDetailMap.put(retDetailLabel[i], retDetailVal[i]);
        }
        
        Button b = (Button) findViewById(R.id.searchButton);
        b.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View arg0) {
				getData();
				placeAPI.search(mKeyword, mRange, mScope, mFilter, pageSize, pageNumber);
			}
		});
	}
}

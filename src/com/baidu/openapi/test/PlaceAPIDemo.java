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
	
	private EditText regionBox;
	private EditText leftbottom_lat;
	private EditText leftbottom_lon;
	private EditText righttop_lat;
	private EditText righttop_lon;
	private EditText center_lat;
	private EditText center_lon;
	private EditText radiusBox;
	
	private EditText searchBox;
	private EditText recordNumberBox;
	private EditText pageNumberBox;
	private View[] groups;
	private View currentGroup;
	
	private Map<String, SearchType> searchTypeMap;
	private Map<String, ResultDetail> retDetailMap;
	
	private void getData(){
		switch(mRange.type){
			case REGION:
			{
				mRange.RegionName = regionBox.getText().toString().trim();
				break;
			}
			case BOUNDS:
			{
				double[] data = new double[4];
				data[0] = Double.valueOf(leftbottom_lat.getText().toString());
				data[1] = Double.valueOf(leftbottom_lon.getText().toString());
				data[2] = Double.valueOf(righttop_lat.getText().toString());
				data[3] = Double.valueOf(righttop_lon.getText().toString());
				mRange.squareBounds = data;
				break;
			}
			case LOCATION:
			{
				double[] data = new double[2];
				data[0] = Double.valueOf(center_lat.getText().toString());
				data[1] = Double.valueOf(center_lon.getText().toString());
				data[2] = Double.valueOf(radiusBox.getText().toString());
				mRange.circle = data;
				break;
			}
			default:
				break;
		}
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
		
		regionBox = (EditText) findViewById(R.id.regionBox);
		leftbottom_lat = (EditText) findViewById(R.id.leftbottom_lat);
		leftbottom_lon = (EditText) findViewById(R.id.leftbottom_lon);
		righttop_lat = (EditText) findViewById(R.id.righttop_lat);
		righttop_lon = (EditText) findViewById(R.id.righttop_lon);
		center_lat = (EditText) findViewById(R.id.center_lat);
		center_lon = (EditText) findViewById(R.id.center_lon);
		radiusBox = (EditText) findViewById(R.id.radiusBox);
		searchBox = (EditText) findViewById(R.id.searchBox);
		pageNumberBox = (EditText) findViewById(R.id.pageNumberBox);
		recordNumberBox = (EditText) findViewById(R.id.recordNumberBox);
				        
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
        
        groups = new View[3];
        groups[0] = findViewById(R.id.regionSearchGroup);
        groups[1] = findViewById(R.id.squareSearchGroup);
        groups[2] = findViewById(R.id.circleSearchGroup);
        
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
				int pos = arg0.getSelectedItemPosition();
				if(currentGroup != null){
					currentGroup.setVisibility(View.GONE);
				}
				currentGroup = groups[pos];
				currentGroup.setVisibility(View.VISIBLE);
				
				String label = (String) arg0.getSelectedItem();
				mRange = new SearchRange();
				mRange.type = searchTypeMap.get(label);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        	
		});

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

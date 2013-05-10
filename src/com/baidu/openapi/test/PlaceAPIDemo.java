package com.baidu.openapi.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.baidu.openapi.R;

public class PlaceAPIDemo extends Activity implements OnItemSelectedListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.place_api_demo);
		
		Spinner searchRangeSpinner = 
				(Spinner) findViewById(R.id.searchRangeSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this,
				R.array.searchRangeArray, 
				android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        searchRangeSpinner.setAdapter(adapter);
        searchRangeSpinner.setOnItemSelectedListener(this);
        
        Spinner resultDetailSpinner = (Spinner) findViewById(R.id.resultDetailSpinner);
		ArrayAdapter<CharSequence> resultDetailAdapter = ArrayAdapter.createFromResource(
				this,
				R.array.searchRangeArray, 
				android.R.layout.simple_spinner_item);
		resultDetailAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        resultDetailSpinner.setAdapter(resultDetailAdapter);
        resultDetailSpinner.setOnItemSelectedListener(this);
        
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
}

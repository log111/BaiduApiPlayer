package com.baidu.openapi.test;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.baidu.openapi.R;
import com.baidu.openapi.map.Geocoding;

public class GeocodingAPIDemo extends Activity {
	
	private static final String TAG = "GeocodingAPIDemo";
	private Geocoding geocoder;
	
	private EditText addressBox;
	private EditText cityBox;
	private EditText latBox;
	private EditText lngBox;
	private Button switchButton;
	private boolean decodeIt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.geocoding_api_demo);
		
		addressBox = (EditText) findViewById(R.id.addressBox);
		cityBox = (EditText) findViewById(R.id.cityBox);
		latBox = (EditText) findViewById(R.id.latBox);
		lngBox = (EditText) findViewById(R.id.lngBox);
		switchButton = (Button) findViewById(R.id.switchButton);
		
		Button transformButton = (Button) findViewById(R.id.transformButton);
		transformButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				if(decodeIt){//to coordination
					String address = addressBox.getText().toString().trim();
			        String city = cityBox.getText().toString().trim();
			        if(! address.isEmpty() && !city.isEmpty()){
			        
				        geocoder.addressToGeolocation(address, city, new Geocoding.Callback() {
							
							@Override
							public void onSuccess(JSONObject ret) {
								Log.d(TAG, ret.toString());
							}
							
							@Override
							public void onFail(int errorCode, String errorMsg) {
								Log.d(TAG, "error code: " + errorCode);
							}
						});
			        }
			    }else{//to address
			    	float latitude = Float.valueOf(latBox.getText().toString());
			    	float longitude = Float.valueOf(lngBox.getText().toString());
			    	geocoder.geolocationToAddress(latitude, longitude, new Geocoding.Callback() {
						
						@Override
						public void onSuccess(JSONObject ret) {
							Log.d(TAG, ret.toString());
						}
						
						@Override
						public void onFail(int errorCode, String errorMsg) {
							Log.d(TAG, "error code: " + errorCode);
						}
					});
			    }
			}
		});
		
		String ak = getString(R.string.map_api_key);
		geocoder = new Geocoding(ak);
	}
	
	public void onClickSwitch(View view) {	    
		decodeIt = ((ToggleButton) view).isChecked();
	}
}

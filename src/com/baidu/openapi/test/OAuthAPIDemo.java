package com.baidu.openapi.test;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.baidu.openapi.R;
import com.baidu.openapi.auth.BaiduOAuth;
import com.baidu.openapi.auth.BaiduOAuth.TokenCallback;

public class OAuthAPIDemo extends Activity implements OnItemSelectedListener {

	private static final String TAG = "MainActivity";
	
	private BaiduOAuth mOAuth;
	
	private String mSelectedAuthType;
	
	private Button validateByAuchCodeButton;
	private Button refreshButton;
	private Button validateByImplicitGrantButton;
	private Button validateByCredentialButton;
	private Button validateByDeviceButton;
	
	private String mRefreshToken;
	
	private void authCodeValidation(){
		
		final String apiKey = getString(R.string.api_key);
		final String secretKey = getString(R.string.secret_key);
		try{
			final URL redirectUrl = new URL("http://www.example.com/oauth_redirect");
			mOAuth.validateByAuthCode(
					apiKey, 
					secretKey, 
					redirectUrl,
					mSelectedAuthType,
					"", 
					new BaiduOAuth.TokenCallback(){

				@Override
				public void onSuccess(String access_token, 
						long expires_in, 
						String refresh_token,
						String scope,
						String session_key,
						String session_secret){
					Log.d(TAG, "token: " + access_token);
					Log.d(TAG, "refresh token: " + refresh_token);
					
					Log.d(TAG, "token: " + access_token);
					Log.d(TAG, "expires in " + expires_in);
					Log.d(TAG, "refresh_token: " + refresh_token);
					Log.d(TAG, "scope: " + scope);
					Log.d(TAG, "session_key: " + session_key);
					Log.d(TAG, "session_secret: " + session_secret);
					
					mRefreshToken = refresh_token;
					refreshButton.setEnabled(true);
				}

				@Override
				public void onFail(String... ret) {
					Log.d(TAG, "errCode=" + ret[0] + " errMsg=" + ret[1]);
				}			
			});
        }catch(MalformedURLException e){
			e.printStackTrace();
		}
	}
	
	private void implicitGrantValidation(){
		final String apiKey = getString(R.string.api_key);
		final String secretKey = getString(R.string.secret_key);
		//try{
			//final URL redirectUrl = new URL("http://www.example.com/oauth_redirect");
			mOAuth.validateByImplicitGrant(
					apiKey, 
					secretKey, 
					//redirectUrl,
					null,
					mSelectedAuthType,
					new BaiduOAuth.TokenCallback()
			{
				@Override
				public void onSuccess(String access_token, 
						long expires_in, 
						String scope,
						String state, //no refresh_token, but state
						String session_key,
						String session_secret){
					
					Log.d(TAG, "token: " + access_token);
					Log.d(TAG, "expires in " + expires_in);
					Log.d(TAG, "state: " + state);
					Log.d(TAG, "scope: " + scope);
					Log.d(TAG, "session_key: " + session_key);
					Log.d(TAG, "session_secret: " + session_secret);
				}

				@Override
				public void onFail(String... ret) {
					Log.d(TAG, "errCode=" + ret[0] + " errMsg=" + ret[1]);
				}			
			});
			/*
        }catch(MalformedURLException e){
			e.printStackTrace();
		}*/
	}
	
	private void clientCredentialValidation(){
		final String apiKey = getString(R.string.api_key);
		final String secretKey = getString(R.string.secret_key);
        
		mOAuth.validateByCredential(
				apiKey, 
				secretKey, 
				mSelectedAuthType, //cannot be basic
				new BaiduOAuth.TokenCallback(){

					@Override
					public void onSuccess(String access_token, 
							long expires_in, 
							String refresh_token,
							String scope,
							String session_key,
							String session_secret){
						
						Log.d(TAG, "token: " + access_token);
						Log.d(TAG, "expires in " + expires_in);
						Log.d(TAG, "refresh_token: " + refresh_token);
						Log.d(TAG, "scope: " + scope);
						Log.d(TAG, "session_key: " + session_key);
						Log.d(TAG, "session_secret: " + session_secret);
						
						mRefreshToken = refresh_token;
						refreshButton.setEnabled(true);
					}

					@Override
					public void onFail(String... ret) {
						String errCode = ret[0];
						String errMsg = ret[1];
						Log.d(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
					}			
				});
	}
	
	private void deviceValidation(){
		
		final String apiKey = getString(R.string.api_key);
		final String secretKey = getString(R.string.secret_key);
		
		mOAuth.validateByDevice(
				apiKey, 
				secretKey, 
				mSelectedAuthType, 
				new TokenCallback(){

					@Override
					public void onSuccess(String access_token, long expires_in,
							String refresh_token, String scope,
							String session_key, String session_secret) {
						
						Log.d(TAG, "token: " + access_token);
						Log.d(TAG, "expires in " + expires_in);
						Log.d(TAG, "refresh_token: " + refresh_token);
						Log.d(TAG, "scope: " + scope);
						Log.d(TAG, "session_key: " + session_key);
						Log.d(TAG, "session_secret: " + session_secret);
						
						mRefreshToken = refresh_token;
						refreshButton.setEnabled(true);
					}

					@Override
					public void onFail(String... ret) {
						String errCode = ret[0];
						String errMsg = ret[1];
						Log.d(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
					}
					
				});
	}
	
	private void refreshToken(){
		
		final String apiKey = getString(R.string.api_key);
		final String secretKey = getString(R.string.secret_key);
		
		Log.d(TAG, "ready to refresh");
		Log.d(TAG, "refresh_token :");
		Log.d(TAG, mRefreshToken);
		
		mOAuth.refreshToken(
				apiKey, 
				secretKey, 
				mRefreshToken,
				mSelectedAuthType, 
				new BaiduOAuth.TokenCallback(){

			@Override
			public void onSuccess(String access_token, 
					long expires_in, 
					String refresh_token,
					String scope,
					String session_key,
					String session_secret){
				Log.d(TAG, "token: " + access_token);
				Log.d(TAG, "refresh token: " + refresh_token);
				
				mRefreshToken = refresh_token;
				refreshButton.setEnabled(true);
			}

			@Override
			public void onFail(String... ret) {
				Log.d(TAG, "errCode=" + ret[0] + " errMsg=" + ret[1]);
				
				refreshButton.setEnabled(false);
			}			
		});        
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.oauth_api_demo);
        
        mOAuth = new BaiduOAuth(this);
        
        Spinner spinner = (Spinner) findViewById(R.id.authorizationList);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
             R.array.authTypeArray, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        
        validateByAuchCodeButton = 
        		(Button) findViewById(R.id.validateByAuchCode);
        validateByAuchCodeButton.setOnClickListener(
        new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				authCodeValidation();
			}
		});
        
        refreshButton = (Button) findViewById(R.id.refreshToken);
        refreshButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refreshToken();
			}
		});
        refreshButton.setEnabled(false);
        
        validateByImplicitGrantButton = 
        		(Button) findViewById(R.id.validateByImplicitGrant);
        validateByImplicitGrantButton.setOnClickListener(
        new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				implicitGrantValidation();
			}
		});
        
        validateByCredentialButton =
        		(Button) findViewById(R.id.validateByCredential);
        validateByCredentialButton.setOnClickListener(
    		new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					clientCredentialValidation();
				}
			});
        
        validateByDeviceButton =
        		(Button) findViewById(R.id.validateByDevice);
        validateByDeviceButton.setOnClickListener(
        	new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					deviceValidation();
				}
			});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public void onItemSelected(AdapterView<?> view, View arg1, int arg2,
			long arg3) {
		Log.d(TAG, "onItemSelected ent");
		mSelectedAuthType = (String) view.getSelectedItem();
		Log.d(TAG, mSelectedAuthType);
		Log.d(TAG, "onItemSelected ret");
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		Log.d(TAG, "onNothingSelected ent");
		Log.d(TAG, "onNothingSelected ret");
	}
    
}

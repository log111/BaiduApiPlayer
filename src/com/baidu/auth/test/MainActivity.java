package com.baidu.auth.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.baidu.auth.BaiduOAuth;
import com.example.apitester.R;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
	private Button getAuthCodeButton;
	private Button getTokenButton;
	private BaiduOAuth mOAuth;
	
	private String mAuthCode;
	
	private void getAuthCode(){
		
		final String apiKey = getString(R.string.api_key);
		
        final String redirectUrl = "http://www.example.com/oauth_redirect";
        
        mOAuth.getAuthCode(apiKey, 
        		redirectUrl,
        		new BaiduOAuth.Callback(){

					@Override
					public void onSuccess(String authCode) {
						Log.d(TAG, "authorized code: " + authCode);
						mAuthCode = authCode;
						
						getTokenButton.setEnabled(true);
						
					}

					@Override
					public void onFail(String errCode, String errMsg) {
						Log.d(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
					}
        		}
        );
	}
	
	private void getToken(){
		final String apiKey = getString(R.string.api_key);
		final String secretKey = getString(R.string.secret_key);
        final String redirectUrl = "http://www.example.com/oauth_redirect";
        
		mOAuth.getAccessTokenByAuthCode(
				apiKey, 
				secretKey, 
				mAuthCode, 
				redirectUrl, new BaiduOAuth.TokenCallback(){

					@Override
					public void onSuccess(String access_token, 
							long expires_in, 
							String refresh_token,
							String scope,
							String session_key,
							String session_secret){
						Log.d(TAG, "token: " + access_token);
						Log.d(TAG, "refresh token: " + refresh_token);
						getTokenButton.setEnabled(false);
					}

					@Override
					public void onFail(String errCode,
							String errMsg) {
						Log.d(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
					}			
				});
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        mOAuth = new BaiduOAuth(this);
        
        getAuthCodeButton = (Button) findViewById(R.id.getAuthCode);
        getAuthCodeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getAuthCode();
			}
		});
        
        getTokenButton = (Button) findViewById(R.id.getAccessToken);
        getTokenButton.setEnabled(false);
        getTokenButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getToken();
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}

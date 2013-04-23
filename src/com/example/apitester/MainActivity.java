package com.example.apitester;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.baidu.auth.BaiduOAuth;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
	private void connectBaiduPassport(){
		
		final String apiKey = getString(R.string.api_key);
		final String secretKey = getString(R.string.secret_key);
        final String user = getString(R.string.baidu_user);
        final String pwd = getString(R.string.baidu_password);
        
        final String redirectUrl = "http://www.example.com/oauth_redirect";
        String successUrl = "http://www.example.com/onSuccess";
        String failUrl = "http://www.example.com/onFail";
        
        final BaiduOAuth oauth = new BaiduOAuth(this);
        oauth.getAuthCode(apiKey, 
        		redirectUrl, successUrl, failUrl,
        		new BaiduOAuth.Callback(){

					@Override
					public void onSuccess(String authCode) {
						Log.d(TAG, "authorized code: " + authCode);
						/*
						oauth.getAccessTokenByAuthCode(
								apiKey, 
								secretKey, 
								authCode, 
								redirectUrl, new BaiduOAuth.Callback(){

									@Override
									public void onSuccess(String accessToken) {
										Log.d(TAG, "token: " + accessToken);
									}

									@Override
									public void onFail(String errCode,
											String errMsg) {
										Log.d(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
									}
									
								});
								*/
					}

					@Override
					public void onFail(String errCode, String errMsg) {
						Log.d(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
					}
        		}
        );
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        Button b = (Button) findViewById(R.id.getAuthCode);
        b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				connectBaiduPassport();
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

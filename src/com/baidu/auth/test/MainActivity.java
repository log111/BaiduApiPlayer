package com.baidu.auth.test;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.baidu.auth.BaiduOAuth;
import com.baidu.auth.InteractionManager;
import com.baidu.auth.R;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
	private BaiduOAuth mOAuth;
	
	private Button getAuthCodeButton;
	private Button getTokenButton;
	private Button validateByAuchCodeButton;
	private Button refreshButton;
	private Button validateByImplicitGrantButton;
	private Button validateByCredentialButton;
	
	private String mAuthCode;
	private String mRefreshToken;
	
	private void getAuthCode(){
		
		final String apiKey = getString(R.string.api_key);
		
		try{
	        final URL redirectUrl = new URL("http://www.example.com/oauth_redirect");
	        
	        mOAuth.getAuthCode(apiKey, 
	        		redirectUrl,
	        		"basic",
	        		new InteractionManager.Callback(){
	
						@Override
						public void onSuccess(Bundle vals) {
							String authCode = vals.containsKey("code")
									? vals.getString("code")
									: "";
							Log.d(TAG, "authorized code: " + authCode);
							mAuthCode = authCode;
							
							getTokenButton.setEnabled(true);
							
						}
	
						@Override
						public void onFail(
								String errCode, 
								String errMsg,
								String state) {
							Log.d(TAG, "errCode=" + errCode + 
									" errMsg=" + errMsg + 
									" state=" + state);
						}
	        		}
	        );
		}catch(MalformedURLException e){
			e.printStackTrace();
		}
	}
	
	private void clientCredentialValidation(){
		final String apiKey = getString(R.string.api_key);
		final String secretKey = getString(R.string.secret_key);
        
		mOAuth.validateByCredential(
				apiKey, 
				secretKey, 
				"", //cannot be basic
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
						Log.d(TAG, "scope: " + scope);
					}

					@Override
					public void onFail(String... ret) {
						String errCode = ret[0];
						String errMsg = ret[1];
						Log.d(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
					}			
				});
	}
	
	private void getToken(){
		final String apiKey = getString(R.string.api_key);
		final String secretKey = getString(R.string.secret_key);
        final String redirectUrl = "http://www.example.com/oauth_redirect";
        
		mOAuth.getTokenByAuthCode(
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
						
						mRefreshToken = refresh_token;
						getTokenButton.setEnabled(false);
					}

					@Override
					public void onFail(String... ret) {
						String errCode = ret[0];
						String errMsg = ret[1];
						Log.d(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
					}			
				});
	}

	private void authCodeValidation(){
		
		final String apiKey = getString(R.string.api_key);
		final String secretKey = getString(R.string.secret_key);
		try{
			final URL redirectUrl = new URL("http://www.example.com/oauth_redirect");
			mOAuth.validateByAuchCode(
					apiKey, 
					secretKey, 
					redirectUrl, 
					"basic", 
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
		try{
			final URL redirectUrl = new URL("http://www.example.com/oauth_redirect");
			mOAuth.validateByImplicitGrant(
					apiKey, 
					secretKey, 
					redirectUrl, 
					"basic",
					new BaiduOAuth.TokenCallback()
			{
				@Override
				public void onSuccess(String access_token, 
						long expires_in, 
						String scope,
						String state,
						String session_key,
						String session_secret){
					Log.d(TAG, "token: " + access_token);
					Log.d(TAG, "state: " + state);
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
				"basic", 
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
        
        setContentView(R.layout.activity_main);
        
        mOAuth = new BaiduOAuth(this);
        
        getAuthCodeButton = (Button) findViewById(R.id.getAuthCode);
        getAuthCodeButton.setEnabled(false);
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}

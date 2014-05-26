package com.reflectmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.reflectmobile.R;

public class LoginActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	public void onClickLogInFacebook(View button) {
		Intent intent = new Intent(this, NetworkActivity.class);
		startActivity(intent);
	}

	public void onClickLogInGoogle(View button) {
		Intent intent = new Intent(this, NetworkActivity.class);
		startActivity(intent);
	}

}

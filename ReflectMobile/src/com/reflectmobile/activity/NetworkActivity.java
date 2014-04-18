package com.reflectmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.reflectmobile.R;

public class NetworkActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network);
	}
	
	public void onClickCommunity(View image) {
		Intent intent = new Intent(this, CommunityActivity.class);
		startActivity(intent);
	}
}

package com.reflectmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.reflectmobile.R;

public class CommunityActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community);
	}
	
	public void onClickMoment(View image) {
		Intent intent = new Intent(this, MomentActivity.class);
		startActivity(intent);
	}
}

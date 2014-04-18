package com.reflectmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.reflectmobile.R;

public class MomentActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_moment);
	}
	
	public void onClickPhotoVideo(View image) {
		Intent intent = new Intent(this, PhotoVideoActivity.class);
		startActivity(intent);
	}
}

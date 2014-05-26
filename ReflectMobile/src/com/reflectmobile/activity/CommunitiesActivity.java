package com.reflectmobile.activity;

import org.json.JSONObject;

import android.content.Intent;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.R.color;
import com.reflectmobile.utility.NetworkManager;

import com.reflectmobile.utility.NetworkManager.*;

public class CommunitiesActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_communities);
		// Remove back button on the action bar
		getActionBar().setDisplayHomeAsUpEnabled(false);
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTextColor(getResources().getColor(R.color.yellow));
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.communities_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void onClickCommunity(View image) {
		Intent intent = new Intent(this, CommunityActivity.class);
		startActivity(intent);
	}
}

package com.reflectmobile.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.reflectmobile.R;

public class MomentActivity extends BaseActivity {

	private String TAG = "MomentActivity";
	private ActionBarDrawerToggle drawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_moment);
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTextColor(getResources().getColor(R.color.yellow));
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

		String jsonString = getIntent().getStringExtra("moment_data");
		Log.d(TAG, jsonString);
		
		GridView parentView = (GridView) findViewById(R.id.parentView);
		parentView.setAdapter(new ArrayAdapter<String>(this,
				R.layout.moment_photo));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.moment_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
		// Handle action buttons
		// switch (item.getItemId()) {
		// case :
		//
		// default:
		// return super.onOptionsItemSelected(item);
		// }
	}

	public void onClickPhoto(View image) {
		Intent intent = new Intent(this, PhotoActivity.class);
		startActivity(intent);
	}

}

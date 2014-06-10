package com.reflectmobile.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.reflectmobile.R;

public class CommunitiesActivity extends BaseActivity {

	private String TAG = "CommunitiesActivity";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_communities);
		super.onCreate(savedInstanceState);
		
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) title
		        .getLayoutParams();
		mlp.setMargins(5, 0, 0, 0);
		title.setTextColor(getResources().getColor(R.color.yellow));
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

		String jsonString = getIntent().getStringExtra("communities_data");
		GridView parentView = (GridView) findViewById(R.id.parentView);
		parentView.setAdapter(new CardAdapter(this, jsonString));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.communities_menu, menu);
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
	
}

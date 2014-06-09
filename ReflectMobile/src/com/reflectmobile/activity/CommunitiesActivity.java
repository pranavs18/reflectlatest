package com.reflectmobile.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.reflectmobile.R;

public class CommunitiesActivity extends BaseActivity {

	private String TAG = "CommunitiesActivity";
	private ActionBarDrawerToggle drawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_communities);

		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTextColor(getResources().getColor(R.color.yellow));
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

		String jsonString = getIntent().getStringExtra("communities_data");
		GridView parentView = (GridView) findViewById(R.id.parentView);
		parentView.setAdapter(new CardAdapter(this, jsonString));

		ListView sideMenuView = (ListView) findViewById(R.id.left_drawer);
		String[] menuItemTitles = getResources().getStringArray(
				R.array.side_menu_titles);

		// Get drawable resources from string-array
		TypedArray imgs = getResources().obtainTypedArray(R.array.side_menu_drawables);
		final int drawables[] = new int[imgs.length()];
		for (int i=0; i<imgs.length(); i++){
			drawables[i] = imgs.getResourceId(i, -1);
		}
		imgs.recycle();
		
		// Set the adapter for the list view
		sideMenuView.setAdapter(new ArrayAdapter<String>(this,
				R.layout.side_menu_item, menuItemTitles){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// Setting the image drawable for the view
				View v = super.getView(position, convertView, parent);
				((TextView) v).setCompoundDrawablesWithIntrinsicBounds(drawables[position], 0, 0, 0);
				return v;
			}
		});
		// Set the list's click listener
		sideMenuView.setOnItemClickListener(new SideMenuItemClickListener());

		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_navigation_drawer, R.string.error_title,
				R.string.error_title);

		// Set the drawer toggle as the DrawerListener
		drawerLayout.setDrawerListener(drawerToggle);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.communities_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
		// Handle action buttons
		// switch (item.getItemId()) {
		// case :
		//
		// default:
		// return super.onOptionsItemSelected(item);
		// }
	}

	public void onClickCommunity(View image) {
		Intent intent = new Intent(this, CommunityActivity.class);
		startActivity(intent);
	}

	private class SideMenuItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (position){
			case 0:
				break;
			case 1:
				signOut();
				break;
			default:
				break;
			}
		}
	}
	
}

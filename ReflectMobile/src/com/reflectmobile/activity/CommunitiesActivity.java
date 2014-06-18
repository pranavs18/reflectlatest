package com.reflectmobile.activity;

import java.util.ArrayList;

import android.animation.AnimatorSet.Builder;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.reflectmobile.R;
import com.reflectmobile.activity.CommunityActivity.CardViewHolder;
import com.reflectmobile.utility.HorizontalListView;

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
		// return super.onOptionsItemSelected(item);
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_community:
			// Initialze dialog window and set content
			View dialogView = getLayoutInflater().inflate(
					R.layout.dialog_community_filter, null);
			ListView filterListView = (ListView) dialogView
					.findViewById(R.id.listView_community_dialog_filter);

			ArrayList<String> nameList = new ArrayList<String>();
			nameList.add("123");
			nameList.add("234");
			nameList.add("234");
			nameList.add("234");
			nameList.add("234");
			nameList.add("234");
			nameList.add("234");
			nameList.add("234");
			nameList.add("234");
			nameList.add("234");
			nameList.add("234");
			nameList.add("234");
			FilterListViewAdapter adapter = new FilterListViewAdapter(
					CommunitiesActivity.this, nameList);
			
			filterListView.setAdapter(adapter);

			// Custom center title
			TextView title = new TextView(this);
			title.setText(R.string.title_dialog_community_filter);
			title.setPadding(20, 20, 20, 20);
			title.setGravity(Gravity.CENTER);
			title.setTextSize(25);

			new AlertDialog.Builder(CommunitiesActivity.this)
					.setView(dialogView)
					.setPositiveButton("Apply",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).setCustomTitle(title).setCancelable(false)
					.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	static class FilterItemHolder {
		public TextView name;
		public CheckBox checkBox;
	}

	private class FilterListViewAdapter extends BaseAdapter {
		private ArrayList<String> nameList;
		private LayoutInflater inflater;
		private Context context;

		public FilterListViewAdapter(Context context, ArrayList<String> nameList) {
			this.nameList = nameList;
			this.context = context;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return nameList.size();
		}

		@Override
		public Object getItem(int item) {
			// TODO Auto-generated method stub
			return item;
		}

		@Override
		public long getItemId(int id) {
			// TODO Auto-generated method stub
			return id;
		}

		@Override
		public View getView(int position, View view, ViewGroup parentView) {
			// TODO Auto-generated method stub
			if (view == null) {
				view = inflater.inflate(
						R.layout.item_community_filter_dialog_listview, null);
				final FilterItemHolder holder = new FilterItemHolder();
				holder.name = (TextView) view
						.findViewById(R.id.textView_community_filter_listview_item_name);
				holder.checkBox = (CheckBox) view
						.findViewById(R.id.checkbox_community_filter_listview_item);
				view.setTag(holder);
			}

			final FilterItemHolder holder = (FilterItemHolder) view.getTag();
			holder.name.setText(nameList.get(position));
			return view;
		}

	}

}

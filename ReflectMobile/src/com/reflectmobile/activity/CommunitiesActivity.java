package com.reflectmobile.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.data.Community;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class CommunitiesActivity extends BaseActivity {

	private String TAG = "CommunitiesActivity";

	private Community[] communities;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// It is important to set content view before calling super.onCreate
		// because BaseActivity uses references to side menu
		setContentView(R.layout.activity_communities);
		super.onCreate(savedInstanceState);

		// Modify action bar title
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTextColor(getResources().getColor(R.color.yellow));
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

		// Set margin before title
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) title
				.getLayoutParams();
		mlp.setMargins(5, 0, 0, 0);

		// Retreive data from the web
		final HttpTaskHandler getCommunitiesHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of communities
				communities = Community.getCommunitiesInfo(result);
				GridView parentView = (GridView) findViewById(R.id.parentView);
				parentView.setAdapter(new CardAdapter(CommunitiesActivity.this));
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getCommunitiesHandler)
				.execute("http://rewyndr.truefitdemo.com/api/communities");

	}

	@Override
	public void onBackPressed() {
		signOut();
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

	// Specific adapter for Communities Activity
	private class CardAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private Context mContext;
		private Drawable[] mDrawables;

		public CardAdapter(Context context) {
			mDrawables = new Drawable[communities.length];
			mContext = context;
			mInflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for (int count = 0; count < communities.length; count++) {
				final int index = count;

				// Load images asynchronously and notify about their loading
				new HttpGetImageTask(new HttpImageTaskHandler() {
					private int drawableIndex = index;

					@Override
					public void taskSuccessful(Drawable drawable) {
						mDrawables[drawableIndex] = drawable;
						notifyDataSetChanged();
					}

					@Override
					public void taskFailed(String reason) {
						Log.e(TAG, "Error downloading the image");
					}
				}).execute(communities[count].getFirstPhoto());
			}

		}

		@Override
		public int getCount() {
			return communities.length;
		}

		@Override
		public Object getItem(int item) {
			return item;
		}

		@Override
		public long getItemId(int id) {
			return id;
		}

		// Uses common Android ViewHolder pattern
		private class CardViewHolder {
			public View view;
			public ImageView image;
			public TextView text;
			public int position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parentView) {
			// If there is no view to recycle - create a new one
			if (convertView == null) {
				convertView = mInflater
						.inflate(R.layout.card, parentView, false);
				final CardViewHolder holder = new CardViewHolder();
				holder.view = convertView;
				holder.text = (TextView) convertView
						.findViewById(R.id.card_text);
				holder.image = (ImageView) convertView
						.findViewById(R.id.card_image);
				holder.image.setScaleType(ScaleType.CENTER_CROP);
				holder.view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int position = ((CardViewHolder) v.getTag()).position;
						Intent intent = new Intent(mContext,
								CommunityActivity.class);
						intent.putExtra("community_id",
								communities[position].getId());
						mContext.startActivity(intent);
					}
				});
				convertView.setTag(holder);
			}
			
			final CardViewHolder holder = (CardViewHolder) convertView.getTag();
			holder.position = position;
			
			holder.text.setText(communities[position].getName());
			holder.image.setImageDrawable(mDrawables[position]);

			return convertView;
		}
	}

}

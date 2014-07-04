package com.reflectmobile.activity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.data.Community;
import com.reflectmobile.data.Moment;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class CommunityActivity extends BaseActivity {

	private static String TAG = "CommunityActivity";
	private Community community;
	private static int communityId;

	// Static identifier for receiving camera apps call back
	private static final int CODE_ADD_MOMENT = 101;
	private static final int CODE_ADD_PHOTO = 102;
	public static final int MEDIA_TYPE_IMAGE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// It is important to set content view before calling super.onCreate
		// because BaseActivity uses references to side menu
		setContentView(R.layout.activity_community);
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

		communityId = getIntent().getIntExtra("community_id", 0);

		// Retreive data from the web
		final HttpTaskHandler getCommunityHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d(TAG, result);
				// Parse JSON to the list of communities
				community = Community.getCommunityInfo(result);
				setTitle(community.getName());
				// set card listview
				ListView cardListView = (ListView) findViewById(R.id.listview_community_card_list);
				cardListView.setAdapter(new CardListViewAdapter(
						CommunityActivity.this));
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getCommunityHandler)
				.execute("http://rewyndr.truefitdemo.com/api/communities/"
						+ communityId);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.community_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		// If the filter item is selected
		case R.id.action_add_photo:
			addPhoto();
			return true;
		case R.id.action_filter_moments:
			filterView();
			return true;
		case R.id.action_add_moment:
			createMoment();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(CommunityActivity.this,
				CommunitiesActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		super.onBackPressed();
	}

	private void addPhoto() {
		Intent intent = new Intent(CommunityActivity.this,
				GalleryActivity.class);
		startActivityForResult(intent, CODE_ADD_PHOTO);
	}

	public void createMoment() {
		Intent intent = new Intent(CommunityActivity.this,
				AddMomentActivity.class);
		intent.putExtra("community_id", communityId);
		startActivityForResult(intent, CODE_ADD_MOMENT);
	}

	@SuppressLint("InflateParams")
	private void filterView() {
		// Initialze dialog window and set content
		View dialogView = getLayoutInflater().inflate(
				R.layout.dialog_community_filter, null);
		ListView filterListView = (ListView) dialogView
				.findViewById(R.id.listView_community_dialog_filter);

		// Dummy name in the community
		ArrayList<String> nameList = new ArrayList<String>();
		nameList.add("123");
		nameList.add("234");
		nameList.add("234");
		nameList.add("234");
		nameList.add("234");
		nameList.add("234");

		// Generate and bind adapter
		FilterListViewAdapter adapter = new FilterListViewAdapter(
				CommunityActivity.this, nameList);

		filterListView.setAdapter(adapter);

		// Generate the custom center title view
		TextView title = new TextView(this);
		title.setText(R.string.title_dialog_community_filter);
		title.setPadding(20, 20, 20, 20);
		title.setGravity(Gravity.CENTER);
		title.setTextSize(25);

		// Generate the dialog
		new AlertDialog.Builder(CommunityActivity.this)
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
						}).setCustomTitle(title).setCancelable(false).show();
	}

	// Specific adapter for Community Activity
	private class CardListViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context mContext;
		private Drawable[] mDrawables;

		public CardListViewAdapter(Context context) {
			mDrawables = new Drawable[3 * community.getNumOfMoments()];
			mContext = context;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for (int count = 0; count < community.getNumOfMoments(); count++) {
				final int index = count;

				Moment moment = community.getMoment(count);

				for (int photoCount = 0; photoCount < Math.min(
						moment.getNumOfPhotos(), 3); photoCount++) {
					final int photoIndex = photoCount;
					// Load images asynchronously and notify about their loading
					new HttpGetImageTask(new HttpImageTaskHandler() {
						private int drawableIndex = 3 * index + photoIndex;

						@Override
						public void taskSuccessful(Drawable drawable) {
							mDrawables[drawableIndex] = drawable;
							notifyDataSetChanged();
						}

						@Override
						public void taskFailed(String reason) {
							Log.e(TAG, "Error downloading the image");
						}
					}).execute(moment.getPhoto(photoCount)
							.getImageMediumThumbURL());
				}
			}
		}

		@Override
		public int getCount() {
			return community.getNumOfMoments();
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
			public TextView name;
			public TextView date;
			public Button totalPhoto;
			public ImageView[] photos = new ImageView[3];
			public TextView people;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parentView) {
			// If there is no view to recycle - create a new one
			Moment moment = community.getMoment(position);
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.card_community,
						parentView, false);
				final CardViewHolder holder = new CardViewHolder();
				holder.name = (TextView) convertView
						.findViewById(R.id.text_community_card_community_name);
				holder.date = (TextView) convertView
						.findViewById(R.id.text_community_card_date);
				holder.totalPhoto = (Button) convertView
						.findViewById(R.id.button_community_card_total_photo);
				holder.people = (TextView) convertView
						.findViewById(R.id.text_community_card_people_name);
				holder.photos[0] = (ImageView) convertView
						.findViewById(R.id.card_photo_1);
				holder.photos[1] = (ImageView) convertView
						.findViewById(R.id.card_photo_2);
				holder.photos[2] = (ImageView) convertView
						.findViewById(R.id.card_photo_3);

				holder.totalPhoto.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int position = (Integer) v.getTag();
						Intent intent = new Intent(mContext,
								MomentActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_CLEAR_TASK);
						intent.putExtra("moment_id",
								community.getMoment(position).getId());
						mContext.startActivity(intent);
					}
				});
				convertView.setTag(holder);
			}

			final CardViewHolder holder = (CardViewHolder) convertView.getTag();
			holder.totalPhoto.setTag(position);

			holder.name.setText(moment.getName());

			// set moment date
			holder.date.setText(moment.getDate());
			// set moment photo total
			int numOfPhotos = moment.getNumOfPhotos();
			if (numOfPhotos <= 1) {
				holder.totalPhoto.setText(moment.getNumOfPhotos() + " photo");
			} else {
				holder.totalPhoto.setText(moment.getNumOfPhotos() + " photos");
			}
			// set moment people
			holder.people.setText("Ellen Reflect, John Baker and 3 others");
			// set moment photos
			for (int count = 0; count < 3; count++) {
				if (count < numOfPhotos) {
					holder.photos[count].setTag(position);
					holder.photos[count].setImageDrawable(mDrawables[3
							* position + count]);
					holder.photos[count].setScaleType(ScaleType.CENTER_CROP);

					holder.photos[count]
							.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									int position = (Integer) v.getTag();
									Intent intent = new Intent(mContext,
											MomentActivity.class);
									intent.putExtra("community_id", communityId);
									intent.putExtra("moment_id", community
											.getMoment(position).getId());
									mContext.startActivity(intent);
								}
							});
				} else {
					holder.photos[count].setImageDrawable(getResources()
							.getDrawable(R.drawable.add_photo_dark));
					holder.photos[count].setScaleType(ScaleType.CENTER);
				}
			}
			// set moment photo list
			return convertView;
		}

	}

	private class FilterListViewAdapter extends BaseAdapter {
		private ArrayList<String> nameList;
		private LayoutInflater mInflater;
		private Context mContext;

		public FilterListViewAdapter(Context context, ArrayList<String> nameList) {
			this.nameList = nameList;
			this.mContext = context;
			this.mInflater = (LayoutInflater) this.mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return nameList.size();
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
		private class FilterItemHolder {
			public TextView name;
			public CheckBox checkBox;
		}

		@Override
		public View getView(int position, View view, ViewGroup parentView) {
			if (view == null) {
				view = mInflater.inflate(
						R.layout.item_community_filter_dialog_listview,
						parentView, false);
				final FilterItemHolder holder = new FilterItemHolder();
				holder.name = (TextView) view
						.findViewById(R.id.textView_community_filter_listview_item_name);
				holder.checkBox = (CheckBox) view
						.findViewById(R.id.checkbox_community_filter_listview_item);
				holder.checkBox.setChecked(false);
				view.setTag(holder);
			}

			final FilterItemHolder holder = (FilterItemHolder) view.getTag();
			holder.name.setText(nameList.get(position));
			return view;
		}
	}

	// Photo app callback function (define how to handle the photo)
	// Currently only add the photo to the gallery
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_ADD_MOMENT && resultCode == RESULT_OK) {
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}
	}

}

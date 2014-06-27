package com.reflectmobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class CommunityActivity extends BaseActivity {

	private static String TAG = "CommunityActivity";
	private Community community;
	private static int communityId;	

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
		case R.id.action_add_moment:
			createMoment();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static class AddMomentDialog extends DialogFragment {

		private boolean nameSet = false;

		private String name;

		private Button saveButton;

		public AddMomentDialog() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

			final View view = inflater.inflate(R.layout.add_moment, container);
			Button cancelButton = (Button) view.findViewById(R.id.cancel);
			saveButton = (Button) view.findViewById(R.id.save);
			saveButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					saveButton.setEnabled(false);
					HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {
						@Override
						public void taskSuccessful(String result) {
							Log.d("POST", result);
							dismiss();
							getActivity().finish();
							startActivity(getActivity().getIntent());
						}

						@Override
						public void taskFailed(String reason) {
							Log.e("POST", "Error within POST request: "
									+ reason);
						}
					};
					JSONObject momentData = new JSONObject();
					try {
						momentData.put("community_id", communityId);
						momentData.put("name", name);
					} catch (JSONException e) {
						Log.e(TAG, "Error forming JSON");
					}
					String payload = momentData.toString();
					new HttpPostTask(httpPostTaskHandler, payload)
							.execute("http://rewyndr.truefitdemo.com/api/communities/"
									+ communityId + "/moments");
				}
			});
			EditText momentName = (EditText) view
					.findViewById(R.id.moment_name);

			momentName.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					nameSet = count > 0;
					modifySaveButton();
					if (nameSet) {
						name = s.toString();
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});

			return view;
		}

		private void modifySaveButton() {
			if (nameSet) {
				saveButton.setTextColor(getResources().getColor(R.color.green));
				saveButton.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.tick_active, 0, 0, 0);
				saveButton.setEnabled(true);
			} else {
				saveButton.setTextColor(getResources().getColor(
						R.color.dark_gray));
				saveButton.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.tick_disabled, 0, 0, 0);
				saveButton.setEnabled(false);
			}
		}

	}

	public void createMoment() {
		FragmentManager fm = getFragmentManager();
		AddMomentDialog addMomentDialog = new AddMomentDialog();
		addMomentDialog.show(fm, "fragment_add_moment");
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
				convertView = mInflater.inflate(R.layout.card_community, null);
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
						intent.putExtra("moment_id",
								community.getMoment(position).getId());
						mContext.startActivity(intent);
					}
				});
				convertView.setTag(holder);
			}

			final CardViewHolder holder = (CardViewHolder) convertView.getTag();
			holder.totalPhoto.setTag(position);

			// set moment name, when the name is too long, cut it
			if (moment.getName().length() >= 20) {
				String shortcutName = moment.getName().substring(0, 17) + "...";
				holder.name.setText(shortcutName);
			} else {
				holder.name.setText(moment.getName());
			}

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
								@Override
								public void onClick(View v) {
									int position = (Integer) v.getTag();
									Intent intent = new Intent(mContext,
											MomentActivity.class);
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

}

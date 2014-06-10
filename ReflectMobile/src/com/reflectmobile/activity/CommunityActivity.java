package com.reflectmobile.activity;

import java.io.FileInputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.drawable;
import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.google.android.gms.internal.ca;
import com.reflectmobile.R;
import com.reflectmobile.activity.CardAdapter.ViewHolder;
import com.reflectmobile.data.Community;
import com.reflectmobile.data.Moment;
import com.reflectmobile.data.Photo;
import com.reflectmobile.utility.HorizontalListView;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class CommunityActivity extends BaseActivity {
	public static final String TAG = "COMMUNITY_ACTIVITY:";
	Community community;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_community);
		super.onCreate(savedInstanceState);

		String jsonString = getIntent().getStringExtra("community_data");
		
		// generate data object (community -> moment -> photo)
		getCommunityInfo(jsonString);

		// set card listview
		ListView cardListView = (ListView) findViewById(R.id.listview_community_card_list);
		CardListViewAdapter cardListViewAdapter = new CardListViewAdapter(
				CommunityActivity.this);
		cardListView.setAdapter(cardListViewAdapter);

	}

	public void getCommunityInfo(String jsonString) {
		JSONObject truefitCommunityJSONObj;
		try {
			truefitCommunityJSONObj = new JSONObject(jsonString);

			Log.d(TAG + "JSON", truefitCommunityJSONObj.toString());
			// create community obj
			int communityID = truefitCommunityJSONObj.getInt("id");
			String communityName = truefitCommunityJSONObj.getString("name");
			String communityDescription = truefitCommunityJSONObj
					.getString("description");
			community = new Community(communityID, communityName,
					communityDescription, truefitCommunityJSONObj.toString());

			// get moments
			JSONArray truefitMomentJSONArray = new JSONArray(
					truefitCommunityJSONObj.getString("moments"));
			for (int i = 0; i <= truefitMomentJSONArray.length() - 1; i++) {
				JSONObject truefitMomentJSONObj = truefitMomentJSONArray
						.getJSONObject(i);

				// create moment obj
				int momentID = truefitMomentJSONObj.getInt("id");
				String momentName = truefitMomentJSONObj.getString("name");
				Moment moment = new Moment(momentID, momentName, truefitMomentJSONObj.toString());

				// get date
				String date = truefitMomentJSONObj.getString("date");
				if (date.equals("null")) {
					moment.setDate("JULY 3, 2014");
				} else {
					moment.setDate(date);
				}

				// get photos
				JSONArray truefitPhotoJSONArray = new JSONArray(
						truefitMomentJSONObj.getString("photos"));
				for (int j = 0; j <= truefitPhotoJSONArray.length() - 1; j++) {
					JSONObject truefitPhotoJSONObj = truefitPhotoJSONArray
							.getJSONObject(j);
					// create photo obj
					int photoID = truefitPhotoJSONObj.getInt("id");
					String photoName = truefitPhotoJSONObj
							.getString("image_medium_url");
					String photoMediumURL = truefitPhotoJSONObj
							.getString("image_medium_url");
					Photo photo = new Photo(photoID, photoName, photoMediumURL, truefitPhotoJSONObj.toString());
					// add photo to moment
					moment.addPhoto(photo);
				}
				// add moment to community
				community.addMoment(moment);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onClickMoment(View image) {
		Intent intent = new Intent(this, MomentActivity.class);
		startActivity(intent);
	}

	static class CardViewHolder {
		public TextView name;
		public TextView date;
		public Button totalPhoto;
		public TextView people;
		public HorizontalListView photoList;
	}

	private class CardListViewAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private Context context;
		private CardImageHorizontalListViewAdapter[] adapters;

		public CardListViewAdapter(Context context) {
			this.context = context;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.adapters = new CardImageHorizontalListViewAdapter[community
					.getNumOfMoment()];
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return community.getNumOfMoment();
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
		public View getView(int position, View view, ViewGroup parent) {
			// TODO Auto-generated method stub
			View cardView = view;
			Moment moment = community.getMoment(position);
			if (cardView == null) {
				cardView = inflater.inflate(R.layout.card_community, null);
				final CardViewHolder holder = new CardViewHolder();
				holder.name = (TextView) cardView
						.findViewById(R.id.text_community_card_community_name);
				holder.date = (TextView) cardView
						.findViewById(R.id.text_community_card_date);
				holder.totalPhoto = (Button) cardView
						.findViewById(R.id.button_community_card_total_photo);
				holder.people = (TextView) cardView
						.findViewById(R.id.text_community_card_people_name);
				holder.photoList = (HorizontalListView) cardView
						.findViewById(R.id.horizontal_listview_card_image_list);
				cardView.setTag(holder);
			}
			// important, never create adpter each time, extremely slow
			if (adapters[position] == null) {
				// add adapter to the array
				CardImageHorizontalListViewAdapter adapter = new CardImageHorizontalListViewAdapter(
						CommunityActivity.this, moment.getPhotoList());
				adapters[position] = adapter;
			}

			final CardViewHolder holder = (CardViewHolder) cardView.getTag();
			// set moment name, when the name is too long, cut it
			if (moment.getName().length() >= 20){
				String shortcutName = moment.getName().substring(0, 12)+"...";
				holder.name.setText(shortcutName);
			}
			else{
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
			// set moment photo list
			holder.photoList.setAdapter(adapters[position]);
			return cardView;
		}

	}

	// static class ImageViewHolder {
	// public ImageView photo;
	// public int position;
	// }
	/*
	 * Adapter for horizontal listview in the card view
	 */
	private class CardImageHorizontalListViewAdapter extends BaseAdapter {

		private Context context;
		private LayoutInflater inflater;
		private ArrayList<Photo> photoList;
		private Drawable[] drawables;
		private Bitmap[] choppedBitmaps;
		private int momentIndex;

		public CardImageHorizontalListViewAdapter(Context context,
				ArrayList<Photo> photoList) {
			this.context = context;
			this.photoList = photoList;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.drawables = new Drawable[photoList.size()];
			this.choppedBitmaps = new Bitmap[photoList.size()];

			// retrive images from internet
			for (int i = 0; i <= photoList.size() - 1; i++) {
				Photo photo = photoList.get(i);
				final int index = i;
				new HttpGetImageTask(new HttpImageTaskHandler() {
					int photoIndex = index;

					@Override
					public void taskSuccessful(Drawable drawable) {
						drawables[photoIndex] = drawable;
						// center chopped the image
						Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
						choppedBitmaps[photoIndex] = getCenterChoppedBitmap(
								bitmap, 100);
						notifyDataSetChanged();
					}

					@Override
					public void taskFailed(String reason) {
						Log.e(TAG, "Error downloading the image");
					}
				}).execute(photo.getImageMediumURL());
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return photoList.size();
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
		public View getView(int position, View view, ViewGroup parent) {
			// TODO Auto-generated method stub
			ImageView photoView = null;
			if (view == null) {
				view = inflater.inflate(R.layout.photo_community_card, null);
			}

			photoView = (ImageView) view
					.findViewById(R.id.photo_community_card);
			// for test
			if (choppedBitmaps[position] != null) {
				photoView.setImageBitmap(getCenterChoppedBitmap(
						choppedBitmaps[position], 100));
				Log.d(TAG, photoList.get(position).getImageMediumURL());
				Log.d(TAG, photoView.getWidth() + " " + photoView.getHeight());
				Log.d(TAG, choppedBitmaps[position].getWidth() + " "
						+ choppedBitmaps[position].getHeight());
			}
			return photoView;
		}
	}


	public Bitmap getCenterChoppedBitmap(Bitmap bitmap, int newHeightDP) {

		Resources r = getResources();
		float newHeightPX = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, newHeightDP,
				r.getDisplayMetrics());

		int height = bitmap.getHeight();
		int width = bitmap.getWidth();

		float scaleHeight = ((float) newHeightPX) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the bit map
		matrix.postScale(scaleHeight, scaleHeight);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
				height, matrix, false);

		// center chop
		Bitmap choppedBitmap = null;
		if (resizedBitmap.getWidth() >= resizedBitmap.getHeight()) {

			choppedBitmap = Bitmap.createBitmap(
					resizedBitmap,
					resizedBitmap.getWidth() / 2
							- resizedBitmap.getHeight() / 2, 0,
					resizedBitmap.getHeight(), resizedBitmap.getHeight());

		} else {

			choppedBitmap = Bitmap.createBitmap(
					resizedBitmap,
					0,
					resizedBitmap.getHeight() / 2
							- resizedBitmap.getWidth() / 2,
					resizedBitmap.getWidth(), resizedBitmap.getWidth());
		}
		return choppedBitmap;
	}
}

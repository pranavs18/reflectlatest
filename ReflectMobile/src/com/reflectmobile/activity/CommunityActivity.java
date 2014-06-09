package com.reflectmobile.activity;

import java.io.FileInputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.reflectmobile.R;
import com.reflectmobile.data.Community;
import com.reflectmobile.data.Moment;
import com.reflectmobile.data.Photo;
import com.reflectmobile.utility.HorizontalListView;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class CommunityActivity extends BaseActivity {
	public static final String TAG = "COMMUNITY_ACTIVITY:";
	Community community;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_community);
//		getCommunityInfo();
		
		
		//set card listview
		ListView cardListView = (ListView)findViewById(R.id.listview_community_card_list);
		CardListViewAdapter cardListViewAdapter = new CardListViewAdapter(CommunityActivity.this);
		cardListView.setAdapter(cardListViewAdapter);
		
		//set horizontal listview
//		HorizontalListView horizontalListView = (HorizontalListView)findViewById(R.id.horizontal_listview);
//		ArrayList<Integer> imageIDList = new ArrayList<Integer>();
//		imageIDList.add(R.drawable.action_bar_icon_logo);
//		imageIDList.add(R.drawable.photo1);
//		imageIDList.add(R.drawable.photo2);
//		imageIDList.add(R.drawable.action_bar_icon_logo);
//		imageIDList.add(R.drawable.photo1);
//		imageIDList.add(R.drawable.photo2);
//		imageIDList.add(R.drawable.action_bar_icon_logo);
//		imageIDList.add(R.drawable.photo1);
//		imageIDList.add(R.drawable.photo2);
//		imageIDList.add(R.drawable.action_bar_icon_logo);
//		imageIDList.add(R.drawable.photo1);
//		imageIDList.add(R.drawable.photo2);
//		imageIDList.add(R.drawable.action_bar_icon_logo);
//		imageIDList.add(R.drawable.photo1);
//		imageIDList.add(R.drawable.photo2);
//		HorizontalListViewAdapter adapter = new HorizontalListViewAdapter(CommunityActivity.this, imageIDList);
//		horizontalListView.setAdapter(adapter);
	}

	public void getCommunityInfo(){
		final HttpTaskHandler getCommunityTaskHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				// change string to json object
				try {
				    JSONObject truefitCommunityJSONObj = new JSONObject(result);
				    Log.d(TAG + "JSON", truefitCommunityJSONObj.toString());
				    // create community obj
				    int communityID = truefitCommunityJSONObj.getInt("id");
				    String communityName = truefitCommunityJSONObj.getString("name");
				    String communityDescription = truefitCommunityJSONObj.getString("description");
				    community = new Community(communityID, communityName, communityDescription);
				    
				    //get moments
				    JSONArray truefitMomentJSONArray = new JSONArray(truefitCommunityJSONObj.getString("moments"));
				    for (int i = 0; i <= truefitMomentJSONArray.length()-1; i++){
				    	JSONObject truefitMomentJSONObj = truefitMomentJSONArray.getJSONObject(i);
				    	// create moment obj
				    	int momentID = truefitMomentJSONObj.getInt("id");
				    	String momentName = truefitMomentJSONObj.getString("name");
				    	Moment moment = new Moment(momentID, momentName);
				    	
				    	// get photos
				    	JSONArray truefitPhotoJSONArray = new JSONArray(truefitMomentJSONObj.getString("photos"));
				    	for (int j = 0; j <= truefitPhotoJSONArray.length()-1; j++){
				    		JSONObject truefitPhotoJSONObj = truefitPhotoJSONArray.getJSONObject(j);
				    		// create photo obj
				    		int photoID = truefitPhotoJSONObj.getInt("id");
				    		String photoName = truefitPhotoJSONObj.getString("image_medium_url");
				    		String photoMediumURL = truefitPhotoJSONObj.getString("image_medium_url");
				    		Photo photo = new Photo(photoID, photoName, photoMediumURL);
				    		// add photo to moment
				    		moment.addPhoto(photo);
				    	}
				    	// add moment to community
				    	community.addMoment(moment);
				    }
				} catch (Throwable t) {
				    Log.e(TAG + "ERROR", "Could not parse malformed JSON");
				}
			}
			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};
		
		final HttpTaskHandler loginHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d("GET", result);
				new HttpGetTask(getCommunityTaskHandler)
					.execute("http://rewyndr.truefitdemo.com/api/communities/30");
			}
			@Override
			public void taskFailed(String reason) {
				Log.e(TAG + "GET", "Error within GET request: " + reason);
			}
		};

		JSONObject truefitLoginData = new JSONObject();
		JSONObject truefitUserData = new JSONObject();
		try {
			truefitUserData.put("uid", "100003480327087");
			// truefitUserData.put("token", accessToken);
			truefitUserData.put("expires_in", 6340);
			truefitUserData.put("provider", "facebook");
			truefitLoginData.put("user_data", truefitUserData);
		} catch (JSONException e) {
			Log.e(TAG + "ERROR", "Error parsing JSON");
		}
		String payload = truefitLoginData.toString();
		new HttpPostTask(loginHandler, payload)
				.execute("http://rewyndr.truefitdemo.com/api/authentication/login");
		
	}
	public void onClickMoment(View image) {
		Intent intent = new Intent(this, MomentActivity.class);
		startActivity(intent);
	}
	
	
	
	private class CardListViewAdapter extends BaseAdapter{
		private LayoutInflater inflater;
		private Context context;
		
		public CardListViewAdapter(Context context){
			this.context = context;
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 3;
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
			View cellView = view;
			if (cellView == null){
				cellView = inflater.inflate(R.layout.item_community_card, null);
			}
			return cellView;
		}
		
	}
	/*
	 * Adapter for horizontal listview in the card view
	 */
	private class HorizontalListViewAdapter extends BaseAdapter {

		private Context context;
		private LayoutInflater inflater;
		private ArrayList<Integer> imageIDList;
		
		public HorizontalListViewAdapter(Context context, ArrayList<Integer> imageIDList){
			this.context = context;
			this.imageIDList = imageIDList;
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return imageIDList.size();
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
			View cellView = view;
			if (cellView == null){
				cellView = inflater.inflate(R.layout.horizontal_listview_item, null);
			}
			ImageView imageView = (ImageView)cellView.findViewById(R.id.horizontal_listview_item_img);
			imageView.setImageResource(imageIDList.get(position));
			return cellView;
		}
	}
}

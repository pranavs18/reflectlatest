package com.reflectmobile.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;

public class CardAdapter extends BaseAdapter {

	private String TAG = "Card";

	private Context mContext;
	private JSONArray mJSONArray;
	private Drawable[] mDrawables;

	public CardAdapter(Context context, String json) {
		mContext = context;
		try {
			mJSONArray = new JSONArray(json);
			mDrawables = new Drawable[mJSONArray.length()];
			for (int count = 0; count < mJSONArray.length(); count++) {
				final int index = count;
				JSONObject communityData = mJSONArray.getJSONObject(count);
				if (!communityData.isNull("first_photo")) {
					JSONObject firstPhoto = communityData
							.getJSONObject("first_photo");
					String location = firstPhoto
							.getString("image_medium_thumb_url");

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
					}).execute(location);
				}
			}

		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON with communities data");
		}
	}

	@Override
	public int getCount() {
		return mJSONArray.length();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	static class ViewHolder {
		public View view;
		public ImageView image;
		public TextView text;
		public int position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parentView) {
		// If there is no view to recycle - create a new one
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.card, parentView, false);
			final ViewHolder holder = new ViewHolder();
			holder.position = position;
			holder.view = convertView;
			holder.text = (TextView) convertView.findViewById(R.id.card_text);
			holder.image = (ImageView) convertView
					.findViewById(R.id.card_image);
			holder.image.setScaleType(ScaleType.CENTER_CROP);
			holder.view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = ((ViewHolder) v.getTag()).position;
					Log.d(TAG, "Position is " + position);
					try {
						JSONObject communityData = mJSONArray.getJSONObject(position);
						Intent intent = new Intent(mContext, CommunityActivity.class);
						intent.putExtra("community_data", communityData.toString());
						mContext.startActivity(intent);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			convertView.setTag(holder);
		} else {
			final ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.position = position;
		}

		final ViewHolder holder = (ViewHolder) convertView.getTag();

		try {
			JSONObject communityData = mJSONArray.getJSONObject(position);
			holder.text.setText(communityData.getString("name"));
			holder.image.setImageDrawable(mDrawables[position]);
			Log.d(TAG, holder.image.getWidth()+" "+holder.image.getHeight());
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing community JSON");
		}

		return convertView;
	}

}

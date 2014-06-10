package com.reflectmobile.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;

public class ImageAdapter extends BaseAdapter {

	private String TAG = "Image";

	private Context mContext;
	private JSONArray mJSONArray;
	private Drawable[] mDrawables;

	public ImageAdapter(Context context, String json) {

		mContext = context;
		try {
			JSONObject moment = new JSONObject(json);
			mJSONArray = moment.getJSONArray("photos");
			mDrawables = new Drawable[mJSONArray.length()];
			for (int count = 0; count < mJSONArray.length(); count++) {
				final int index = count;
				JSONObject photo = (JSONObject) mJSONArray.get(count);
				String location = photo.getString("image_medium_thumb_url");
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
		public int position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parentView) {
		// If there is no view to recycle - create a new one
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.moment_photo, parentView, false);
			final ViewHolder holder = new ViewHolder();
			holder.position = position;
			holder.view = convertView;
			holder.image = (ImageView) convertView
					.findViewById(R.id.photo);
			holder.image.setScaleType(ScaleType.CENTER_CROP);
			holder.view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = ((ViewHolder) v.getTag()).position;
					Log.d(TAG, "Position is " + position);
/*					try {
						JSONObject communityData = mJSONArray
								.getJSONObject(position);
						Intent intent = new Intent(mContext,
								PhotoActivity.class);
						intent.putExtra("community_data",
								communityData.toString());
						mContext.startActivity(intent);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
				}
			});
			convertView.setTag(holder);
		} else {
			final ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.position = position;
		}

		final ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.image.setImageDrawable(mDrawables[position]);

		return convertView;
	}

}

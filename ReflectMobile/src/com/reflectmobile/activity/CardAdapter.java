package com.reflectmobile.activity;

import java.net.URI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CardAdapter extends BaseAdapter {

	private String TAG = "Card";

	private Context mContext;
	private JSONArray mJSONArray;

	public CardAdapter(Context context, String json) {
		mContext = context;
		try {
			mJSONArray = new JSONArray(json);
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

	@Override
	public View getView(int position, View convertView, ViewGroup parentView) {
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			convertView = (RelativeLayout) View.inflate(mContext,
					R.layout.card, null);
			convertView.setLayoutParams(new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			final TextView textView = (TextView) convertView
					.findViewById(R.id.card_text);

			final ImageView imageView = (ImageView) convertView
					.findViewById(R.id.card_image);
			imageView.setScaleType(ScaleType.CENTER_CROP);

			try {
				JSONObject communityData = mJSONArray.getJSONObject(position);
				textView.setText(communityData.getString("name"));

				if (!communityData.isNull("first_photo")) {
					JSONObject firstPhoto = communityData
							.getJSONObject("first_photo");
					String location = firstPhoto
							.getString("image_medium_thumb_url");

					new HttpGetImageTask(new HttpImageTaskHandler() {
						@Override
						public void taskSuccessful(Drawable drawable) {
							imageView.setImageDrawable(drawable);
						}

						@Override
						public void taskFailed(String reason) {
							Log.e(TAG, "Error downloading the image");
						}
					}).execute(location);
				}

			} catch (JSONException e) {
				Log.e(TAG, "Error parsing community JSON");
			}
		}
		return convertView;
	}

}

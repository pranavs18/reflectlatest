package com.reflectmobile.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;

public class PhotoActivity extends BaseActivity {

	private String TAG = "PhotoActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo);

		String jsonMoment = getIntent().getStringExtra("moment_data");
		jsonMoment = "{ \"community_id\" : 27,\r\n  \"date\" : null,\r\n  \"first_photo\" : { \"created_at\" : \"2014-05-07T06:33:32.108Z\",\r\n      \"id\" : 36,\r\n      \"image_content_type\" : \"image/jpeg\",\r\n      \"image_file_name\" : \"Pizza1.jpg\",\r\n      \"image_file_size\" : 58125,\r\n      \"image_large_url\" : \"/system/photos/images/000/000/036/large/Pizza1.jpg?1399444411\",\r\n      \"image_medium_thumb_url\" : \"/system/photos/images/000/000/036/medium_thumb/Pizza1.jpg?1399444411\",\r\n      \"image_medium_url\" : \"/system/photos/images/000/000/036/medium/Pizza1.jpg?1399444411\",\r\n      \"image_thumb_url\" : \"/system/photos/images/000/000/036/thumb/Pizza1.jpg?1399444411\",\r\n      \"image_updated_at\" : \"2014-05-07T06:33:31.399Z\",\r\n      \"image_url\" : \"/system/photos/images/000/000/036/original/Pizza1.jpg?1399444411\",\r\n      \"moment_id\" : 34,\r\n      \"taken_at\" : \"2014-05-07T06:33:32.106Z\",\r\n      \"updated_at\" : \"2014-05-07T06:33:32.108Z\",\r\n      \"user_id\" : 18\r\n    },\r\n  \"id\" : 34,\r\n  \"name\" : \"Eduardo's Pizza Night\",\r\n  \"owner\" : { \"email\" : \"zmolodchenko@gmail.com\",\r\n      \"first_name\" : \"Zakhar\",\r\n      \"graduating_class\" : 2013,\r\n      \"id\" : 18,\r\n      \"last_name\" : \"Herych\"\r\n    },\r\n  \"photos\" : [ { \"id\" : 36,\r\n        \"image_content_type\" : \"image/jpeg\",\r\n        \"image_file_name\" : \"Pizza1.jpg\",\r\n        \"image_file_size\" : 58125,\r\n        \"image_large_url\" : \"/system/photos/images/000/000/036/large/Pizza1.jpg?1399444411\",\r\n        \"image_medium_thumb_url\" : \"/system/photos/images/000/000/036/medium_thumb/Pizza1.jpg?1399444411\",\r\n        \"image_medium_url\" : \"/system/photos/images/000/000/036/medium/Pizza1.jpg?1399444411\",\r\n        \"image_thumb_url\" : \"/system/photos/images/000/000/036/thumb/Pizza1.jpg?1399444411\",\r\n        \"image_updated_at\" : \"2014-05-07T06:33:31.399Z\",\r\n        \"image_url\" : \"/system/photos/images/000/000/036/original/Pizza1.jpg?1399444411\",\r\n        \"moment_id\" : 34,\r\n        \"taken_at\" : \"2014-05-07T06:33:32.106Z\",\r\n        \"user_id\" : 18\r\n      },\r\n      { \"id\" : 37,\r\n        \"image_content_type\" : \"image/jpeg\",\r\n        \"image_file_name\" : \"Pizza2.jpg\",\r\n        \"image_file_size\" : 72921,\r\n        \"image_large_url\" : \"/system/photos/images/000/000/037/large/Pizza2.jpg?1399444463\",\r\n        \"image_medium_thumb_url\" : \"/system/photos/images/000/000/037/medium_thumb/Pizza2.jpg?1399444463\",\r\n        \"image_medium_url\" : \"/system/photos/images/000/000/037/medium/Pizza2.jpg?1399444463\",\r\n        \"image_thumb_url\" : \"/system/photos/images/000/000/037/thumb/Pizza2.jpg?1399444463\",\r\n        \"image_updated_at\" : \"2014-05-07T06:34:23.196Z\",\r\n        \"image_url\" : \"/system/photos/images/000/000/037/original/Pizza2.jpg?1399444463\",\r\n        \"moment_id\" : 34,\r\n        \"taken_at\" : \"2014-05-07T06:34:24.076Z\",\r\n        \"user_id\" : 18\r\n      },\r\n      { \"id\" : 38,\r\n        \"image_content_type\" : \"image/jpeg\",\r\n        \"image_file_name\" : \"Pizza3.jpg\",\r\n        \"image_file_size\" : 69062,\r\n        \"image_large_url\" : \"/system/photos/images/000/000/038/large/Pizza3.jpg?1399444495\",\r\n        \"image_medium_thumb_url\" : \"/system/photos/images/000/000/038/medium_thumb/Pizza3.jpg?1399444495\",\r\n        \"image_medium_url\" : \"/system/photos/images/000/000/038/medium/Pizza3.jpg?1399444495\",\r\n        \"image_thumb_url\" : \"/system/photos/images/000/000/038/thumb/Pizza3.jpg?1399444495\",\r\n        \"image_updated_at\" : \"2014-05-07T06:34:55.857Z\",\r\n        \"image_url\" : \"/system/photos/images/000/000/038/original/Pizza3.jpg?1399444495\",\r\n        \"moment_id\" : 34,\r\n        \"taken_at\" : \"2014-05-07T06:34:56.707Z\",\r\n        \"user_id\" : 18\r\n      }\r\n    ]\r\n}";
		final int photoIndex = getIntent().getIntExtra("photo_index", 0);
		JSONObject moment;
		try {
			moment = new JSONObject(jsonMoment);
			JSONArray photos = moment.getJSONArray("photos");
			JSONObject photo = (JSONObject) photos.get(photoIndex);
			String location = photo.getString("image_large_url");

			final ImageView imageView = (ImageView) findViewById(R.id.photo);

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

			imageView.setOnTouchListener(new OnSwipeTouchListener(this) {
				
				public void onSwipeRight() {
					Toast.makeText(PhotoActivity.this, "right",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(PhotoActivity.this, PhotoActivity.class);
					intent.putExtra("photo_index", photoIndex-1);
					startActivity(intent);
				}

				public void onSwipeLeft() {

					Toast.makeText(PhotoActivity.this, "left",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(PhotoActivity.this, PhotoActivity.class);
					intent.putExtra("photo_index", photoIndex+1);
					startActivity(intent);
				}

				public boolean onTouch(View v, MotionEvent event) {
					return gestureDetector.onTouchEvent(event);
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public class OnSwipeTouchListener implements OnTouchListener {

		protected final GestureDetector gestureDetector;

		public OnSwipeTouchListener(Context ctx) {
			gestureDetector = new GestureDetector(ctx, new GestureListener());
		}

		private final class GestureListener extends SimpleOnGestureListener {

			private static final int SWIPE_THRESHOLD = 100;
			private static final int SWIPE_VELOCITY_THRESHOLD = 100;

			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				boolean result = false;
				try {
					float diffY = e2.getY() - e1.getY();
					float diffX = e2.getX() - e1.getX();
					if (Math.abs(diffX) > Math.abs(diffY)) {
						if (Math.abs(diffX) > SWIPE_THRESHOLD
								&& Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
							if (diffX > 0) {
								onSwipeRight();
							} else {
								onSwipeLeft();
							}
						}
					} else {
						if (Math.abs(diffY) > SWIPE_THRESHOLD
								&& Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
							if (diffY > 0) {
								onSwipeBottom();
							} else {
								onSwipeTop();
							}
						}
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				return result;
			}
		}

		public void onSwipeRight() {
		}

		public void onSwipeLeft() {
		}

		public void onSwipeTop() {
		}

		public void onSwipeBottom() {
		}

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			return false;
		}
	}

}

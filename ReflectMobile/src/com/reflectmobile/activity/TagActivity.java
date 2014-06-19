package com.reflectmobile.activity;

import org.json.JSONArray;
import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.reflectmobile.R;
import com.reflectmobile.data.Photo;
import com.reflectmobile.data.Tag;
import com.reflectmobile.utility.TagImageView;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class TagActivity extends BaseActivity {

	private String TAG = "TagActivity";
	Photo photo;
	TagImageView photoTagImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// It is important to set content view before calling super.onCreate
		// because BaseActivity uses references to side menu
		setContentView(R.layout.activity_tag);
		super.onCreate(savedInstanceState);

		// Set photo view
		photoTagImageView = (TagImageView) findViewById(R.id.tagimageview_tag_image);

		// Retrieve photo from the web
		int photoId = getIntent().getIntExtra("photo_id", 0);
		final HttpTaskHandler getPhotoHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d(TAG, result);
				// Parse JSON to a specific photo
				photo = Photo.getPhotoInfo(result);

				// Continue download image
				new HttpGetImageTask(new HttpImageTaskHandler() {
					@Override
					public void taskSuccessful(Drawable drawable) {
						// Continue download tag
						final Drawable image = drawable;
						new HttpGetTask(new HttpTaskHandler() {

							@Override
							public void taskSuccessful(String result) {
								Log.d(TAG, result);
								JSONArray tagJSONArray;
								try {
									tagJSONArray = new JSONArray(result);
									for (int j = 0; j <= tagJSONArray.length() - 1; j++) {
										Tag tag = Tag.getTagInfo(tagJSONArray
												.getString(j));
										photo.addTag(tag);
									}
									// Draw tags
									photoTagImageView
											.setImageBitmap(drawTags(((BitmapDrawable) image)
													.getBitmap()));
								} catch (JSONException e) {
									Log.e(TAG, "Error parse the tag json");
								}
							}

							@Override
							public void taskFailed(String reason) {
								Log.e(TAG, "Error downloading the tag");
							}
						}).execute("http://rewyndr.truefitdemo.com/api/photos/"
								+ photo.getId() + "/tags");
					}

					@Override
					public void taskFailed(String reason) {
						Log.e(TAG, "Error downloading the image");
					}
				}).execute(photo.getImageLargeURL());

			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getPhotoHandler)
				.execute("http://rewyndr.truefitdemo.com/api/photos/" + photoId);
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.moment_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public Bitmap drawTags(Bitmap bitmap) {
		// Create buffer new bitmap
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);

		// Generate brush
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);

		// Draw tags based on tag coordinate
		for (Tag tag : photo.getTagList()) {
			RectF rect = new RectF(tag.getUpLeftX(), tag.getUpLeftY(),
					tag.getUpLeftX() + tag.getBoxWidth(), tag.getUpLeftY()
							+ tag.getBoxLength());
			canvas.drawRoundRect(rect, 2, 2, paint);
		}
		return newBitmap;
	}
}

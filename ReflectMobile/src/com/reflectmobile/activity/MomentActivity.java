package com.reflectmobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;

import com.reflectmobile.R;

public class MomentActivity extends BaseActivity {
	
	private String TAG = "MomentActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_moment);
		super.onCreate(savedInstanceState);
		
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTextColor(getResources().getColor(R.color.yellow));
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

		String jsonString = getIntent().getStringExtra("moment_data");
		jsonString = "{ \"community_id\" : 27,\r\n  \"date\" : null,\r\n  \"first_photo\" : { \"created_at\" : \"2014-05-07T06:33:32.108Z\",\r\n      \"id\" : 36,\r\n      \"image_content_type\" : \"image/jpeg\",\r\n      \"image_file_name\" : \"Pizza1.jpg\",\r\n      \"image_file_size\" : 58125,\r\n      \"image_large_url\" : \"/system/photos/images/000/000/036/large/Pizza1.jpg?1399444411\",\r\n      \"image_medium_thumb_url\" : \"/system/photos/images/000/000/036/medium_thumb/Pizza1.jpg?1399444411\",\r\n      \"image_medium_url\" : \"/system/photos/images/000/000/036/medium/Pizza1.jpg?1399444411\",\r\n      \"image_thumb_url\" : \"/system/photos/images/000/000/036/thumb/Pizza1.jpg?1399444411\",\r\n      \"image_updated_at\" : \"2014-05-07T06:33:31.399Z\",\r\n      \"image_url\" : \"/system/photos/images/000/000/036/original/Pizza1.jpg?1399444411\",\r\n      \"moment_id\" : 34,\r\n      \"taken_at\" : \"2014-05-07T06:33:32.106Z\",\r\n      \"updated_at\" : \"2014-05-07T06:33:32.108Z\",\r\n      \"user_id\" : 18\r\n    },\r\n  \"id\" : 34,\r\n  \"name\" : \"Eduardo's Pizza Night\",\r\n  \"owner\" : { \"email\" : \"zmolodchenko@gmail.com\",\r\n      \"first_name\" : \"Zakhar\",\r\n      \"graduating_class\" : 2013,\r\n      \"id\" : 18,\r\n      \"last_name\" : \"Herych\"\r\n    },\r\n  \"photos\" : [ { \"id\" : 36,\r\n        \"image_content_type\" : \"image/jpeg\",\r\n        \"image_file_name\" : \"Pizza1.jpg\",\r\n        \"image_file_size\" : 58125,\r\n        \"image_large_url\" : \"/system/photos/images/000/000/036/large/Pizza1.jpg?1399444411\",\r\n        \"image_medium_thumb_url\" : \"/system/photos/images/000/000/036/medium_thumb/Pizza1.jpg?1399444411\",\r\n        \"image_medium_url\" : \"/system/photos/images/000/000/036/medium/Pizza1.jpg?1399444411\",\r\n        \"image_thumb_url\" : \"/system/photos/images/000/000/036/thumb/Pizza1.jpg?1399444411\",\r\n        \"image_updated_at\" : \"2014-05-07T06:33:31.399Z\",\r\n        \"image_url\" : \"/system/photos/images/000/000/036/original/Pizza1.jpg?1399444411\",\r\n        \"moment_id\" : 34,\r\n        \"taken_at\" : \"2014-05-07T06:33:32.106Z\",\r\n        \"user_id\" : 18\r\n      },\r\n      { \"id\" : 37,\r\n        \"image_content_type\" : \"image/jpeg\",\r\n        \"image_file_name\" : \"Pizza2.jpg\",\r\n        \"image_file_size\" : 72921,\r\n        \"image_large_url\" : \"/system/photos/images/000/000/037/large/Pizza2.jpg?1399444463\",\r\n        \"image_medium_thumb_url\" : \"/system/photos/images/000/000/037/medium_thumb/Pizza2.jpg?1399444463\",\r\n        \"image_medium_url\" : \"/system/photos/images/000/000/037/medium/Pizza2.jpg?1399444463\",\r\n        \"image_thumb_url\" : \"/system/photos/images/000/000/037/thumb/Pizza2.jpg?1399444463\",\r\n        \"image_updated_at\" : \"2014-05-07T06:34:23.196Z\",\r\n        \"image_url\" : \"/system/photos/images/000/000/037/original/Pizza2.jpg?1399444463\",\r\n        \"moment_id\" : 34,\r\n        \"taken_at\" : \"2014-05-07T06:34:24.076Z\",\r\n        \"user_id\" : 18\r\n      },\r\n      { \"id\" : 38,\r\n        \"image_content_type\" : \"image/jpeg\",\r\n        \"image_file_name\" : \"Pizza3.jpg\",\r\n        \"image_file_size\" : 69062,\r\n        \"image_large_url\" : \"/system/photos/images/000/000/038/large/Pizza3.jpg?1399444495\",\r\n        \"image_medium_thumb_url\" : \"/system/photos/images/000/000/038/medium_thumb/Pizza3.jpg?1399444495\",\r\n        \"image_medium_url\" : \"/system/photos/images/000/000/038/medium/Pizza3.jpg?1399444495\",\r\n        \"image_thumb_url\" : \"/system/photos/images/000/000/038/thumb/Pizza3.jpg?1399444495\",\r\n        \"image_updated_at\" : \"2014-05-07T06:34:55.857Z\",\r\n        \"image_url\" : \"/system/photos/images/000/000/038/original/Pizza3.jpg?1399444495\",\r\n        \"moment_id\" : 34,\r\n        \"taken_at\" : \"2014-05-07T06:34:56.707Z\",\r\n        \"user_id\" : 18\r\n      }\r\n    ]\r\n}";
		try {
			JSONObject data = new JSONObject(jsonString);
			String momentTitle = data.getString("name");
			title.setText(momentTitle);
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON");
			e.printStackTrace();
		}
		GridView parentView = (GridView) findViewById(R.id.parentView);
		parentView.setAdapter(new ImageAdapter(this, jsonString));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.moment_menu, menu);
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

}

package com.reflectmobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class AddDetailActivity extends BaseActivity {

	private String TAG = "AddDetailActivity";

	private int photoId;
	private boolean addButtonPressed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.add_detail);
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
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		ImageView view = (ImageView) findViewById(android.R.id.home);
		view.setPadding(10, 0, 0, 0);

		addButtonPressed = false;
		photoId = getIntent().getIntExtra("photo_id", 0);

		Spinner spinner = (Spinner) findViewById(R.id.spinner_emotions);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.detail_items,
				R.layout.spinner);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_detail_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_detail:
			if (!addButtonPressed){
				addButtonPressed = true;
				addDetail();
			}
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void addDetail() {
		Log.d(TAG, "PhotoId " + photoId);
		HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d("POST", result);
				finish();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e("POST", "Error within POST request: " + reason);
			}
		};
		JSONObject storyData = new JSONObject();
		EditText taggedEditText = (EditText) findViewById(R.id.tagged);
		String taggedText = taggedEditText.getText().toString();

		Spinner spinner = (Spinner) findViewById(R.id.spinner_emotions);
		String spinnerText = spinner.getSelectedItem().toString();

		EditText detailEditText = (EditText) findViewById(R.id.detail_text);
		String detailText = detailEditText.getText().toString();

		String memoryText = taggedText + " " + spinnerText + " " + detailText;
		try {
			storyData.put("photo_id", photoId);
			storyData.put("memory_type", "detail");
			storyData.put("memory_content", memoryText);
		} catch (JSONException e) {
			Log.e(TAG, "Error forming JSON");
		}
		String payload = storyData.toString();
		new HttpPostTask(httpPostTaskHandler, payload)
				.execute("http://rewyndr.truefitdemo.com/api/memories");
	}
}

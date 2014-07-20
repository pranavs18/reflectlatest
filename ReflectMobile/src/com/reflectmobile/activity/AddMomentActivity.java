package com.reflectmobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class AddMomentActivity extends BaseActivity {

	private String TAG = "AddMomentActivity";

	private boolean nameSet = false;

	private int communityId;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.add_moment);
		super.onCreate(savedInstanceState);

		// Modify action bar title
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

		// Set margin before title
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) title
				.getLayoutParams();
		mlp.setMargins(5, 0, 0, 0);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		ImageView view = (ImageView) findViewById(android.R.id.home);
		view.setPadding(10, 0, 0, 0);

		communityId = getIntent().getIntExtra("community_id", 0);

		EditText momentName = (EditText) findViewById(R.id.moment_name);

		momentName.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				nameSet = s.length() > 0;
				if (nameSet) {
					name = s.toString();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_community_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_community:
			if (nameSet) {
				nameSet = false;
				addMoment();
			} else {
				int red = android.R.color.holo_red_light;
				Style CustomAlert = new Style.Builder().setDuration(2000)
						.setHeight(LayoutParams.WRAP_CONTENT).setTextSize(16)
						.setBackgroundColor(red).setPaddingInPixels(26).build();
				Crouton.makeText(this, "Please, specify all fields",
						CustomAlert).show();
			}
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void addMoment() {
		HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d("POST", result);
				setResult(RESULT_OK);
				finish();
			}

			@Override
			public void taskFailed(String reason) {
				Log.e("POST", "Error within POST request: " + reason);
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
				.execute(NetworkManager.hostName+"/api/communities/"
						+ communityId + "/moments");
	}
}

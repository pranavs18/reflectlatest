package com.reflectmobile.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpPostSoundTask;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpPutTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class AddSoundActivity extends BaseActivity {

	private String TAG = "AddSoundActivity";
	private int photoId;

	private String soundName;
	private String soundUrl;

	private boolean soundNameSet;

	private static String mFileName = null;
	private Menu menu;
	private LinearLayout mTitle = null;
	private TextView mTimer = null;
	private ImageButton mRecordButton = null;
	private TextView mInstruction = null;
	private MediaRecorder mRecorder = null;
	private boolean isRecording = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.add_sound);
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

		photoId = getIntent().getIntExtra("photo_id", 0);

		EditText soundNameText = (EditText) findViewById(R.id.sound_name);
		if (getIntent().hasExtra("sound_name")) {
			soundName = getIntent().getStringExtra("soundName");
			soundNameText.setText(soundName);
			soundNameSet = true;
		}

		soundNameText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				soundNameSet = s.length() > 0;
				if (soundNameSet) {
					soundName = s.toString();
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

		
		try {
			createSoundFile();
		} catch (IOException ex) {
			Log.d(TAG, "Can't create sound file");
		}

		mTitle = (LinearLayout) findViewById(R.id.title);
		mTimer = (TextView) findViewById(R.id.timer);
		mInstruction = (TextView) findViewById(R.id.instruction);
		
		mRecordButton = (ImageButton) findViewById(R.id.record);
		mRecordButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isRecording) {
					mTitle.setVisibility(View.INVISIBLE);
					mRecordButton.setImageResource(R.drawable.recorder_pause);
					mInstruction.setText("Press to stop");
					MenuItem add_sound = menu.findItem(R.id.action_add_sound);
					add_sound.setVisible(false);
					startRecording();
				}
				else{
					mTitle.setVisibility(View.VISIBLE);
					mRecordButton.setImageResource(R.drawable.recorder_record);
					mInstruction.setText("Press to start");
					MenuItem add_sound = menu.findItem(R.id.action_add_sound);
					add_sound.setVisible(true);
					stopRecording();
					mRecordButton.setEnabled(false);
				} 
				isRecording = !isRecording;
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_sound_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_sound:
			if (soundNameSet) {
				soundNameSet = false;
				addSound();
			} else {
				int red = android.R.color.holo_red_light;
				Style CustomAlert = new Style.Builder().setDuration(2000)
						.setHeight(LayoutParams.WRAP_CONTENT).setTextSize(16)
						.setBackgroundColor(red).setPaddingInPixels(26).build();
				Crouton.makeText(this, "Please, add title to the sound",
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

	private File createSoundFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		String imageFileName = "GP3_" + timeStamp + ".3gp";
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = new File(storageDir, imageFileName);
		mFileName = image.getAbsolutePath();
		return image;
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(TAG, "Player 'prepare' failed");
		}

		mRecorder.start();
	}
	
	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	public void addSound() {
		/*
		final HttpTaskHandler postSoundHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				try {
					JSONObject soundData = new JSONObject(result);
					int soundId = soundData.getInt("id");
					Log.d("sound id", soundId+"");

				} catch (JSONException e) {
					Log.e(TAG, "Error parsing JSON");
				}
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within POST SOUND request: " + reason);
			}
		};

		final HttpTaskHandler postMemoryHandler = new HttpTaskHandler() {
			
			@Override
			public void taskSuccessful(String result) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void taskFailed(String reason) {
				Log.d(TAG, "Error posting memory");
			}
		};
		
		new HttpPostSoundTask(postSoundHandler, NetworkManager.SOUND_HOST_NAME
				+ "/sounds/" + sound_id,
				AddSoundActivity.this).execute(soundUrl);

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

		try {
			storyData.put("photo_id", photoId);
			storyData.put("memory_type", "sound");
			storyData.put("memory_content", soundName);
			storyData.put("memory_location", soundUrl);
		} catch (JSONException e) {
			Log.e(TAG, "Error forming JSON");
		}
		String payload = storyData.toString();

		if (getIntent().hasExtra("memory_id")) {
			int memoryId = getIntent().getIntExtra("memory_id", 0);
			new HttpPutTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/memories/"
							+ memoryId);
		} else {
			new HttpPostTask(httpPostTaskHandler, payload)
					.execute(NetworkManager.hostName + "/api/memories");
		}*/
	}
}

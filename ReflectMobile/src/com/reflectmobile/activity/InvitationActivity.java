package com.reflectmobile.activity;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.reflectmobile.R;
import com.reflectmobiledemo.data.Community;
import com.reflectmobiledemo.utility.NetworkManager;
import com.reflectmobiledemo.utility.NetworkManager.HttpGetTask;
import com.reflectmobiledemo.utility.NetworkManager.HttpTaskHandler;


public class InvitationActivity extends BaseActivity {
	
	private Community community;
	private static int communityId;
	private static int photoId;
	private static String TAG = "InvitationActivity";
	private String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
	//@SuppressWarnings("deprecation")

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		hasNavigationDrawer = false;
		setContentView(R.layout.activity_invite);
		super.onCreate(savedInstanceState);

		// Modify action bar title
		int titleId = getResources().getIdentifier("action_bar_title", "id",
				"android");
		TextView title = (TextView) findViewById(titleId);
		title.setTextColor(getResources().getColor(R.color.yellow));
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));
		Button invite = (Button) findViewById(R.id.invite_button); 
		Button cancel = (Button) findViewById(R.id.cancel_email);
	    final EditText contentBox = (EditText) findViewById(R.id.content_email);
	 
		cancel.setOnClickListener(new OnClickListener()
		{ 
			public void onClick(View v){
				finish();
			}
				
		
		 });
		
		contentBox.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus == true){
					contentBox.setText("");
				}
				
			}
		});
		
		invite.setOnClickListener(new OnClickListener()
		   {
		            public void onClick(View v)
		             {
		               
		             EditText emailBox = (EditText) findViewById(R.id.email_invite);
		             String emailID = emailBox.getText().toString();
		            
		             
		             
		             
		             String emailContent = contentBox.getText().toString();
		             if(emailID.matches(EMAIL_REGEX)) {
		            	 
		            	 communityId = getIntent().getIntExtra("community_id", 0);
		            	 photoId = getIntent().getIntExtra("photo_id", -1);
		            	 
		         		// Retreive data from the web
		         		final HttpTaskHandler getCommunityHandler = new HttpTaskHandler() {
		         			@Override
		         			public void taskSuccessful(String result) {
		         				Log.d(TAG, result);
		         				// Parse JSON to the list of communities
		         				community = Community.getCommunityInfo(result);
		         				Log.d(TAG,"Comunity invited to " + community.toString());
		         					
		         			}

		         			@Override
		         			public void taskFailed(String reason) {
		         				Log.e(TAG, "Error within GET request: " + reason);
		         			}
		         		};

		         		new HttpGetTask(getCommunityHandler).execute(NetworkManager.hostName
		         				+ "/api/communities/" + communityId);
		         		// Retreive data from the web
		         		final HttpTaskHandler getInviteHandler = new HttpTaskHandler() {
		         			@Override
		         			public void taskSuccessful(String result) {
		         				Log.d(TAG, result);

		         			}

		         			@Override
		         			public void taskFailed(String reason) {
		         				Log.e(TAG, "Error within GET request: " + reason);

		         			}

		         		};
		            	 
		             JSONObject invitationData = new JSONObject();
		       		 JSONObject invitationPayload = new JSONObject();

		       		 try {
		       			invitationData.put("email", emailID);
		       			invitationData.put("message", emailContent);
		       			invitationData.put("community_id", communityId);
		       			if(photoId != -1){
		       				invitationData.put("photo_id",photoId);
		       				Log.d("Invitation from Photo", photoId +"");
		       			}
		       			invitationPayload.put("invitation", invitationData.toString());
		       		} catch (JSONException e) {
		       		    Log.d(TAG,"Error sending invitation data");
		       			e.printStackTrace();
		       		}
		                String payload = invitationData.toString();
		                Log.d(TAG, payload);
		       		 new NetworkManager.HttpPostTask(getInviteHandler, payload).execute(NetworkManager.hostName + "/api/invitations");		
		       		 Log.d("Invitation", NetworkManager.hostName + "/api/invites/link/" + communityId);
		       		 Toast.makeText(InvitationActivity.this, "Invitation sent successfully" ,
		    				Toast.LENGTH_LONG).show();
		            	
		             	}
		             else{
		            	 Toast.makeText(InvitationActivity.this, "Invalid email ID, Please enter again !" ,
				    				Toast.LENGTH_LONG).show();
		             	}
		             }
				
		   }); 
		   
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.donation_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
			// If the filter item is selected
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(InvitationActivity.this,
				CommunitiesActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		super.onBackPressed();
	}
	
	
}
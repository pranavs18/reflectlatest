package com.reflectmobile.activity;

import org.json.JSONObject;

import android.R.anim;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.reflectmobile.R;

public class LoginActivity extends BaseActivity {
    private static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";
    
    private Button facebookLogInButton;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		facebookLogInButton = (Button)findViewById(R.id.button_log_in_facebook);
		
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		
        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }
        else session.closeAndClearTokenInformation();
        
        // set facebook listener
    	facebookLogInButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) { onClickLogin(); }
        });
	}

//	public void onClickLogInFacebook(View button) {
//		Intent intent = new Intent(this, NetworkActivity.class);
//		startActivity(intent);
//	}

	public void onClickLogInGoogle(View button) {
		Intent intent = new Intent(this, NetworkActivity.class);
		startActivity(intent);
	}

    
    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }
    
    private void onClickLogin() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(this, true, statusCallback);
        }
    }
    
    private void onClickLogout() {
        Session session = Session.getActiveSession();
        if (!session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }
    
    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(final Session session, SessionState state, Exception exception) {
            String accessToken = session.getAccessToken();
            // error handle
            if(session.getState() == SessionState.CLOSED_LOGIN_FAILED){
//            	Toast.makeText(LoginActivity.this, R.string.error_internet_connection, Toast.LENGTH_LONG).show();
            	new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, android.R.style.Theme_Holo_Light_Dialog))
            		.setTitle(R.string.error_title)
            		.setMessage(R.string.error_internet_connection)
            		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
						}
					})
            		.show();			
            }
            if(accessToken.length() > 0){
            	// get user info
            	Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
					
					@Override
					public void onCompleted(GraphUser user, Response response) {
						// TODO Auto-generated method stub
						JSONObject userData = user.getInnerJSONObject();
						Toast.makeText(LoginActivity.this, userData.toString(), Toast.LENGTH_LONG).show();
					}
				});
            	Request.executeBatchAsync(request);
            }
            Log.w("access_token", accessToken);
        }
    }
}

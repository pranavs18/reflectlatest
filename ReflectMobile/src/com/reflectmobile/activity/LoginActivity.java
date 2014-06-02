package com.reflectmobile.activity;

import org.json.JSONException;
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

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.reflectmobile.R;

public class LoginActivity extends BaseActivity implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks, com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener {
    private static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";

    private Button facebookLogInButton;
    private Button googleLogInButton;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();

    /*Google +API login specific variables */
    private boolean mSignInClicked;
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;
    private ConnectionResult mConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        facebookLogInButton = (Button)findViewById(R.id.button_log_in_facebook);
        googleLogInButton= (Button)findViewById(R.id.button_log_in_google);

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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(Plus.API, new Plus.PlusOptions.Builder().build())
        .addScope(Plus.SCOPE_PLUS_LOGIN)
        .build();

        googleLogInButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) { mGoogleApiClient.connect(); }
        });
    }

//	public void onClickLogInFacebook(View button) {
//		Intent intent = new Intent(this, NetworkActivity.class);
//		startActivity(intent);
//	}
    public void onClickLogInFacebook(View button) {
        Intent intent = new Intent(this, CommunitiesActivity.class);
        startActivity(intent);
    }

    /* Google+ API login code */
    public void onClickLogInGoogle(View button) {
        Intent intent = new Intent(this, CommunitiesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
        //mGoogleApiClient.connect();

    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting() && resultCode != RESULT_CANCELED) {
                mGoogleApiClient.connect();
            }
        }
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
            final String accessToken = session.getAccessToken();
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

                        //start interact with truefit backend
                        HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {

                            @Override
                            public void taskSuccessful(String result) {
                                Log.d("POST", result);
                            }

                            @Override
                            public void taskFailed(String reason) {
                                Log.e("POST", "Error within POST request: " + reason);
                            }
                        };

                        //create json object for truefit
                        //possible bugs
                        JSONObject truefitLoginData = new JSONObject();
                        JSONObject truefitUserData = new JSONObject();
                        try {
                            truefitUserData.put("uid", userData.get("id"));
                            truefitUserData.put("token", accessToken);
                            truefitUserData.put("expires_in", 6340);
                            truefitUserData.put("provider", "facebook");

                            truefitLoginData.put("user_data", truefitUserData);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        String payload = truefitLoginData.toString();
                        new HttpPostTask(httpPostTaskHandler, payload)
                        .execute("http://rewyndr.truefitdemo.com/api/authentication/login");

                    }
                });
                Request.executeBatchAsync(request);
            }
            Log.w("access_token", accessToken);
        }
    }

    /*Google+ API login code */
    public void onGoogleSignInClick(View view) {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO Auto-generated method stub
        if (!mIntentInProgress && result.hasResolution()) {
            try {

                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // We've resolved any connection errors.  mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
        onClickLogInGoogle(null);
    }

    @Override
    public void onDisconnected() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }



}

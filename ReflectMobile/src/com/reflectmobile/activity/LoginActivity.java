package com.reflectmobile.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.reflectmobile.R;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import android.content.IntentSender.SendIntentException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import android.widget.Toast;

public class LoginActivity extends BaseActivity
		implements
		ConnectionCallbacks,
		OnConnectionFailedListener,
		com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks,
		com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener {
	private static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";
	private static final String TAG = "LoginActivity";

	private Session.StatusCallback facebookSessionCallback = new SessionStatusCallback();

	/* Google +API login specific variables */
	private boolean mSignInClicked;
	/* Request code used to invoke sign in user interactions. */
	private static final int RC_SIGN_IN = 0;

	/* Client used to interact with Google APIs. */
	private GoogleApiClient mGoogleApiClient;

	/*
	 * A flag indicating that a PendingIntent is in progress and prevents us
	 * from starting further intents.
	 */
	private boolean mIntentInProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Facebook initialize settings
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		// Initialize Facebook session
		Session facebookSession = Session.getActiveSession();
		if (facebookSession == null) {
			if (savedInstanceState != null) {
				facebookSession = Session.restoreSession(this, null,
						facebookSessionCallback, savedInstanceState);
			}
			if (facebookSession == null) {
				facebookSession = new Session(this);
			}
			Session.setActiveSession(facebookSession);
			if (facebookSession.getState().equals(
					SessionState.CREATED_TOKEN_LOADED)) {
				facebookSession.openForRead(new Session.OpenRequest(this)
						.setCallback(facebookSessionCallback));
			}
		} else {
			facebookSession.closeAndClearTokenInformation();
		}

		// Initialize Google API client
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API, new Plus.PlusOptions.Builder().build())
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}

	public void onClickLogInFacebook(View button) {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
					.setCallback(facebookSessionCallback));
		} else {
			Session.openActiveSession(this, true, facebookSessionCallback);
		}
	}

	public void onClickLogInGoogle(View button) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(facebookSessionCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(facebookSessionCallback);
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
		if (requestCode == RC_SIGN_IN) {
			if (resultCode != RESULT_OK) {
				mSignInClicked = false;
			}

			mIntentInProgress = false;

			if (!mGoogleApiClient.isConnecting()
					&& resultCode != RESULT_CANCELED) {
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session facebookSession = Session.getActiveSession();
		Session.saveSession(facebookSession, outState);
	}

	private void onClickLogout() {
		Session facebookSession = Session.getActiveSession();
		if (!facebookSession.isClosed()) {
			facebookSession.closeAndClearTokenInformation();
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(final Session session, SessionState state,
				Exception exception) {
			final String accessToken = session.getAccessToken();
			// error handle
			if (session.getState() == SessionState.CLOSED_LOGIN_FAILED) {
				// Toast.makeText(LoginActivity.this,
				// R.string.error_internet_connection,
				// Toast.LENGTH_LONG).show();
				new AlertDialog.Builder(new ContextThemeWrapper(
						LoginActivity.this,
						android.R.style.Theme_Holo_Light_Dialog))
						.setTitle(R.string.error_title)
						.setMessage(R.string.error_internet_connection)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								}).show();
			}
			if (accessToken.length() > 0) {
				// get user info
				Request request = Request.newMeRequest(session,
						new Request.GraphUserCallback() {

							@Override
							public void onCompleted(GraphUser user,
									Response response) {
								JSONObject userData = user.getInnerJSONObject();

								// start interact with truefit backend
								HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {

									@Override
									public void taskSuccessful(String result) {
										Log.d("POST", result);
									}

									@Override
									public void taskFailed(String reason) {
										Log.e("POST",
												"Error within POST request: "
														+ reason);
									}
								};

								// create json object for truefit
								// possible bugs
								JSONObject truefitLoginData = new JSONObject();
								JSONObject truefitUserData = new JSONObject();
								try {
									truefitUserData.put("uid",
											userData.get("id"));
									truefitUserData.put("token", accessToken);
									truefitUserData.put("expires_in", 6340);
									truefitUserData.put("provider", "facebook");
									truefitLoginData.put("user_data",
											truefitUserData);
								} catch (JSONException e) {
									Log.e(TAG, "Error parsing JSON");
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

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!mIntentInProgress && result.hasResolution()) {
			try {

				mIntentInProgress = true;
				startIntentSenderForResult(result.getResolution()
						.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				// The intent was canceled before it was sent. Return to the
				// default state and attempt to connect to get an updated
				// ConnectionResult.
				mIntentInProgress = false;
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// We've resolved any connection errors. mGoogleApiClient can be used to
		// access Google APIs on behalf of the user.
		mSignInClicked = false;
		Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
		
		String userId = null;
		
		try {
			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                userId = currentPerson.getId();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
		} catch (Exception e) {
			Log.e(TAG, "Error getting Google profile information");
		}

		final HttpTaskHandler getCommunitiesHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d("GET", result);
				Intent intent = new Intent(LoginActivity.this, CommunitiesActivity.class);
				intent.putExtra("communities_data", result);
				startActivity(intent);
			}
			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};
		
		final HttpTaskHandler loginReflectWebHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				Log.d("POST", result);
				new HttpGetTask(getCommunitiesHandler)
					.execute("http://rewyndr.truefitdemo.com/api/communities");
			}
			@Override
			public void taskFailed(String reason) {
				Log.e("POST", "Error within POST request: " + reason);
			}
		};

		// create json object for truefit
		// possible bugs
		JSONObject truefitLoginData = new JSONObject();
		JSONObject truefitUserData = new JSONObject();
		try {
			truefitUserData.put("uid", userId);
			// truefitUserData.put("token", accessToken);
			truefitUserData.put("expires_in", 6340);
			truefitUserData.put("provider", "google");
			truefitLoginData.put("user_data", truefitUserData);
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON");
		}
		String payload = truefitLoginData.toString();
		new HttpPostTask(loginReflectWebHandler, payload)
				.execute("http://rewyndr.truefitdemo.com/api/authentication/login");
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

}

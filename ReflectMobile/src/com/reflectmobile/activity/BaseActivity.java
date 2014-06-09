package com.reflectmobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.Session;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

public abstract class BaseActivity extends Activity {

	private static String TAG = "BaseActivity";

	public static final int SIGNED_OUT = 1001;
	public static final int SIGNED_IN_GOOGLE = 1002;
	public static final int SIGNED_IN_FACEBOOK = 1003;

	private static int signInStatus;

	// Client used to interact with Google APIs.
	protected static GoogleApiClient mGoogleApiClient;

	private static void googleSignOut() {
		if (mGoogleApiClient.isConnected()) {
			Log.d(TAG, "Clear default account");
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
		}
	}

	private static void facebookSignOut() {
		Session facebookSession = Session.getActiveSession();
		if (!facebookSession.isClosed()) {
			facebookSession.closeAndClearTokenInformation();
		}
	}

	protected static void setSignInStatus(int newStatus) {
		signInStatus = newStatus;
	}

	protected void signOut() {
		Log.d(TAG, "Signin status " + signInStatus);
		switch (signInStatus) {
		case SIGNED_IN_GOOGLE:
			googleSignOut();
			break;
		case SIGNED_IN_FACEBOOK:
			facebookSignOut();
			break;
		default:
			break;
		}
		signInStatus = SIGNED_OUT;
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}
}

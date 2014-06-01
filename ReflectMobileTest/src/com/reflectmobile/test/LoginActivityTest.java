package com.reflectmobile.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.reflectmobile.activity.LoginActivity;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpPostTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

import com.reflectmobile.R;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import android.widget.Button;

public class LoginActivityTest extends
		ActivityInstrumentationTestCase2<LoginActivity> {

	private LoginActivity loginActivity;
	private Button button;

	public LoginActivityTest() {
		super(LoginActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		loginActivity = getActivity();
		button = (Button) loginActivity
				.findViewById(R.id.button_log_in_facebook);
	}

	public void testPreconditions() {
		assertNotNull("Login Activity is null", loginActivity);
		assertNotNull("Button is null", button);
	}

	final CountDownLatch signalPost = new CountDownLatch(1);
	final CountDownLatch signalGet = new CountDownLatch(1);
	
	public void testRequests() throws Throwable {
		runTestOnUiThread(new Runnable() { 
            @Override 
            public void run() { 
        		HttpTaskHandler httpPostTaskHandler = new HttpTaskHandler() {

        			@Override
        			public void taskSuccessful(String result) {
        				Log.d("POST", result);
        				signalPost.countDown();
        			}

        			@Override
        			public void taskFailed(String reason) {
        				Log.e("POST", "Error within POST request: " + reason);
        				signalPost.countDown();
        			}
        		};

        		String payload = "{\"user_data\":{\"uid\":\"100001908160407\",\"token\":\"CAAJVI83Cz98BANKUwdVQZCEjfB6fwBHsfCHCbBcKskM49aap8sQKZBehhdLTZAld8XRTBdd3ZBaEL6ixDixiAlj7tU4dUUBckMRkyKpPRqINh6Sd9s1XCZAlAZAwE1PfnzLdVKGNLiBjx8A0xVUGJczAZAKMxfZBxlDZCRbyAGy28gf80IqfHVMbIEZBnFTb1YkZBIHvZBBQe5tIRwZDZD\",\"expires_in\":5994,\"first_name\":\"Zakhar\",\"last_name\":\"Herych\",\"email\":\"zmolodchenko@gmail.com\",\"provider\":\"facebook\"},\"_utf8\":\"\u2603\"}";

        		new HttpPostTask(httpPostTaskHandler, payload)
        				.execute("http://rewyndr.truefitdemo.com/api/authentication/login");            } 
        });
		
		signalPost.await(30, TimeUnit.SECONDS);

		runTestOnUiThread(new Runnable() { 
            @Override 
            public void run() { 
        		HttpTaskHandler httpGetTaskHandler = new HttpTaskHandler() {

        			@Override
        			public void taskSuccessful(String result) {
        				Log.d("GET", result);
        				signalGet.countDown();
        			}

        			@Override
        			public void taskFailed(String reason) {
        				Log.e("GET", "Error within GET request: " + reason);
        				signalGet.countDown();
        			}
        		};

        		new HttpGetTask(httpGetTaskHandler)
        				.execute("http://rewyndr.truefitdemo.com/api/communities");
            }
        });

		signalGet.await(30, TimeUnit.SECONDS);
	}
}

package com.reflectmobile.activity;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.reflectmobile.R;
import com.reflectmobile.data.Moment;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;

public class PhotoActivity extends BaseActivity {

	private String TAG = "PhotoActivity";
	private Moment moment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_photo);
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

		int momentId = getIntent().getIntExtra("moment_id", 0);
		getIntent().getIntExtra("photo_id", 0);

		// Retreive data from the web
		final HttpTaskHandler getMomentHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of communities
				moment = Moment.getMomentInfo(result);
				setTitle(moment.getName());
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getMomentHandler)
				.execute("http://rewyndr.truefitdemo.com/api/moments/"
						+ momentId);

		// Retreive data from the web
		final HttpTaskHandler getPhotoHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of communities
				// getPhotoInfo(result);
				ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
				ImagePagerAdapter adapter = new ImagePagerAdapter(
						PhotoActivity.this);
				viewPager.setAdapter(adapter);
				viewPager.setCurrentItem(0);
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getPhotoHandler)
				.execute("http://rewyndr.truefitdemo.com/api/communities");
	}

	public class OnSwipeTouchListener implements OnTouchListener {

		protected final GestureDetector gestureDetector;

		public OnSwipeTouchListener(Context ctx) {
			gestureDetector = new GestureDetector(ctx, new GestureListener());
		}

		private final class GestureListener extends SimpleOnGestureListener {

			private static final int SWIPE_THRESHOLD = 100;
			private static final int SWIPE_VELOCITY_THRESHOLD = 100;

			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				boolean result = false;
				try {
					float diffY = e2.getY() - e1.getY();
					float diffX = e2.getX() - e1.getX();
					if (Math.abs(diffX) > Math.abs(diffY)) {
						if (Math.abs(diffX) > SWIPE_THRESHOLD
								&& Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
							if (diffX > 0) {
								onSwipeRight();
							} else {
								onSwipeLeft();
							}
						}
					} else {
						if (Math.abs(diffY) > SWIPE_THRESHOLD
								&& Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
							if (diffY > 0) {
								onSwipeBottom();
							} else {
								onSwipeTop();
							}
						}
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				return result;
			}
		}

		public void onSwipeRight() {
		}

		public void onSwipeLeft() {
		}

		public void onSwipeTop() {
		}

		public void onSwipeBottom() {
		}

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			return false;
		}
	}

	public class ImagePagerAdapter extends PagerAdapter {

		// private Context mContext;
		private Drawable[] mDrawables;

		public ImagePagerAdapter(Context context) {
			// mContext = context;
			mDrawables = new Drawable[moment.getNumOfPhotos()];
			for (int count = 0; count < moment.getNumOfPhotos(); count++) {
				final int index = count;

				new HttpGetImageTask(new HttpImageTaskHandler() {
					private int drawableIndex = index;

					@Override
					public void taskSuccessful(Drawable drawable) {
						mDrawables[drawableIndex] = drawable;
						notifyDataSetChanged();
					}

					@Override
					public void taskFailed(String reason) {
						Log.e(TAG, "Error downloading the image");
					}
				}).execute(moment.getPhoto(index).getImageMediumURL());
			}
		}

		@Override
		public int getCount() {
			// return mImages.length;
			return moment.getNumOfPhotos();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Context context = PhotoActivity.this;
			ImageView imageView = new ImageView(context);
			// int padding =
			// context.getResources().getDimensionPixelSize(R.dimen.padding_medium);
			imageView.setScaleType(ImageView.ScaleType.FIT_START);
			imageView.setImageDrawable(mDrawables[position]);
			((ViewPager) container).addView(imageView, 0);

			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}
	}

}

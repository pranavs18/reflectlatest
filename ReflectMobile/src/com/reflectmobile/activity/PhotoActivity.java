package com.reflectmobile.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;


import com.reflectmobile.R;
import com.reflectmobile.data.Memory;
import com.reflectmobile.data.Moment;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;


import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;


public class PhotoActivity extends BaseActivity {

	private String TAG = "PhotoActivity";
	private Moment moment;
	private Memory[] mMemories;
	private LayoutInflater mInflater;
	
	/* photo gallery variables */
	private static final int SELECT_PICTURE = 1;

	private String selectedImagePath;
	private ImageView img;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
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

		final int momentId = getIntent().getIntExtra("moment_id", 0);
		final int photoId = getIntent().getIntExtra("photo_id", 0);

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		img = (ImageView) findViewById(R.id.imageView1);

		// Retreive data from the web
		final HttpTaskHandler getMomentHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of communities
				moment = Moment.getMomentInfo(result);
				setTitle(moment.getName());

				int index = 0;
				for (int count = 0; count < moment.getNumOfPhotos(); count++) {
					if (moment.getPhoto(count).getId() == photoId) {
						index = count;
						break;
					}
				}
				ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
				ImagePagerAdapter adapter = new ImagePagerAdapter(
						PhotoActivity.this);
				viewPager.setAdapter(adapter);
				viewPager.setOnPageChangeListener(new OnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						loadMemories(position);
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
					}

					@Override
					public void onPageScrollStateChanged(int arg0) {
					}
				});
				viewPager.setCurrentItem(index);
				if (index == 0) {
					// Special case for the first item (page is not changed)
					loadMemories(0);
				}

			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getMomentHandler)
				.execute("http://rewyndr.truefitdemo.com/api/moments/"
						+ momentId);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.photo_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_photo:
			/*
			 * Intent intent = new Intent(); intent.setType("image/*");
			 * intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
			 * intent.setAction(Intent.ACTION_GET_CONTENT);
			 * startActivityForResult
			 * (Intent.createChooser(intent,"Select Picture"), SELECT_PICTURE);
			 */
			Intent intent = new Intent(PhotoActivity.this, MultiPhotoSelectActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				selectedImagePath = getPath(selectedImageUri);
				System.out.println("Image Path : " + selectedImagePath);
				img.setImageURI(selectedImageUri);
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		CursorLoader cursor = new CursorLoader(getApplication(), uri,
				projection, null, null, null);
		int column_index = ((Cursor) cursor)
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		((Cursor) cursor).moveToFirst();
		return ((Cursor) cursor).getString(column_index);
	}

	public void loadMemories(int position) {
		final ViewGroup memoryContainer = (ViewGroup) findViewById(R.id.memories_container);
		memoryContainer.removeAllViews();
		final TextView memoryCaption = (TextView) findViewById(R.id.memories_caption);
		memoryCaption.setText("0 MEMORIES");

		final HttpTaskHandler getMemoriesHandler = new HttpTaskHandler() {
			@Override
			public void taskSuccessful(String result) {
				// Parse JSON to the list of memories
				mMemories = Memory.getMemoriesInfo(result);
				if (mMemories.length == 1) {
					memoryCaption.setText(mMemories.length + " MEMORY");
				} else {
					memoryCaption.setText(mMemories.length + " MEMORIES");
				}
				for (int count = 0; count < mMemories.length; count++) {
					View card = mInflater.inflate(R.layout.card_memory,
							memoryContainer, false);
					ImageView memoryIcon = (ImageView) card
							.findViewById(R.id.memory_icon);
					TextView memoryText = (TextView) card
							.findViewById(R.id.memory_text);
					TextView memoryInfo = (TextView) card
							.findViewById(R.id.memory_info);
					Memory memory = mMemories[count];
					memoryIcon.setImageResource(memory.getResourceId());
					memoryText.setText(memory.getContent());
					memoryInfo.setText(memory.getInfo());
					memoryContainer.addView(card);
				}
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getMemoriesHandler)
				.execute("http://rewyndr.truefitdemo.com/api/memories?photo_id="
						+ moment.getPhoto(position).getId());

	}

	public class ImagePagerAdapter extends PagerAdapter {

		// private Context mContext;
		private Drawable[] mDrawables;

		public ImagePagerAdapter(Context context) {
			// mContext = context;
			mDrawables = new Drawable[moment.getNumOfPhotos()];
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
		public Object instantiateItem(final ViewGroup container, int position) {
			Log.d(TAG, position + "");
			ImageView imageView = new ImageView(PhotoActivity.this);
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setTag(position);
			((ViewPager) container).addView(imageView);

			HttpImageTaskHandler[] httpImageTaskHandlers = new HttpImageTaskHandler[3];
			for (int count = 0; count < 3; count++) {
				final int index = position - 1 + count;

				httpImageTaskHandlers[count] = new HttpImageTaskHandler() {
					private int drawableIndex = index;

					@Override
					public void taskSuccessful(Drawable drawable) {
						mDrawables[drawableIndex] = drawable;
						ImageView imageView = (ImageView) ((ViewPager) container)
								.findViewWithTag(drawableIndex);
						if (imageView != null) {
							imageView.setImageDrawable(drawable);
						}
					}

					@Override
					public void taskFailed(String reason) {
						Log.e(TAG, "Error downloading the image");
					}
				};
			}
			if (mDrawables[position] == null) {
				new HttpGetImageTask(httpImageTaskHandlers[1]).execute(moment
						.getPhoto(position).getImageMediumURL());
			} else {
				imageView.setImageDrawable(mDrawables[position]);
			}
			if (position > 0 && mDrawables[position - 1] == null) {
				new HttpGetImageTask(httpImageTaskHandlers[0]).execute(moment
						.getPhoto(position - 1).getImageMediumURL());
			}
			if (position < moment.getNumOfPhotos() - 1
					&& mDrawables[position + 1] == null) {
				new HttpGetImageTask(httpImageTaskHandlers[2]).execute(moment
						.getPhoto(position + 1).getImageMediumURL());
			}
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}
	}

}

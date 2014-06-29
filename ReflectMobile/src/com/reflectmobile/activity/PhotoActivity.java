package com.reflectmobile.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.internal.cu;
import com.google.android.gms.internal.ev;
import com.reflectmobile.R;
import com.reflectmobile.data.Memory;
import com.reflectmobile.data.Moment;
import com.reflectmobile.data.Photo;
import com.reflectmobile.data.Tag;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;
import com.reflectmobile.widget.ImageProcessor;

public class PhotoActivity extends BaseActivity {

	private String TAG = "PhotoActivity";
	private Moment moment;
	private Memory[] mMemories;
	private LayoutInflater mInflater;

	// TODO
	private int currentPhotoIndex = 1;
	private ImageView currentImageView = null;
	private static int photoImageViewHeightDP = 258;
	private static int photoImageViewWidthDP = 360;
	private static int photoImageViewHeightPX = 0;
	private static int photoImageViewWidthPX = 0;
	private float photoOffsetX = 0;
	private float photoOffsetY = 0;
	private float photoScaleFactor = 1;
	private boolean isExpandHorizontal = false;

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
						// TODO
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

		// TODO
		handleTagButton();
		// Transfer image view size from dp to px
		photoImageViewHeightPX = dpToPx(photoImageViewHeightDP);
		photoImageViewWidthPX = dpToPx(photoImageViewWidthDP);
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
			// TODO
			final int finalPosition = position;
			ImageView imageView = new ImageView(PhotoActivity.this);
			// TODO
			if (position == currentPhotoIndex) {
				currentImageView = imageView;
			}

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
						moment.getPhoto(index).setLargeBitmap(
								((BitmapDrawable) drawable).getBitmap());
					}

					@Override
					public void taskFailed(String reason) {
						Log.e(TAG, "Error downloading the image");
					}
				};
			}
			if (mDrawables[position] == null) {
				new HttpGetImageTask(httpImageTaskHandlers[1]).execute(moment
						.getPhoto(position).getImageLargeURL());
			} else {
				imageView.setImageDrawable(mDrawables[position]);
			}
			if (position > 0 && mDrawables[position - 1] == null) {
				new HttpGetImageTask(httpImageTaskHandlers[0]).execute(moment
						.getPhoto(position - 1).getImageLargeURL());
			}
			if (position < moment.getNumOfPhotos() - 1
					&& mDrawables[position + 1] == null) {
				new HttpGetImageTask(httpImageTaskHandlers[2]).execute(moment
						.getPhoto(position + 1).getImageLargeURL());
			}
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}
	}

	// Add ontouch listener for the tag button
	public void handleTagButton() {
		// Calculate photo offset of this image view

		// Add ontouch listener
		ImageButton tagButton = (ImageButton) findViewById(R.id.button_photo_tag);
		tagButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO
				// Currently hardcode
				setPhotoOffset();

				// Down the tag for this photo from rewyndr
				new HttpGetTask(new HttpTaskHandler() {

					@Override
					public void taskSuccessful(String result) {
						Log.d(TAG, result);
						JSONArray tagJSONArray;
						try {
							tagJSONArray = new JSONArray(result);
							for (int j = 0; j <= tagJSONArray.length() - 1; j++) {
								Tag tag = Tag.getTagInfo(tagJSONArray
										.getString(j));
								moment.getPhoto(currentPhotoIndex).addTag(tag);
							}
							Photo currentPhoto = moment
									.getPhoto(currentPhotoIndex);
							// Draw tags for the photo
							Bitmap taggedBitmap = ImageProcessor
									.generateTaggedBitmap(
											currentPhoto.getLargeBitmap(),
											currentPhoto.getTagList());
							// Cache the tagged photo bitmap
							currentPhoto.setTaggedLargeBitmap(taggedBitmap);
							currentImageView.setImageBitmap(taggedBitmap);
						} catch (JSONException e) {
							Log.e(TAG, "Error parse the tag json");
						}
					}

					@Override
					public void taskFailed(String reason) {
						Log.e(TAG, "Error downloading the tag");
					}
				}).execute("http://rewyndr.truefitdemo.com/api/photos/"
						+ moment.getPhoto(currentPhotoIndex).getId() + "/tags");

				// Add on touch listener for the current image view on screen
				currentImageView.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						Log.d(TAG, event.getX() + " " + event.getY());
						// Transfer the coordinate from the image view to the
						// photo bitmap
						// Location on the enlarged photo
						float bitmapX = event.getX() + photoOffsetX;
						float bitmapY = event.getY() + photoOffsetY;
						// Location to original photo
						if (isExpandHorizontal) {
							bitmapX = bitmapX / photoScaleFactor;
						} else {
							bitmapY = bitmapY / photoScaleFactor;
						}
						Photo currentPhoto = moment.getPhoto(currentPhotoIndex);
//						Bitmap newBitmap = ImageProcessor
//								.generateHighlightedTaggedBitmap(
//										currentPhoto.getLargeBitmap(),
//										currentPhoto.getTaggedLargeBitmap(),
//										currentPhoto.getDarkenTaggedLargeBitmap(),
//										currentPhoto.getTagList(), bitmapX,
//										bitmapY);
						Bitmap newBitmap = ImageProcessor.drawEditSquare(currentPhoto.getLargeBitmap(), currentPhoto.getDarkenLargeBitmap(), 20, 20, (int)bitmapX, (int)bitmapY);
						currentImageView.setImageBitmap(newBitmap);
						return true;
					}
				});

			}
		});
	}

	
	// Change dp to px
	private int dpToPx(int dp) {
		DisplayMetrics displayMetrics = PhotoActivity.this.getResources()
				.getDisplayMetrics();
		int px = Math.round(dp * displayMetrics.density);
		return px;
	}

	// This function calculates the offset of the photo in the image view
	// This shoud be called when the current image view change
	// Currently hardcode to tagbutton onclick listener
	private void setPhotoOffset() {
		Photo currentPhoto = moment.getPhoto(currentPhotoIndex);
		isExpandHorizontal = ((float) currentPhoto.getLargeBitmap().getHeight() / currentPhoto
				.getLargeBitmap().getWidth()) > ((float) photoImageViewHeightPX / photoImageViewWidthPX);
		// If the photo in image view is expanded horizontally
		if (isExpandHorizontal) {
			photoScaleFactor = (float) photoImageViewHeightPX
					/ currentPhoto.getLargeBitmap().getHeight();
			float offsetX = ((photoImageViewWidthPX - photoScaleFactor
					* currentPhoto.getLargeBitmap().getWidth()) / 2);
			photoOffsetX = -offsetX;
			photoOffsetY = 0;
		} else {
			photoScaleFactor = (float) photoImageViewWidthPX
					/ currentPhoto.getLargeBitmap().getWidth();
			float offsetY = ((photoImageViewHeightPX - photoScaleFactor
					* currentPhoto.getLargeBitmap().getHeight()) / 2);
			photoOffsetX = 0;
			photoOffsetY = -offsetY;
		}
	}
}

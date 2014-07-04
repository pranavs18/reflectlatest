package com.reflectmobile.activity;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class PhotoActivity extends BaseActivity {

	private String TAG = "PhotoActivity";
	private Moment moment;
	private Memory[] mMemories;
	private LayoutInflater mInflater;

	/* photo gallery variables */
	static final int CODE_ADD_STORY = 101;
	static final int CODE_ADD_DETAIL = 102;
	static final int CODE_SELECT_PICTURE = 103;

	/* photo gallery variables */
	private String selectedImagePath;
	private ImageView img;

	// TODO
	// Calculate when the activity start
	private static int photoImageViewHeightDP = 258;
	private static int photoImageViewWidthDP = 360;
	private static int photoImageViewHeightPX = 0;
	private static int photoImageViewWidthPX = 0;
	// Set when the image changes
	private int currentPhotoIndex = 2;
	private ImageView currentImageView = null;
	private float photoOffsetX = 0;
	private float photoOffsetY = 0;
	private float photoScaleFactor = 1;
	private boolean isExpandHorizontal = false;
	// Set when user want to edit the tag
	// If it is in add new tag mode, it should be set the the center of the
	// image
	// If it is in edit tag mode, it should be set to the tag location
	private RectF currentEdittedTagLocation = new RectF(0, 0, 0, 0);

	private int photoId = 0;

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

		getActionBar().setDisplayHomeAsUpEnabled(true);
		ImageView view = (ImageView) findViewById(android.R.id.home);
		view.setPadding(10, 0, 0, 0);

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
					public void onPageSelected(final int position) {
						onPhotoSelected(position);
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
					onPhotoSelected(0);
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
		handleTagButtonView();
		// Transfer image view size from dp to px
		photoImageViewHeightPX = dpToPx(photoImageViewHeightDP);
		photoImageViewWidthPX = dpToPx(photoImageViewWidthDP);
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(PhotoActivity.this,
				MomentActivity.class);
		intent.putExtra("moment_id",
				getIntent().getIntExtra("moment_id", 0));
		intent.putExtra("community_id",
				getIntent().getIntExtra("community_id", 0));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		super.onBackPressed();
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
			Intent intent = new Intent(PhotoActivity.this,
					GalleryActivity.class);
			startActivity(intent);
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == CODE_SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				selectedImagePath = getPath(selectedImageUri);
				System.out.println("Image Path : " + selectedImagePath);
				img.setImageURI(selectedImageUri);
			}
		}
		finish();
		Intent intent = this.getIntent();
		intent.putExtra("photo_id", photoId);
		startActivity(intent);
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

	public void onPhotoSelected(final int position) {
		photoId = moment.getPhoto(position).getId();

		final ViewGroup memoryContainer = (ViewGroup) findViewById(R.id.memories_container);
		memoryContainer.removeAllViews();
		final TextView memoryCaption = (TextView) findViewById(R.id.memories_caption);
		memoryCaption.setText("0 MEMORIES");

		ImageButton addStoryButton = (ImageButton) findViewById(R.id.add_story);
		addStoryButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(PhotoActivity.this,
						AddStoryActivity.class);
				intent.putExtra("photo_id", moment.getPhoto(position).getId());
				startActivityForResult(intent, CODE_ADD_STORY);
			}
		});

		ImageButton addDetailButton = (ImageButton) findViewById(R.id.add_detail);
		addDetailButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(PhotoActivity.this,
						AddDetailActivity.class);
				intent.putExtra("photo_id", moment.getPhoto(position).getId());
				startActivityForResult(intent, CODE_ADD_DETAIL);
			}
		});

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
		public Object instantiateItem(final ViewGroup container,
				final int position) {
			Log.d(TAG, position + "");
			ImageView imageView = new ImageView(PhotoActivity.this);
			// TODO
			if (position == currentPhotoIndex) {
				currentImageView = imageView;
			}

			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setTag(position);
			((ViewPager) container).addView(imageView);

			HttpImageTaskHandler httpImageTaskHandler = new HttpImageTaskHandler() {
				private int drawableIndex = position;

				@Override
				public void taskSuccessful(Drawable drawable) {
					mDrawables[drawableIndex] = drawable;
					ImageView imageView = (ImageView) ((ViewPager) container)
							.findViewWithTag(drawableIndex);
					if (imageView != null) {
						imageView.setImageDrawable(drawable);
					}
					// TODO
					// Cache the large bitmap
					moment.getPhoto(position).setLargeBitmap(
							((BitmapDrawable) drawable).getBitmap());

					// Continue download tag information
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
									moment.getPhoto(position).addTag(tag);
								}
								Photo currentPhoto = moment.getPhoto(position);
								// Draw tags for the photo
								Bitmap taggedBitmap = ImageProcessor
										.generateTaggedBitmap(
												currentPhoto.getLargeBitmap(),
												currentPhoto.getTagList());
								// Cache the tagged photo bitmap
								currentPhoto.setTaggedLargeBitmap(taggedBitmap);
							} catch (JSONException e) {
								Log.e(TAG, "Error parse the tag json");
							}
						}

						@Override
						public void taskFailed(String reason) {
							Log.e(TAG, "Error downloading the tag");
						}
					}).execute("http://rewyndr.truefitdemo.com/api/photos/"
							+ moment.getPhoto(currentPhotoIndex).getId()
							+ "/tags");
				}

				@Override
				public void taskFailed(String reason) {
					Log.e(TAG, "Error downloading the tags");
				}
			};

			if (mDrawables[position] == null) {
				new HttpGetImageTask(httpImageTaskHandler).execute(moment
						.getPhoto(position).getImageLargeURL());
			} else {
				imageView.setImageDrawable(mDrawables[position]);
			}
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}
	}

	// Add ontouch listener for the tag button
	public void handleTagButtonEdition() {
		// Add ontouch listener
		ImageButton tagButton = (ImageButton) findViewById(R.id.button_photo_tag);
		tagButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO
				// Currently hardcode
				setPhotoOffset();
				// Currently hardcode dafault tag location
				currentEdittedTagLocation.set(100, 100, 200, 200);
				final Photo currentPhoto = moment.getPhoto(currentPhotoIndex);

				// Add on touch listener for the current image view on screen
				currentImageView.setOnTouchListener(new OnTouchListener() {
					float prevBitmapX = 0;
					float prevBitmapY = 0;
					int movingNode = 8;

					public boolean onTouch(View v, MotionEvent event) {
						// Transfer the coordinate from the image view to the
						// photo bitmap
						// Location on the enlarged photo
						float bitmapX = event.getX() + photoOffsetX;
						float bitmapY = event.getY() + photoOffsetY;
						// Location to original photo
						bitmapX = bitmapX / photoScaleFactor;
						bitmapY = bitmapY / photoScaleFactor;

						switch (event.getAction()) {
						case MotionEvent.ACTION_MOVE:
							Log.d(TAG, "MovingNode:" + movingNode);
							// During move, keep updating the edit square
							// Change tag location based on touch location
							changeTagLocation(prevBitmapX, prevBitmapY,
									bitmapX, bitmapY, movingNode);
							// Draw new edit tag square
							Bitmap newBitmap = ImageProcessor.drawEditSquare(
									currentPhoto.getLargeBitmap(),
									currentPhoto.getDarkenLargeBitmap(),
									currentEdittedTagLocation, true);
							currentImageView.setImageBitmap(newBitmap);
							// Save touch location
							prevBitmapX = bitmapX;
							prevBitmapY = bitmapY;
							break;
						case MotionEvent.ACTION_DOWN:
							movingNode = determineMovingMode(bitmapX, bitmapY);
							break;
						case MotionEvent.ACTION_UP:
							// Clear last touch location
							prevBitmapX = -1;
							prevBitmapY = -1;
							break;
						default:
							break;
						}
						return true;
					}
				});

			}
		});
	}

	public void handleTagButtonView() {
		// Add ontouch listener
		ImageButton tagButton = (ImageButton) findViewById(R.id.button_photo_tag);
		tagButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO
				// Currently hardcode
				setPhotoOffset();
				// Set the current image view as the tagged photo
				final Photo currentPhoto = moment.getPhoto(currentPhotoIndex);
				currentImageView.setImageBitmap(currentPhoto
						.getTaggedLargeBitmap());
				// Add on touch listener for the current image view on screen
				currentImageView.setOnTouchListener(new OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						// Transfer the coordinate from the image view to the
						// photo bitmap
						// Location on the enlarged photo
						float bitmapX = event.getX() + photoOffsetX;
						float bitmapY = event.getY() + photoOffsetY;
						// Location to original photo
						bitmapX = bitmapX / photoScaleFactor;
						bitmapY = bitmapY / photoScaleFactor;
						Log.d(TAG, "Bitmap" + bitmapX + " " + bitmapY);
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							// Generate the highlighted tag bitmap
							Bitmap newBitmap = ImageProcessor.generateHighlightedTaggedBitmap(
									currentPhoto.getLargeBitmap(),
									currentPhoto.getTaggedLargeBitmap(),
									currentPhoto.getDarkenTaggedLargeBitmap(),
									currentPhoto.getTagList(), bitmapX, bitmapY);
							// Set to the current image view
							currentImageView.setImageBitmap(newBitmap);
							break;
						default:
							break;
						}
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
	// This should be called when the current image view change
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

	// This function test whether user wants to move the tag or resize the tag
	// based
	// on touch location
	// All location is location on the bitmap, not on the enlarged bitmap
	private int determineMovingMode(float curX, float curY) {
		// Currently decide the radius is 30, maybe changed
		int squareRadius = 80;
		// Calculate whether the touch location is in the edit square region
		float left = currentEdittedTagLocation.left;
		float right = currentEdittedTagLocation.right;
		float top = currentEdittedTagLocation.top;
		float bottom = currentEdittedTagLocation.bottom;
		float[] editSquareXs = { left, (left + right) / 2, right, left, right,
				left, (left + right) / 2, right };
		float[] editSquareYs = { top, top, top, (top + bottom) / 2,
				(top + bottom) / 2, bottom, bottom, bottom };

		for (int i = 0; i <= 7; i++) {
			boolean inSquare = Math.abs(curX - editSquareXs[i]) <= squareRadius
					&& Math.abs(curY - editSquareYs[i]) <= squareRadius;
			if (inSquare) {
				return i;
			}
		}
		// Not in any edit square region, then default is 8, move the tag
		return 8;
	}

	// This function is used to change tag location based on the user
	// touch location
	// All location is location on the bitmap, not on the enlarged bitmap
	private void changeTagLocation(float prevX, float prevY, float curX,
			float curY, int movingMode) {
		// Fist touch situation, when prevX and prevY are less than 0
		if (prevX < 0 && prevY < 0) {
			return;
		}

		Photo currentPhoto = moment.getPhoto(currentPhotoIndex);
		float left = currentEdittedTagLocation.left;
		float right = currentEdittedTagLocation.right;
		float top = currentEdittedTagLocation.top;
		float bottom = currentEdittedTagLocation.bottom;
		float offsetX = curX - prevX;
		float offsetY = curY - prevY;

		// Change the tag location
		if (movingMode == 0) {
			// Change leftTop
			currentEdittedTagLocation.set(curX, curY, right, bottom);
		} else if (movingMode == 1) {
			// Change middleTop
			currentEdittedTagLocation.set(left, curY, right, bottom);
		} else if (movingMode == 2) {
			// Change rightTop
			currentEdittedTagLocation.set(left, curY, curX, bottom);
		} else if (movingMode == 3) {
			// Change leftMiddle
			currentEdittedTagLocation.set(curX, top, right, bottom);
		} else if (movingMode == 4) {
			// Change rightMiddle
			currentEdittedTagLocation.set(left, top, curX, bottom);
		} else if (movingMode == 5) {
			// Change leftBottom
			currentEdittedTagLocation.set(curX, top, right, curY);
		} else if (movingMode == 6) {
			// Change middleBottom
			currentEdittedTagLocation.set(left, top, right, curY);
		} else if (movingMode == 7) {
			// Change bottomRight
			currentEdittedTagLocation.set(left, top, curX, curY);
		} else {
			// Check whether the new location is in the boundary
			boolean isInBoundary = left + offsetX >= ImageProcessor.IMAGE_BORDER_WIDTH
					&& top + offsetY >= ImageProcessor.IMAGE_BORDER_WIDTH
					&& right + offsetX <= currentPhoto.getLargeBitmap()
							.getWidth() - ImageProcessor.IMAGE_BORDER_WIDTH
					&& bottom + offsetY <= currentPhoto.getLargeBitmap()
							.getHeight() - ImageProcessor.IMAGE_BORDER_WIDTH;
			// Move the tag
			if (isInBoundary) {
				currentEdittedTagLocation.set(left + offsetX, top + offsetY,
						right + offsetX, bottom + offsetY);
			}
		}
	}
}

package com.reflectmobile.activity;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.reflectmobile.R;
import com.reflectmobile.data.Memory;
import com.reflectmobile.data.Moment;
import com.reflectmobile.data.Photo;
import com.reflectmobile.data.Tag;
import com.reflectmobile.utility.NetworkManager;
import com.reflectmobile.utility.NetworkManager.HttpGetImageTask;
import com.reflectmobile.utility.NetworkManager.HttpGetTask;
import com.reflectmobile.utility.NetworkManager.HttpImageTaskHandler;
import com.reflectmobile.utility.NetworkManager.HttpDeleteTask;
import com.reflectmobile.utility.NetworkManager.HttpTaskHandler;
import com.reflectmobile.view.CustomScrollView;
import com.reflectmobile.view.CustomViewPager;
import com.reflectmobile.widget.ImageProcessor;

import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.provider.MediaStore;

public class PhotoActivity extends BaseActivity {

	private String TAG = "PhotoActivity";
	private Moment moment;
	private Memory[] mMemories;
	private LayoutInflater mInflater;

	private int communityId;
	private int momentId;

	private Menu menu;

	/* photo gallery variables */
	static final int CODE_ADD_STORY = 101;
	static final int CODE_ADD_DETAIL = 102;
	static final int CODE_ADD_SOUND = 103;
	static final int CODE_SELECT_PICTURE = 104;

	/* photo gallery variables */
	private CustomViewPager viewPager;
	private CustomScrollView scrollView;
	private String selectedImagePath;
	private ImageView img;

	// MediaPlayer for playing sounds
	private MediaPlayer mediaPlayer;
	private boolean isPlaying = false;
	private boolean isPaused = false;
	private int soundPlayingId = -1;
	private ImageView soundIcon;
	

	// TODO
	// Calculate when the activity start
	private static int photoImageViewHeightDP = 258;
	private static int photoImageViewWidthDP = 360;
	private static int photoImageViewHeightPX = 0;
	private static int photoImageViewWidthPX = 0;
	// Set when the image changes
	private int currentPhotoIndex = 0;
	private ImageView currentImageView = null;
	private float photoOffsetX = 0;
	private float photoOffsetY = 0;
	private float photoScaleFactor = 1;
	private boolean isExpandHorizontal = false;
	// Set when user want to edit the tag
	// If it is in add tag mode, it should be set the the center of the image
	// If it is in edit tag mode, it should be set to the tag location
	private RectF currentEdittedTagLocation = new RectF(100, 100, 200, 200);

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
		title.setTypeface(Typeface.createFromAsset(getAssets(),
				"fonts/RobotoCondensed-Regular.ttf"));

		// Set margin before title
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) title
				.getLayoutParams();
		mlp.setMargins(5, 0, 0, 0);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		ImageView view = (ImageView) findViewById(android.R.id.home);
		view.setPadding(10, 0, 0, 0);

		communityId = getIntent().getIntExtra("community_id", 0);
		momentId = getIntent().getIntExtra("moment_id", 0);
		photoId = getIntent().getIntExtra("photo_id", 0);

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		img = (ImageView) findViewById(R.id.imageView1);
		scrollView = (CustomScrollView) findViewById(R.id.scroll_view);

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
				viewPager = (CustomViewPager) findViewById(R.id.view_pager);
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

		new HttpGetTask(getMomentHandler).execute(NetworkManager.hostName
				+ "/api/moments/" + momentId);

		// TODO
		// handleTagButtonView();
		// Transfer image view size from dp to px
		photoImageViewHeightPX = dpToPx(photoImageViewHeightDP);
		photoImageViewWidthPX = dpToPx(photoImageViewWidthDP);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(PhotoActivity.this, MomentActivity.class);
		intent.putExtra("moment_id", momentId);
		intent.putExtra("community_id", communityId);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.photo_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onStop() {
		stopPlaying();
		super.onStop();
	};

	private void showTaggedMenu() {
		MenuItem add_photo = menu.findItem(R.id.action_add_photo);
		MenuItem add_tag = menu.findItem(R.id.action_add_tag);
		MenuItem edit_tag = menu.findItem(R.id.action_edit_tag);
		MenuItem delete_tag = menu.findItem(R.id.action_delete_tag);
		add_photo.setVisible(false);
		add_tag.setVisible(true);
		edit_tag.setVisible(true);
		delete_tag.setVisible(true);
	}

	private void showNonTaggedMenu() {
		MenuItem add_photo = menu.findItem(R.id.action_add_photo);
		MenuItem add_tag = menu.findItem(R.id.action_add_tag);
		MenuItem edit_tag = menu.findItem(R.id.action_edit_tag);
		MenuItem delete_tag = menu.findItem(R.id.action_delete_tag);
		add_photo.setVisible(true);
		add_tag.setVisible(false);
		edit_tag.setVisible(false);
		delete_tag.setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_photo:
			Intent intent = new Intent(PhotoActivity.this,
					GalleryActivity.class);
			intent.putExtra("community_id", communityId);
			intent.putExtra("moment_id", momentId);
			startActivity(intent);
			return true;
		case R.id.action_edit_tag:
			scrollView.setScrollingEnabled(false);
			currentImageView.setOnTouchListener(onEditPhotoTouchListener);
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
		currentPhotoIndex = position;
		photoId = moment.getPhoto(position).getId();

		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		currentImageView = (ImageView) viewPager
				.findViewWithTag(currentPhotoIndex);

		final ViewGroup memoryContainer = (ViewGroup) findViewById(R.id.memories_container);
		memoryContainer.removeAllViews();
		final TextView memoryCaption = (TextView) findViewById(R.id.memories_caption);
		memoryCaption.setText("0 MEMORIES");

		ToggleButton tagButton = (ToggleButton) findViewById(R.id.button_photo_tag);
		tagButton.setActivated(false);
		tagButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.isActivated()) {
					v.setActivated(false);
					showNonTaggedPhoto();
				} else {
					v.setActivated(true);
					showTaggedRegions();
				}
			}
		});

		ImageButton addSoundButton = (ImageButton) findViewById(R.id.add_sound);
		addSoundButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(PhotoActivity.this,
						AddSoundActivity.class);
				intent.putExtra("photo_id", moment.getPhoto(position).getId());
				startActivityForResult(intent, CODE_ADD_SOUND);
			}
		});

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
					final ImageButton dotMenu = (ImageButton) card
							.findViewById(R.id.memory_card_dot);
					final Memory memory = mMemories[count];

					if (memory.getType().equals("sound")) {
						card.setTag(memory);
						card.setOnClickListener(onSoundPlayClicked);
					}
					memoryIcon.setImageResource(memory.getResourceId());
					memoryText.setText(memory.getContent());
					memoryInfo.setText(memory.getInfo());
					memoryContainer.addView(card);
					dotMenu.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// Creating the instance of PopupMenu
							PopupMenu popup = new PopupMenu(PhotoActivity.this,
									dotMenu);
							// Inflating the Popup using xml file
							popup.getMenuInflater().inflate(
									R.menu.popup_memory, popup.getMenu());

							// registering popup with OnMenuItemClickListener
							popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
								public boolean onMenuItemClick(MenuItem item) {
									switch (item.getItemId()) {
									case R.id.action_edit_memory:
										editMemory(memory,
												moment.getPhoto(position)
														.getId());
										return true;
									case R.id.action_delete_memory:
										deleteMemory(memory.getId());
										return true;
									default:
										return true;
									}
								}
							});

							popup.show();// showing popup menu
						}
					});
				}
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error within GET request: " + reason);
			}
		};

		new HttpGetTask(getMemoriesHandler)
				.execute(NetworkManager.hostName + "/api/memories?photo_id="
						+ moment.getPhoto(position).getId());

		final HttpTaskHandler getTagsHandler = new HttpTaskHandler() {

			@Override
			public void taskSuccessful(String result) {
				Log.d(TAG, result);
				JSONArray tagJSONArray;
				try {
					tagJSONArray = new JSONArray(result);
					for (int j = 0; j <= tagJSONArray.length() - 1; j++) {
						Tag tag = Tag.getTagInfo(tagJSONArray.getString(j));
						moment.getPhoto(position).addTag(tag);
					}
				} catch (JSONException e) {
					Log.e(TAG, "Error parse the tag json");
				}
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error downloading the tag");
			}
		};

		new HttpGetTask(getTagsHandler).execute(NetworkManager.hostName
				+ "/api/photos/" + moment.getPhoto(position).getId() + "/tags");

	}

	private void editMemory(Memory memory, int photoId) {
		String type = memory.getType();
		if (type.equals("detail")) {
			Intent intent = new Intent(PhotoActivity.this,
					AddDetailActivity.class);
			intent.putExtra("memory_id", memory.getId());
			intent.putExtra("photo_id", photoId);
			Pattern pattern = Pattern.compile("(.*) (WAS [A-Z]*) (.*)");
			Matcher matcher = pattern.matcher(memory.getContent());
			if (matcher.find()) {
				intent.putExtra("name", matcher.group(1));
				intent.putExtra("spinner_value", matcher.group(2));
				intent.putExtra("detail", matcher.group(3));
			}
			startActivityForResult(intent, CODE_ADD_DETAIL);
		} else if (type.equals("story")) {
			Intent intent = new Intent(PhotoActivity.this,
					AddStoryActivity.class);
			intent.putExtra("memory_id", memory.getId());
			intent.putExtra("story", memory.getContent());
			intent.putExtra("photo_id", photoId);
			startActivityForResult(intent, CODE_ADD_STORY);
		} else if (type.equals("sound")) {

		}
	}

	private void deleteMemory(int id) {
		HttpTaskHandler httpDeleteTaskHandler = new HttpTaskHandler() {

			@Override
			public void taskSuccessful(String result) {
				Intent intent = getIntent();
				finish();
				startActivity(intent);
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error deleting memory");
			}
		};
		new HttpDeleteTask(httpDeleteTaskHandler)
				.execute(NetworkManager.hostName + "/api/memories/" + id);
	}

	private void showTaggedRegions() {

		HttpImageTaskHandler httpImageTaskHandler = new HttpImageTaskHandler() {
			@Override
			public void taskSuccessful(Drawable drawable) {
				final Photo currentPhoto = moment.getPhoto(currentPhotoIndex);
				currentPhoto.setLargeBitmap(((BitmapDrawable) drawable)
						.getBitmap());
				// Draw tags for the photo
				Bitmap taggedBitmap = ImageProcessor.generateTaggedBitmap(
						currentPhoto.getLargeBitmap(),
						currentPhoto.getTagList());
				// Cache the tagged photo bitmap
				currentPhoto.setTaggedLargeBitmap(taggedBitmap);

				currentImageView.setImageBitmap(taggedBitmap);
				setPhotoOffset();
				currentImageView.setOnTouchListener(onPhotoTouchListener);
			}

			@Override
			public void taskFailed(String reason) {
				Log.e(TAG, "Error downloading the tags");
			}
		};

		Bitmap taggedBitmap = moment.getPhoto(currentPhotoIndex)
				.getTaggedLargeBitmap();
		if (taggedBitmap == null) {
			new HttpGetImageTask(httpImageTaskHandler).execute(moment.getPhoto(
					currentPhotoIndex).getImageLargeURL());
		} else {
			currentImageView.setImageBitmap(taggedBitmap);
			setPhotoOffset();
			currentImageView.setOnTouchListener(onPhotoTouchListener);
		}

		showTaggedMenu();
		viewPager.setPagingEnabled(false);
	}

	private void showNonTaggedPhoto() {
		currentImageView.setImageDrawable(moment.getPhoto(currentPhotoIndex)
				.getMediumDrawable());
		currentImageView.setOnTouchListener(null);

		showNonTaggedMenu();
		viewPager.setPagingEnabled(true);
	}

	private void stopPlaying() {
		isPlaying = false;
		isPaused = false;
		soundPlayingId = -1;
		if (soundIcon!=null){
			soundIcon.setImageResource(R.drawable.sound_small);
		}
		if (mediaPlayer!=null) {
			mediaPlayer.release();
		}
		mediaPlayer = null;
	}

	private OnClickListener onSoundPlayClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Memory memory = (Memory) v.getTag();
			if (memory.getId() != soundPlayingId){
				stopPlaying();
				soundPlayingId = memory.getId();
				soundIcon = (ImageView) v.findViewById(R.id.memory_icon);
			}
			
			if (!isPlaying && !isPaused) {
				isPlaying = true;
				isPaused = false;
				soundIcon.setImageResource(R.drawable.pause);
				int sound_id = memory.getId();
				String url = NetworkManager.SOUND_HOST_NAME + "/sounds/"
						+ sound_id;
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				try {
					mediaPlayer.setDataSource(url);
				} catch (IllegalArgumentException e) {
					Log.d(TAG, "Illegal Argument Exception");
				} catch (SecurityException e) {
					Log.d(TAG, "Security Exception");
				} catch (IllegalStateException e) {
					Log.d(TAG, "Illegal State Exception");
				} catch (IOException e) {
					Log.d(TAG, "IOException");
				}
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer player) {
						player.start();
					}
				});
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer arg0) {
						stopPlaying();
					}
				});

				mediaPlayer.prepareAsync();
			} else if (isPlaying && !isPaused) {
				isPaused = true;
				isPlaying = false;
				soundIcon.setImageResource(R.drawable.sound_small);
				mediaPlayer.pause();
			} else if (!isPlaying && isPaused) {
				soundIcon.setImageResource(R.drawable.pause);
				isPaused = false;
				isPlaying = true;
				mediaPlayer.start();
			}
		}
	};

	private OnTouchListener onPhotoTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			final Photo currentPhoto = moment.getPhoto(currentPhotoIndex);
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
				Bitmap newBitmap = ImageProcessor
						.generateHighlightedTaggedBitmap(
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
	};

	private OnTouchListener onEditPhotoTouchListener = new OnTouchListener() {
		float prevBitmapX = 0;
		float prevBitmapY = 0;
		int movingNode = 8;

		public boolean onTouch(View v, MotionEvent event) {
			final Photo currentPhoto = moment.getPhoto(currentPhotoIndex);
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
				changeTagLocation(prevBitmapX, prevBitmapY, bitmapX, bitmapY,
						movingNode);
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
	};

	public class ImagePagerAdapter extends PagerAdapter {

		public ImagePagerAdapter(Context context) {

		}

		@Override
		public int getCount() {
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

			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setTag(position);
			((ViewPager) container).addView(imageView);

			HttpImageTaskHandler httpImageTaskHandler = new HttpImageTaskHandler() {
				private int drawableIndex = position;

				@Override
				public void taskSuccessful(Drawable drawable) {
					moment.getPhoto(drawableIndex).setMediumDrawable(drawable);
					ImageView imageView = (ImageView) ((ViewPager) container)
							.findViewWithTag(drawableIndex);
					if (imageView != null) {
						imageView.setImageDrawable(drawable);
					}
				}

				@Override
				public void taskFailed(String reason) {
					Log.e(TAG, "Error downloading the tags");
				}
			};

			Drawable mediumDrawable = moment.getPhoto(position)
					.getMediumDrawable();
			if (mediumDrawable == null) {
				new HttpGetImageTask(httpImageTaskHandler).execute(moment
						.getPhoto(position).getImageMediumURL());
			} else {
				imageView.setImageDrawable(mediumDrawable);
			}
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}
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

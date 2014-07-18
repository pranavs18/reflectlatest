package com.reflectmobile.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.reflectmobile.R;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class GalleryActivity extends BaseActivity {

	// private static final String TAG = "GalleryActivity";

	private ArrayList<String> imageUrls;
	private DisplayImageOptions options;
	private ImageAdapter imageAdapter;
	String photoPath;

	private static final int CODE_TAKE_PHOTO = 101;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.activity_gallery);
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

		getActionBar().setIcon(R.drawable.picture);

		// set configuration for the image loader instance
		// we can have default configuration but this config will invoke faster
		// loading of the images
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.threadPoolSize(3)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(1500000)
				// 1.5 Mb
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.enableLogging().build();

		ImageLoader.getInstance().init(config);

		// Access the Media Store to retrieve the images
		final String[] columns = { MediaStore.Images.Media.DATA,
				MediaStore.Images.Media._ID };
		final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
		@SuppressWarnings("deprecation")
		Cursor imagecursor = managedQuery(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
				null, orderBy + " DESC");

		this.imageUrls = new ArrayList<String>();

		for (int i = 0; i < imagecursor.getCount(); i++) {
			imagecursor.moveToPosition(i);
			int dataColumnIndex = imagecursor
					.getColumnIndex(MediaStore.Images.Media.DATA);
			imageUrls.add(imagecursor.getString(dataColumnIndex));
			Log.d(GalleryActivity.class.getSimpleName(),
					"=====> Array path => " + imageUrls.get(i));
			// Log the url of the images being displayed in the photo gallery
		}

		options = new DisplayImageOptions.Builder().cacheInMemory()
				.cacheOnDisc().build();

		ArrayList<String> array = new ArrayList<String>();
		if (getIntent().hasExtra("chosenImages")) {
			array = getIntent().getStringArrayListExtra("chosenImages");
		}
		Set<String> chosenImages = new HashSet<String>(array);

		imageAdapter = new ImageAdapter(this, imageUrls, chosenImages);

		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(imageAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gallery_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_add_photos:
			if (imageAdapter.getCheckedItems().size() > 0) {
				Intent intent = new Intent(GalleryActivity.this,
						AddPhotosActivity.class);
				if (getIntent().hasExtra("community_id")) {
					intent.putExtra("community_id",
							getIntent().getIntExtra("community_id", 0));
				}
				if (getIntent().hasExtra("moment_id")) {
					intent.putExtra("moment_id",
							getIntent().getIntExtra("moment_id", 0));
				}
				intent.putExtra("images", imageAdapter.getCheckedItems());
				startActivity(intent);
			} else {
				int red = android.R.color.holo_red_light;
				Style CustomAlert = new Style.Builder().setDuration(2000)
						.setHeight(LayoutParams.WRAP_CONTENT).setTextSize(16)
						.setBackgroundColor(red).setPaddingInPixels(26).build();
				Crouton.makeText(this, "Please, select at least one photo",
						CustomAlert).show();
			}
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
			// Save to gallery
			File f = new File(photoPath);
			MediaScannerConnection.scanFile(GalleryActivity.this,
					new String[] { f.toString() }, null,
					new MediaScannerConnection.OnScanCompletedListener() {
						public void onScanCompleted(String path, Uri uri) {
							Log.i("ExternalStorage", "Scanned " + path + ":");
							Log.i("ExternalStorage", "-> uri=" + uri);
							finish();
							Intent intent = getIntent();
							startActivity(intent);
						}
					});
		}
	}

	// Create a image file on external storage and return the file
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + ".jpg";
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = new File(storageDir, imageFileName);
		photoPath = image.getAbsolutePath();
		return image;
	}

	private void takePhoto() {
		// Save current image selection
		getIntent().putExtra("chosenImages", imageAdapter.getCheckedItems());
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, CODE_TAKE_PHOTO);
			}
		}
	}

	@Override
	protected void onStop() {
		imageLoader.stop();
		super.onStop();
	}

	// This class defines the view for the photo gallery and populates the data
	// structure for holding the
	// selected images
	public class ImageAdapter extends BaseAdapter {

		ArrayList<String> mList;
		LayoutInflater mInflater;
		Context mContext;
		SparseBooleanArray mSparseBooleanArray;

		public ImageAdapter(Context context, ArrayList<String> imageList,
				Set<String> chosenImages) {

			mContext = context;
			mInflater = LayoutInflater.from(mContext);
			mSparseBooleanArray = new SparseBooleanArray();
			mList = imageList;
			for (int count = 0; count < mList.size(); count++) {
				if (chosenImages.contains(mList.get(count))) {
					mSparseBooleanArray.put(count, true);
				}
			}
		}

		public ArrayList<String> getCheckedItems() {
			ArrayList<String> mTempArray = new ArrayList<String>();

			for (int i = 0; i < mList.size(); i++) {
				if (mSparseBooleanArray.get(i)) {
					mTempArray.add(mList.get(i - 1));
				}
			}

			return mTempArray;
		}

		@Override
		public int getCount() {
			return mList.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.gallery_item, parent,
						false);
			}

			final ImageView imageView = (ImageView) convertView
					.findViewById(R.id.imageView1);
			final ImageView borderView = (ImageView) convertView
					.findViewById(R.id.border);
			final ImageView checkbox = (ImageView) convertView
					.findViewById(R.id.checkBox);

			// Special case for displaying camera on the position 0
			if (position == 0) {
				imageView.setImageDrawable(getResources().getDrawable(
						R.drawable.add_from_camera));
				borderView.setVisibility(View.GONE);
				checkbox.setVisibility(View.GONE);
			} else {
				boolean isChecked = mSparseBooleanArray.get(position);
				if (isChecked) {
					borderView.setVisibility(View.VISIBLE);
					checkbox.setVisibility(View.VISIBLE);
				} else {
					borderView.setVisibility(View.GONE);
					checkbox.setVisibility(View.GONE);
				}

				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageView.setImageDrawable(null);

				imageLoader.displayImage("file://" + mList.get(position - 1),
						imageView, options, new SimpleImageLoadingListener() {
							@Override
							public void onLoadingComplete(Bitmap loadedImage) {
								Animation anim = AnimationUtils.loadAnimation(
										GalleryActivity.this, R.anim.fade_in);
								imageView.setAnimation(anim);
								anim.start();
							}
						});
			}
			convertView.setTag(position);
			convertView.setOnClickListener(mOnClickListener);

			return convertView;
		}

		OnClickListener mOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = (Integer) v.getTag();
				// Special case for first photo:
				if (position == 0) {
					takePhoto();
				} else {
					boolean isChecked = mSparseBooleanArray.get(position);
					isChecked = !isChecked;
					mSparseBooleanArray.put(position, isChecked);

					final ImageView borderView = (ImageView) v
							.findViewById(R.id.border);
					final ImageView checkbox = (ImageView) v
							.findViewById(R.id.checkBox);

					if (isChecked) {
						borderView.setVisibility(View.VISIBLE);
						checkbox.setVisibility(View.VISIBLE);
					} else {
						borderView.setVisibility(View.GONE);
						checkbox.setVisibility(View.GONE);
					}
				}

			}
		};
	}

}
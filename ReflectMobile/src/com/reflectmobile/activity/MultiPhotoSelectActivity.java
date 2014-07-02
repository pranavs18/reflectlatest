package com.reflectmobile.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
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
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.reflectmobile.R;

public class MultiPhotoSelectActivity extends BaseActivity {

	private ArrayList<String> imageUrls;
	private DisplayImageOptions options;
	private ImageAdapter imageAdapter;
	private static ArrayList<String> selectedItems;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		hasNavigationDrawer = false;
		setContentView(R.layout.ac_image_grid);
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
			Log.d(MultiPhotoSelectActivity.class.getSimpleName(),
					"=====> Array path => " + imageUrls.get(i));
			// Log the url of the images being displayed in the photo gallery
		}

		options = new DisplayImageOptions.Builder()
				.cacheInMemory().cacheOnDisc().build();

		imageAdapter = new ImageAdapter(this, imageUrls);

		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(imageAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.multiple_photo_menu, menu);
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
			Intent intent = new Intent(MultiPhotoSelectActivity.this,
					MultiPhotoSelectActivity.class);
			startActivity(intent);
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStop() {
		imageLoader.stop();
		super.onStop();
	}

	// track the selected images (tapped images)
	public void btnChoosePhotosClick(View v) {

		selectedItems = imageAdapter.getCheckedItems();
		Toast.makeText(MultiPhotoSelectActivity.this,
				"Total photos selected: " + selectedItems.size(),
				Toast.LENGTH_SHORT).show();
		Log.d(MultiPhotoSelectActivity.class.getSimpleName(),
				"Selected Items: " + selectedItems.toString());
	}

	public void btnChoosePhotosCancel(View v) {

		// TODO
		/*
		 * if(selectedItems.size() >0){ for(int i=0;i<selectedItems.size();i++){
		 * selectedItems.remove(i); CheckBox mCheckBox = (CheckBox)
		 * findViewById(R.id.checkBox1); mCheckBox.setChecked(false); } }
		 * for(int i=0;i<selectedItems.size();i++){ final CheckBox checkBox =
		 * (CheckBox) findViewById(R.id.checkBox1); if (checkBox.isChecked()) {
		 * checkBox.setChecked(false); } }
		 */
		imageAdapter.notifyDataSetChanged();
		Log.d(MultiPhotoSelectActivity.class.getSimpleName(),
				"Cancelled Selected Photos");

	}

	// This class defines the view for the photo gallery and populates the data
	// structure for holding the
	// selected images
	public class ImageAdapter extends BaseAdapter {

		ArrayList<String> mList;
		LayoutInflater mInflater;
		Context mContext;
		SparseBooleanArray mSparseBooleanArray;

		public ImageAdapter(Context context, ArrayList<String> imageList) {

			mContext = context;
			mInflater = LayoutInflater.from(mContext);
			mSparseBooleanArray = new SparseBooleanArray();
			mList = new ArrayList<String>();
			this.mList = imageList;

		}

		public ArrayList<String> getCheckedItems() {
			ArrayList<String> mTempArry = new ArrayList<String>();

			for (int i = 0; i < mList.size(); i++) {
				if (mSparseBooleanArray.get(i)) {
					mTempArry.add(mList.get(i));
				}
			}

			return mTempArry;
		}

		@Override
		public int getCount() {
			return imageUrls.size();
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
				convertView = mInflater.inflate(R.layout.row_multiphoto_item,
						null);
			}

			boolean isChecked = mSparseBooleanArray.get(position);
			final ImageView borderView = (ImageView) convertView
					.findViewById(R.id.border);
			final ImageView checkbox = (ImageView) convertView
					.findViewById(R.id.checkBox);

			if (isChecked) {
				borderView.setVisibility(View.VISIBLE);
				checkbox.setVisibility(View.VISIBLE);
			} else {
				borderView.setVisibility(View.GONE);
				checkbox.setVisibility(View.GONE);
			}

			final ImageView imageView = (ImageView) convertView
					.findViewById(R.id.imageView1);
			imageView.setScaleType(ScaleType.CENTER_CROP);

			imageLoader.displayImage("file://" + imageUrls.get(position),
					imageView, options, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(Bitmap loadedImage) {
							Animation anim = AnimationUtils.loadAnimation(
									MultiPhotoSelectActivity.this,
									R.anim.fade_in);
							imageView.setAnimation(anim);
							anim.start();
						}
					});

			convertView.setTag(position);
			convertView.setOnClickListener(mOnClickListener);

			return convertView;
		}

		OnClickListener mOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = (Integer) v.getTag();
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
		};
	}

}
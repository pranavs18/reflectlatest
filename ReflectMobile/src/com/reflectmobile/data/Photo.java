package com.reflectmobile.data;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class Photo {
	private static String TAG = "Photo";

	private int id;
	private String imageMediumURL;
	private String imageMediumThumbURL;
	private String imageLargeURL;
	private ArrayList<Tag> tagList;
	private Bitmap largeBitmap;
	private Bitmap taggedBitmap;

	public Photo(int id) {
		this.setId(id);
		this.tagList = new ArrayList<Tag>();
	}

	public String getImageMediumURL() {
		return imageMediumURL;
	}

	public void setImageMediumURL(String imageMediumURL) {
		this.imageMediumURL = imageMediumURL;
	}

	public String getImageMediumThumbURL() {
		return imageMediumThumbURL;
	}

	public void setImageMediumThumbURL(String imageMediumThumbURL) {
		this.imageMediumThumbURL = imageMediumThumbURL;
	}

	public String getImageLargeURL() {
		return imageLargeURL;
	}

	public void setImageLargeURL(String imageLargeURL) {
		this.imageLargeURL = imageLargeURL;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public ArrayList<Tag> getTagList() {
		return tagList;
	}

	public void setTagList(ArrayList<Tag> tagList) {
		this.tagList = tagList;
	}

	public void addTag(Tag tag){
		this.tagList.add(tag);
	}
	
	public static Photo getPhotoInfo(String jsonString) {
		try {
			JSONObject photoJSONObject = new JSONObject(jsonString);
			int photoID = photoJSONObject.getInt("id");

			Photo photo = new Photo(photoID);
			String photoImageMediumURL = photoJSONObject
					.getString("image_medium_url");
			photo.setImageMediumURL(photoImageMediumURL);
			String photoImageMediumThumbURL = photoJSONObject
					.getString("image_medium_thumb_url");
			photo.setImageMediumThumbURL(photoImageMediumThumbURL);
			String photoImageLargeURL = photoJSONObject
					.getString("image_large_url");
			photo.setImageLargeURL(photoImageLargeURL);
			return photo;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON");
			e.printStackTrace();
		}
		return null;
	}

	public Bitmap getLargeBitmap() {
		return largeBitmap;
	}

	public void setLargeBitmap(Bitmap bitmap) {
		this.largeBitmap = bitmap;
	}
	
	public Bitmap generateTaggedBitmap() {
		Bitmap bitmap = largeBitmap;
		// Create buffer new bitmap
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);

		// Generate brush
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);

		// Draw tags based on tag coordinate
		for (Tag tag : tagList) {
			RectF rect = new RectF(tag.getUpLeftX(), tag.getUpLeftY(),
					tag.getUpLeftX() + tag.getBoxWidth(), tag.getUpLeftY()
							+ tag.getBoxLength());
			canvas.drawRoundRect(rect, 2, 2, paint);
		}
		taggedBitmap = newBitmap;
		return newBitmap;
	}
	
	public Bitmap drawOnPhoto(int imageViewHeight, int imageViewWidth, float imageViewX, float imageViewY, float startX, float startY) {
		float scaleFactor = imageViewHeight / largeBitmap.getHeight();
		float offsetX = (int) ((imageViewWidth - scaleFactor * largeBitmap.getWidth()) / 2);
		float bitmapX = imageViewX - offsetX;
		float bitmapY = imageViewY;
		startX = startX - offsetX;
		
		Bitmap bitmap = largeBitmap;
		// Create buffer new bitmap
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);

		// Generate brush
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(10);
		
		canvas.drawLine(startX, startY, bitmapX, bitmapY, paint);
		largeBitmap = newBitmap;
		return newBitmap;
	}
	
	public Bitmap generateHighlightedTagBitmap(int imageViewHeight, int imageViewWidth, float imageViewX, float imageViewY) {
		float scaleFactor = imageViewHeight / largeBitmap.getHeight();
		float offsetX = (int) ((imageViewWidth - scaleFactor * largeBitmap.getWidth()) / 2);
		float bitmapX = imageViewX - offsetX;
		float bitmapY = imageViewY;
		
		Bitmap bitmap = taggedBitmap;
		// Create buffer new bitmap
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);

		// Generate brush
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(10);		
		
		boolean isInTagRegion = false;
		for (Tag tag : tagList) {
			boolean isInXRange = bitmapX <= tag.getUpLeftX() + tag.getBoxWidth() && bitmapX >= tag.getUpLeftX();
			boolean isInYRange = bitmapY <= tag.getUpLeftY() + tag.getBoxLength() && bitmapY >= tag.getUpLeftY();
			if (isInXRange && isInYRange) {
				isInTagRegion = true;
				RectF rect = new RectF(tag.getUpLeftX(), tag.getUpLeftY(),
						tag.getUpLeftX() + tag.getBoxWidth(), tag.getUpLeftY()
								+ tag.getBoxLength());
				canvas.drawRoundRect(rect, 2, 2, paint);				
			}
		}
		return isInTagRegion ? newBitmap : taggedBitmap;
	}
}

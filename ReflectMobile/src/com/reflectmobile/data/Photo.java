package com.reflectmobile.data;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.reflectmobile.widget.ImageProcessor;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Photo {
	private static String TAG = "Photo";

	private int id;
	private String imageMediumURL;
	private String imageMediumThumbURL;
	private String imageLargeURL;
	private ArrayList<Tag> tagList;

	private Drawable mediumDrawable;

	private Bitmap largeBitmap;
	private Bitmap darkenLargeBitmap;
	private Bitmap darkenTaggedLargeBitmap;
	private Bitmap taggedLargeBitmap;

	public Photo(int id) {
		this.setId(id);
		this.tagList = new ArrayList<Tag>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public ArrayList<Tag> getTagList() {
		return tagList;
	}

	public void setTagList(ArrayList<Tag> tagList) {
		this.tagList = tagList;
	}

	public void addTag(Tag tag) {
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

	public Drawable getMediumDrawable() {
		return mediumDrawable;
	}

	public void setMediumDrawable(Drawable mediumDrawable) {
		this.mediumDrawable = mediumDrawable;
	}

	public Bitmap getLargeBitmap() {
		return largeBitmap;
	}

	public void setLargeBitmap(Bitmap bitmap) {
		this.largeBitmap = bitmap;
	}

	public Bitmap getDarkenLargeBitmap() {
		if (darkenLargeBitmap == null) {
			darkenLargeBitmap = ImageProcessor.generateDarkenImage(
					getLargeBitmap(), 100);
		}
		return darkenLargeBitmap;
	}

	public Bitmap getDarkenTaggedLargeBitmap() {
		if (darkenTaggedLargeBitmap == null) {
			darkenTaggedLargeBitmap = ImageProcessor.generateDarkenImage(
					getTaggedLargeBitmap(), 100);
		}
		return darkenTaggedLargeBitmap;
	}

	public void refreshTags() {
		taggedLargeBitmap = ImageProcessor.generateTaggedBitmap(
				getLargeBitmap(), getTagList());
		darkenTaggedLargeBitmap = ImageProcessor.generateDarkenImage(
				getTaggedLargeBitmap(), 100);
	}

	public Bitmap getTaggedLargeBitmap() {
		if (taggedLargeBitmap == null) {
			taggedLargeBitmap = ImageProcessor.generateTaggedBitmap(
					getLargeBitmap(), getTagList());
		}
		return taggedLargeBitmap;
	}

}

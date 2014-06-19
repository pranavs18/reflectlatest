package com.reflectmobile.data;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Photo {
	private static String TAG = "Photo";

	private int id;
	private String imageMediumURL;
	private String imageMediumThumbURL;
	private String imageLargeURL;
	private ArrayList<Tag> tagList;

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
}

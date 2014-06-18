package com.reflectmobile.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Photo {
	private static String TAG = "Photo";

	private int id;
	private String imageMediumURL;
	private String imageMediumThumbURL;

	public Photo(int id) {
		this.setId(id);
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
			
			return photo;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON");
			e.printStackTrace();
		}
		return null;
	}

}

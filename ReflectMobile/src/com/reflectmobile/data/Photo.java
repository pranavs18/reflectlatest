package com.reflectmobile.data;

import android.graphics.Bitmap;

public class Photo {
	private int id;
	private String name;
	private String imageMediumURL;
	private Bitmap choppedBitmap;
	private String jsonString;
	
	public Photo(int id, String name, String imageMediumURL, String jsonString){
		this.id = id;
		this.name = name;
		this.setImageMediumURL(imageMediumURL);
		this.jsonString = jsonString;
	}

	public String getImageMediumURL() {
		return imageMediumURL;
	}

	public void setImageMediumURL(String imageMediumURL) {
		this.imageMediumURL = imageMediumURL;
	}

	public Bitmap getChoppedBitmap() {
		return choppedBitmap;
	}

	public void setChoppedBitmap(Bitmap choppedBitmap) {
		this.choppedBitmap = choppedBitmap;
	}
}

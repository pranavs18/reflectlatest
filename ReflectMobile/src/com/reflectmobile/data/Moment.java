package com.reflectmobile.data;

import java.util.ArrayList;

public class Moment {
	private int id;
	private String name;
	private String date;
	private ArrayList<Photo> photoList;
	private String jsonString;

	public Moment(int id, String name, String jsonString) {
		this.id = id;
		this.setName(name);
		this.photoList = new ArrayList<Photo>();
		this.jsonString = jsonString;
	}

	public ArrayList<Photo> getPhotoList() {
		return photoList;
	}

	public void setPhotoList(ArrayList<Photo> photoList) {
		this.photoList = photoList;
	}

	public Photo getPhoto(int index) {
		if (index <= photoList.size() - 1) {
			return photoList.get(index);
		} else {
			return null;
		}
	}

	public void addPhoto(Photo photo) {
		this.photoList.add(photo);
	}

	public int getNumOfPhotos() {
		return photoList.size();
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

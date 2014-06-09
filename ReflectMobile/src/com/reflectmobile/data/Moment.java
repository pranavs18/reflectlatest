package com.reflectmobile.data;

import java.util.ArrayList;

public class Moment {
	private int id;
	private String name;
	private ArrayList<Photo> photoList;
	
	public Moment(int id, String name){
		this.id = id;
		this.name = name;
		this.photoList = new ArrayList<Photo>();
	}
	
	public Photo getPhoto(int index){
		if (index <= photoList.size() - 1) { return photoList.get(index);}
		else { return null;}
	}
	
	public void addPhoto(Photo photo){
		this.photoList.add(photo);
	}
}

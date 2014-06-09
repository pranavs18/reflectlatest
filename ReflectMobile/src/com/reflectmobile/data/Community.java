package com.reflectmobile.data;

import java.util.ArrayList;

import android.R.integer;

public class Community {
	private int id;
	private String name;
	private String description;
	private ArrayList<Moment> momentList;
	
	public Community(int id, String name, String description){
		this.id = id;
		this.name = name;
		this.description = description;
		this.momentList = new ArrayList<Moment>();
	}
	
	public void addMoment(Moment moment){
		momentList.add(moment);
	}
	
	public Moment getMoment(int index){
		if (index <= momentList.size() - 1) { return momentList.get(index);}
		else { return null;}		
	}
}

package com.reflectmobile.data;

import java.util.ArrayList;

import android.R.integer;

public class Community {
	private int id;
	private String name;
	private String description;
	private ArrayList<Moment> momentList;
	private String jsonString;
	
	public Community(int id, String name, String description, String jsonString){
		this.id = id;
		this.setName(name);
		this.description = description;
		this.momentList = new ArrayList<Moment>();
		this.jsonString = jsonString;
	}
	
	public void addMoment(Moment moment){
		momentList.add(moment);
	}
	
	public Moment getMoment(int index){
		if (index <= momentList.size() - 1) { return momentList.get(index);}
		else { return null;}		
	}
	
	public int getNumOfMoment(){
		return momentList.size();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
}

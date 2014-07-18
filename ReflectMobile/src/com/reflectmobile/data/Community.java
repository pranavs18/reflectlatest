package com.reflectmobile.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Community {
	private static String TAG = "Community";

	private int id;
	private String name;
	private String description;
	private ArrayList<Moment> momentList;
	private String jsonString;
	private String firstPhoto;

	public Community() {
		this.setId(0);
		this.name = null;
		this.setDescription(null);
		this.momentList = new ArrayList<Moment>();
		this.jsonString = null;
		this.firstPhoto = null;
	}

	public Community(int id, String name, String description, String jsonString) {
		this.setId(id);
		this.name = name;
		this.setDescription(description);
		this.momentList = new ArrayList<Moment>();
		this.jsonString = jsonString;
	}

	public void addMoment(Moment moment) {
		momentList.add(moment);
	}

	public Moment getMoment(int index) {
		if (index <= momentList.size() - 1) {
			return momentList.get(index);
		} else {
			return null;
		}
	}

	public int getNumOfMoments() {
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

	public String getFirstPhoto() {
		return firstPhoto;
	}

	public void setFirstPhoto(String firstPhoto) {
		this.firstPhoto = firstPhoto;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static Community[] getCommunitiesInfo(String jsonString) {
		JSONArray mJSONArray;
		try {
			mJSONArray = new JSONArray(jsonString);
			Community[] communities = new Community[mJSONArray.length()];
			for (int count = 0; count < mJSONArray.length(); count++) {
				communities[count] = new Community();
				JSONObject communityData = mJSONArray.getJSONObject(mJSONArray
						.length() - 1 - count);
				String communityName = communityData.getString("name");
				int communityId = communityData.getInt("id");

				communities[count].setName(communityName);
				communities[count].setId(communityId);

				if (!communityData.isNull("first_photo")) {
					JSONObject firstPhoto = communityData
							.getJSONObject("first_photo");
					String location = firstPhoto
							.getString("image_medium_thumb_url");

					communities[count].setFirstPhoto(location);
				}
			}
			return communities;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON");
		}
		return null;
	}

	public static Community getCommunityInfo(String jsonString) {
		JSONObject communityJSONObject;
		try {
			communityJSONObject = new JSONObject(jsonString);

			int communityID = communityJSONObject.getInt("id");
			String communityName = communityJSONObject.getString("name");
			String communityDescription = communityJSONObject
					.getString("description");

			Community community = new Community(communityID, communityName,
					communityDescription, communityJSONObject.toString());

			// get moments
			JSONArray momentJSONArray = new JSONArray(
					communityJSONObject.getString("moments"));
			for (int count = 0; count < momentJSONArray.length(); count++) {
				// create moment obj
				JSONObject momentJSONObject = momentJSONArray
						.getJSONObject(momentJSONArray.length() - 1 - count);

				int momentID = momentJSONObject.getInt("id");
				String momentName = momentJSONObject.getString("name");
				Moment moment = new Moment(momentID, momentName,
						momentJSONObject.toString());

				String date = momentJSONObject.getString("date");
				if (date.equals("null")) {
					moment.setDate("3 July 2014");
				} else {
					moment.setDate(date);
				}

				JSONArray photoJSONArray = new JSONArray(
						momentJSONObject.getString("photos"));
				for (int j = 0; j < photoJSONArray.length(); j++) {
					Photo photo = Photo.getPhotoInfo(photoJSONArray
							.getString(j));
					moment.addPhoto(photo);
				}
				// add moment to community
				community.addMoment(moment);
			}
			return community;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON");
		}
		return null;
	}
}

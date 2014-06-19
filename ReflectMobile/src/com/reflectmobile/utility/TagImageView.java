package com.reflectmobile.utility;

import java.util.ArrayList;

import com.reflectmobile.data.Tag;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TagImageView extends ImageView {
	ArrayList<Tag> tagList;

	public TagImageView(Context context) {
		super(context);
	}

	public TagImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TagImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void drawTags(ArrayList<Tag> tagList) {
		this.tagList = tagList;
		this.invalidate();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Draw tags on the image
	}
}

package com.reflectmobile.utility;

import java.util.ArrayList;

import com.reflectmobile.data.Tag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
		setDrawingCacheEnabled(true);
		buildDrawingCache();
		Bitmap bitmap =getDrawingCache();
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);

		// Generate brush
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);

		// Draw tags based on tag coordinate
		for (Tag tag : tagList) {
			RectF rect = new RectF(tag.getUpLeftX(), tag.getUpLeftY(),
					tag.getUpLeftX() + tag.getBoxWidth(), tag.getUpLeftY()
							+ tag.getBoxLength());
			canvas.drawRoundRect(rect, 2, 2, paint);
		}
		setImageBitmap(newBitmap);
		this.invalidate();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Draw tags on the image
//		if (this.tagList != null) {
//			// Generate brush
//			Paint paint = new Paint();
//			paint.setColor(Color.RED);
//			paint.setStyle(Paint.Style.STROKE);
//			paint.setStrokeWidth(5);
//
//			// Draw tags based on tag coordinate
//			for (Tag tag : tagList) {
//				RectF rect = new RectF(tag.getUpLeftX(), tag.getUpLeftY(),
//						tag.getUpLeftX() + tag.getBoxWidth(), tag.getUpLeftY()
//								+ tag.getBoxLength());
//				canvas.drawRoundRect(rect, 2, 2, paint);
//			}
//		}
	}
}

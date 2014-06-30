package com.reflectmobile.widget;

import java.util.ArrayList;

import com.reflectmobile.data.Tag;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class ImageProcessor {
	public final static int IMAGE_BORDER_WIDTH = 20;
	
	
	// This function is used to draw tags on the bitmap.
	// The input originalbitmap is the bitmap we get from the
	// rewyndr(LargeImage)
	public static Bitmap generateTaggedBitmap(Bitmap originalBitmap,
			ArrayList<Tag> tagList) {
		Bitmap bitmap = originalBitmap;
		// Create buffer new bitmap
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);

		// Generate brush
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);

		// Draw tags based on tag coordinate
		for (Tag tag : tagList) {
			RectF rect = new RectF(tag.getUpLeftX(), tag.getUpLeftY(),
					tag.getUpLeftX() + tag.getBoxWidth(), tag.getUpLeftY()
							+ tag.getBoxLength());
			canvas.drawRoundRect(rect, 2, 2, paint);
		}
		return newBitmap;
	}

	// This function is used to highlighted tag based on the location where user
	// touch
	// If the user touches a tag, then it will darken the rest of the image
	// If the user does not touch a tag, then it will just show the image with
	// all tags
	public static Bitmap generateHighlightedTaggedBitmap(Bitmap originalBitmap,
			Bitmap taggedBitmap, Bitmap darkenTaggedBitmap,
			ArrayList<Tag> tagList, float x, float y) {
		// Dark bitmap as the background
		Bitmap bitmap = darkenTaggedBitmap;
		// Create buffer new bitmap
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);

		// Generate brush
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);

		// Loop through the tag list and check whether the (x, y) is in any tag
		boolean isInTag = false;
		for (Tag tag : tagList) {
			boolean isInXRange = x <= tag.getUpLeftX() + tag.getBoxWidth()
					&& x >= tag.getUpLeftX();
			boolean isInYRange = y <= tag.getUpLeftY() + tag.getBoxLength()
					&& y >= tag.getUpLeftY();
			if (isInXRange && isInYRange) {
				// If (x, y) is in a tag, then copy the lighter sub image
				copySubImage(originalBitmap, newBitmap, tag.getUpLeftX(),
						tag.getUpLeftY(), tag.getBoxWidth(), tag.getBoxLength());
				// Draw the boundary of the tag
				RectF rect = new RectF(tag.getUpLeftX(), tag.getUpLeftY(),
						tag.getUpLeftX() + tag.getBoxWidth(), tag.getUpLeftY()
								+ tag.getBoxLength());
				canvas.drawRoundRect(rect, 2, 2, paint);
				isInTag = true;
				break;
			}
		}

		// Test
		canvas.drawPoint(x, y, paint);
		return isInTag ? newBitmap : taggedBitmap;
	}

	// This function is used to draw the edit square on the image.
	public static Bitmap drawEditSquare(Bitmap originalBitmap,
			Bitmap darkenOriginalBitmap, int upLeftX, int upLeftY,
			int bottomRightX, int bottomRightY, boolean withLittleSquare) {
		Bitmap bitmap = darkenOriginalBitmap;
		// Create buffer new bitmap
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);

		// Guarantee the coordinate is in the boundary
		// The parameter 20 can be changed
		// This check guarantee the little square is always visible to users
		upLeftX = Math.max(IMAGE_BORDER_WIDTH, upLeftX);
		upLeftY = Math.max(IMAGE_BORDER_WIDTH, upLeftY);
		bottomRightX = Math.min(bottomRightX, originalBitmap.getWidth() - IMAGE_BORDER_WIDTH);
		bottomRightY = Math.min(bottomRightY, originalBitmap.getHeight() - IMAGE_BORDER_WIDTH);

		// Generate brush for draw big square
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(10);

		// Draw the square
		RectF rect = new RectF(upLeftX, upLeftY, bottomRightX, bottomRightY);
		canvas.drawRoundRect(rect, 2, 2, paint);

		// Copy the lighter sub image
		copySubImage(originalBitmap, newBitmap, upLeftX, upLeftY, bottomRightX
				- upLeftX, bottomRightY - upLeftY);

		if (withLittleSquare) {
			// Draw the little square on the board
			int[] littleSquareXList = { upLeftX - 3,
					(bottomRightX + upLeftX) / 2, bottomRightX + 3,
					upLeftX - 3, bottomRightX + 3, upLeftX - 3,
					(bottomRightX + upLeftX) / 2, bottomRightX + 3 };
			int[] littleSquareYList = { upLeftY - 3, upLeftY - 3, upLeftY - 3,
					(bottomRightY + upLeftY) / 2, (bottomRightY + upLeftY) / 2,
					bottomRightY + 3, bottomRightY + 3, bottomRightY + 3 };
			int littleSquareRadius = 6;
			for (int i = 0; i <= littleSquareXList.length - 1; i++) {
				// Generate brush for draw little square border
				paint = new Paint();
				paint.setColor(Color.BLACK);
				paint.setStyle(Paint.Style.FILL);
				// Draw the little square border
				rect = new RectF(littleSquareXList[i] - littleSquareRadius - 2,
						littleSquareYList[i] - littleSquareRadius - 2,
						littleSquareXList[i] + littleSquareRadius + 2,
						littleSquareYList[i] + littleSquareRadius + 2);
				canvas.drawRoundRect(rect, 2, 2, paint);

				// Generate brush for draw little square
				paint = new Paint();
				paint.setColor(Color.WHITE);
				paint.setStyle(Paint.Style.FILL);
				// Draw the little square
				rect = new RectF(littleSquareXList[i] - littleSquareRadius,
						littleSquareYList[i] - littleSquareRadius,
						littleSquareXList[i] + littleSquareRadius,
						littleSquareYList[i] + littleSquareRadius);
				canvas.drawRoundRect(rect, 2, 2, paint);
			}
		}
		canvas.drawPoint(upLeftX, upLeftY, paint);
		return newBitmap;
	}

	// This function is used to draw the edit square on the image.
	public static Bitmap drawEditSquare(Bitmap originalBitmap,
			Bitmap darkenOriginalBitmap, RectF rect, boolean withLittleSquare) {
		return drawEditSquare(originalBitmap, darkenOriginalBitmap,
				(int) rect.left, (int) rect.top, (int) rect.right,
				(int) rect.bottom, withLittleSquare);
	}

	// This function is used to darken a given image.
	// Basically, it just add a fixed value to each channel of the image.
	public static Bitmap generateDarkenImage(Bitmap originalBitmap, int value) {
		Bitmap bitmap = originalBitmap;
		// Create buffer new bitmap
		Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.RGB_565);
		// Loop every pixel and add a value to each channel
		for (int x = 0; x < bitmap.getWidth(); x++) {
			for (int y = 0; y < bitmap.getHeight(); y++) {
				int pixel = bitmap.getPixel(x, y);
				int A = Color.alpha(pixel);
				int R = Color.red(pixel);
				int G = Color.green(pixel);
				int B = Color.blue(pixel);
				// Make sure every channel in new pixel is less than 255 and
				// large than 0
				if (value > 0) {
					R = Math.min(255, R + value);
					G = Math.min(255, G + value);
					B = Math.min(255, B + value);
				} else {
					R = Math.max(0, R + value);
					G = Math.max(0, G + value);
					B = Math.max(0, B + value);
				}
				newBitmap.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		return newBitmap;
	}

	// This function copies a part of a bitmap to another bitmap.
	private static void copySubImage(Bitmap sourceImage, Bitmap targetImage,
			int upLeftX, int upLeftY, int width, int height) {
		// Guarantee the coordinate is in the boundary
		int startX = Math.max(0, upLeftX);
		int endX = Math.min(sourceImage.getWidth() - 1, upLeftX + width);
		int startY = Math.max(0, upLeftY);
		int endY = Math.min(sourceImage.getHeight() - 1, upLeftY + height);

		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				int pixel = sourceImage.getPixel(x, y);
				targetImage.setPixel(x, y, pixel);
			}
		}
		return;
	}
}

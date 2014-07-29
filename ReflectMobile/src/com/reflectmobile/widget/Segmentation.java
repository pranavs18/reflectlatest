package com.reflectmobile.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

public class Segmentation {
	private Bitmap originalBitmap;
	private int lineColor = Color.WHITE;
	private int dotColor = Color.YELLOW;
	private int squareColor = Color.GREEN;
	private int width;
	private int height;

	public Segmentation(Bitmap bitmap) {
		this.originalBitmap = bitmap;
		this.width = originalBitmap.getWidth();
		this.height = originalBitmap.getHeight();
	}

	public Bitmap segmentation(int x, int y, int threshold) {
		FloodFiller floodFiller = new FloodFiller(originalBitmap);
		floodFiller.setTolerance(threshold);
		floodFiller.floodFill(x, y);

		Bitmap resultBitmap = floodFiller.getImage();
		ArrayList<Point> borderPointList = floodFiller.getBorderPointList();

		// Convex hull
		ArrayList<Point> convevHullPointList = findConvexHull(borderPointList);

		// Normolize point list
		normilizePointList(resultBitmap, convevHullPointList);

		// Draw square
//		drawSquare(resultBitmap, convevHullPointList);

		// Draw lines
		drawLines(resultBitmap, convevHullPointList);

		// Draw dots
//		drawDots(resultBitmap, convevHullPointList);

		ArrayList<Point> touchPointList = new ArrayList<Point>();
		touchPointList.add(new Point(x, y));
		drawDots(resultBitmap, touchPointList);

		// Save
		// saveToDisk(resultBitmap, "Test");
		return resultBitmap;
	}

	public ArrayList<Point> findConvexHull(ArrayList<Point> points) {
		@SuppressWarnings("unchecked")
		ArrayList<Point> xSorted = (ArrayList<Point>) points.clone();
		Collections.sort(xSorted, new Comparator<Point>() {

			@Override
			public int compare(Point lhs, Point rhs) {
				return lhs.x - rhs.x;
			}
		});

		int n = xSorted.size();

		Point[] lUpper = new Point[n];

		lUpper[0] = xSorted.get(0);
		lUpper[1] = xSorted.get(1);

		int lUpperSize = 2;

		for (int i = 2; i < n; i++) {
			lUpper[lUpperSize] = xSorted.get(i);
			lUpperSize++;

			while (lUpperSize > 2
					&& !rightTurn(lUpper[lUpperSize - 3],
							lUpper[lUpperSize - 2], lUpper[lUpperSize - 1])) {
				// Remove the middle point of the three last
				lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
				lUpperSize--;
			}
		}

		Point[] lLower = new Point[n];

		lLower[0] = xSorted.get(n - 1);
		lLower[1] = xSorted.get(n - 2);

		int lLowerSize = 2;

		for (int i = n - 3; i >= 0; i--) {
			lLower[lLowerSize] = xSorted.get(i);
			lLowerSize++;

			while (lLowerSize > 2
					&& !rightTurn(lLower[lLowerSize - 3],
							lLower[lLowerSize - 2], lLower[lLowerSize - 1])) {
				// Remove the middle point of the three last
				lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
				lLowerSize--;
			}
		}

		ArrayList<Point> result = new ArrayList<Point>();

		for (int i = 0; i < lUpperSize; i++) {
			result.add(lUpper[i]);
		}

		for (int i = 1; i < lLowerSize - 1; i++) {
			result.add(lLower[i]);
		}

		return result;
	}

	private boolean rightTurn(Point a, Point b, Point c) {
		return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) > 0;
	}

	/*
	 * Make sure the point is in the border of the image
	 */
	private void normilizePointList(Bitmap bitmap, ArrayList<Point> pointList) {
		int offset = 10;
		for (Point point : pointList) {
			point.x = Math.min(bitmap.getWidth() - offset,
					Math.max(offset, point.x));
			point.y = Math.min(bitmap.getHeight() - offset,
					Math.max(offset, point.y));
		}

	}

	private void drawLines(Bitmap bitmap, ArrayList<Point> pointList) {
		if (pointList.size() <= 3) {
			return;
		}
		Paint paint = new Paint();
		paint.setColor(lineColor);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(6);
		Canvas canvas = new Canvas(bitmap);

		Point prevPoint = pointList.get(0);
		for (int i = 1; i <= pointList.size() - 1; i++) {
			Point currentPoint = pointList.get(i);
			canvas.drawLine(prevPoint.x, prevPoint.y, currentPoint.x,
					currentPoint.y, paint);
			prevPoint = currentPoint;
		}
		canvas.drawLine(prevPoint.x, prevPoint.y, pointList.get(0).x,
				pointList.get(0).y, paint);
		return;
	}

	private void drawDots(Bitmap bitmap, ArrayList<Point> pointList) {
		int littleSquareRadius = 6;
		Canvas canvas = new Canvas(bitmap);
		for (Point point : pointList) {
			int x = Math.min(bitmap.getWidth() - littleSquareRadius,
					Math.max(littleSquareRadius, point.x));
			int y = Math.min(bitmap.getHeight() - littleSquareRadius,
					Math.max(littleSquareRadius, point.y));
			// Generate brush for draw little square border
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.FILL);
			// Draw the little square border
			RectF rect = new RectF(x - littleSquareRadius - 2, y
					- littleSquareRadius - 2, x + littleSquareRadius + 2, y
					+ littleSquareRadius + 2);
			canvas.drawRoundRect(rect, 2, 2, paint);

			// Generate brush for draw little square
			paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.FILL);
			// Draw the little square
			rect = new RectF(x - littleSquareRadius, y - littleSquareRadius, x
					+ littleSquareRadius, y + littleSquareRadius);
			canvas.drawRoundRect(rect, 2, 2, paint);
		}
		return;
	}

	private void drawSquare(Bitmap bitmap, ArrayList<Point> pointList) {
		// Calculate border
		int left = width;
		int right = 0;
		int top = height;
		int bottom = 0;
		for (Point point : pointList) {
			left = Math.min(left, point.x);
			right = Math.max(right, point.x);
			top = Math.min(top, point.y);
			bottom = Math.max(bottom, point.y);
		}

		Paint paint = new Paint();
		paint.setColor(squareColor);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(6);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawRect(left, top, right, bottom, paint);
		return;
	}
}

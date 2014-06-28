package com.reflectmobile.utility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class TagView extends View implements View.OnTouchListener {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TagView(Context context) {
        super(context);
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(5);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.setMeasuredDimension(100, 100);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(50, 50, 50, this.paint);
    }

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.d("myApp", "cercle");
		return false;
	}

}
package com.reflectmobile.utility;

import java.util.LinkedList;
import java.util.Queue;

import android.R.integer;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class HorizontalListView extends AdapterView<ListAdapter>{

	
	public boolean alwaysOverrideTouch = true;
	protected ListAdapter adapter;
	private int leftViewIndex = -1; // record the leftest child index
	private int rightViewIndex = 0; // record the righest child index
	protected int currentX;
	protected int nextX;
	private int maxX = Integer.MAX_VALUE;
	private int displayOffset = 0;
	protected Scroller scroller;
	private GestureDetector gestureDetector;
	private Queue<View> removedViewQueue = new LinkedList<View>();
	private OnItemSelectedListener onItemSelectListenered;
	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;
	private boolean isDataChanged = false;
	

	public HorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	private synchronized void initView() {
		leftViewIndex = -1;
		rightViewIndex = 0;
		displayOffset = 0;
		currentX = 0;
		nextX = 0;
		maxX = Integer.MAX_VALUE;
		scroller = new Scroller(getContext());
		gestureDetector = new GestureDetector(getContext(), onGestureListener);
	}
	
	@Override
	public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
		onItemSelectListenered = listener;
	}
	
	@Override
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
		onItemClickListener = listener;
	}
	
	@Override
	public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
		onItemLongClickListener = listener;
	}

	private DataSetObserver dataSetObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			synchronized(HorizontalListView.this){
				isDataChanged = true;
			}
			invalidate();
			requestLayout();
		}

		@Override
		public void onInvalidated() {
			reset();
			invalidate();
			requestLayout();
		}
		
	};

	@Override
	public ListAdapter getAdapter() {
		return this.adapter;
	}

	@Override
	public View getSelectedView() {
		//TODO: implement
		return null;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if(this.adapter != null) {
			this.adapter.unregisterDataSetObserver(dataSetObserver);
		}
		this.adapter = adapter;
		this.adapter.registerDataSetObserver(dataSetObserver);
		reset();
	}
	
	/*
	 * Reset the listview, called when the dataset changed
	 */
	private synchronized void reset(){
		initView();
		removeAllViewsInLayout();
        requestLayout();
	}

	@Override
	public void setSelection(int position) {
		//TODO: implement
	}
	
	
	//add child to the view, this method is called in the fillList method
	@SuppressWarnings("deprecation")
	private void addAndMeasureChild(final View child, int viewPos) {
		LayoutParams params = child.getLayoutParams();
		if(params == null) {
			params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}

		addViewInLayout(child, viewPos, params, true);
		child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}
	
	

	@Override
	protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if(this.adapter == null){
			return;
		}
		
		// if data changed, initialize the view
		if(isDataChanged){
			int oldCurrentX = currentX;
			initView();
			removeAllViewsInLayout();
			nextX = oldCurrentX;
			isDataChanged = false;
		}
		
		// get new horizontal scroll bar location
		if(scroller.computeScrollOffset()){
			int scrollx = scroller.getCurrX();
			nextX = scrollx;
		}
		
		// scroll bar hit left end, stop scroll
		if(nextX <= 0){
			nextX = 0;
			scroller.forceFinished(true);
		}
		
		// scroll bar hit right end, stop scroll
		if(nextX >= maxX) {
			nextX = maxX;
			scroller.forceFinished(true);
		}
		
		int dx = currentX - nextX;
		
		// re-draw the view based on the offset
		// 1. remove the non visible item
		// 2. add new items  (child.measure is called in the addAndMeasure..)
		// 3. position items (child.layout is called in the positionItems)
		// https://developer.android.com/guide/topics/ui/how-android-draws.html
		removeNonVisibleItems(dx);
		fillList(dx);
		positionItems(dx);
		
		currentX = nextX;
		
		if(!scroller.isFinished()){
			post(new Runnable(){
				@Override
				public void run() {
					requestLayout();
				}
			});
			
		}
	}
	
	/*
	 * Add new items to the listview, including two parts, fillLeft, fillRight
	 */
	private void fillList(final int dx) {
		int edge = 0;
		View child = getChildAt(getChildCount()-1);
		if(child != null) {
			edge = child.getRight();
		}
		fillListRight(edge, dx);
		
		edge = 0;
		child = getChildAt(0);
		if(child != null) {
			edge = child.getLeft();
		}
		fillListLeft(edge, dx);
		
		
	}
	
	private void fillListRight(int rightEdge, final int dx) {
		while(rightEdge + dx < getWidth() && rightViewIndex < this.adapter.getCount()) {
			
			View child = this.adapter.getView(rightViewIndex, removedViewQueue.poll(), this);
			addAndMeasureChild(child, -1);
			rightEdge += child.getMeasuredWidth();
			
			if(rightViewIndex == this.adapter.getCount()-1) {
				maxX = currentX + rightEdge - getWidth();
			}
			
			if (maxX < 0) {
				maxX = 0;
			}
			rightViewIndex++;
		}
		
	}
	
	private void fillListLeft(int leftEdge, final int dx) {
		while(leftEdge + dx > 0 && leftViewIndex >= 0) {
			View child = this.adapter.getView(leftViewIndex, removedViewQueue.poll(), this);
			addAndMeasureChild(child, 0);
			leftEdge -= child.getMeasuredWidth();
			leftViewIndex--;
			displayOffset -= child.getMeasuredWidth();
		}
	}
	
	/*
	 * Remove non visible list items during scroling 
	 */
	private void removeNonVisibleItems(final int dx) {
		View child = getChildAt(0);
		while(child != null && child.getRight() + dx <= 0) {
			displayOffset += child.getMeasuredWidth();
			removedViewQueue.offer(child);
			removeViewInLayout(child);
			leftViewIndex++;
			child = getChildAt(0);
			
		}
		
		child = getChildAt(getChildCount()-1);
		while(child != null && child.getLeft() + dx >= getWidth()) {
			removedViewQueue.offer(child);
			removeViewInLayout(child);
			rightViewIndex--;
			child = getChildAt(getChildCount()-1);
		}
	}
	
	// Re-drew the list view items after measuer()
	private void positionItems(final int dx) {
		if(getChildCount() > 0){
			displayOffset += dx;
			int left = displayOffset;
			for(int i=0;i<getChildCount();i++){
				View child = getChildAt(i);
				int childWidth = child.getMeasuredWidth();
				child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
				left += childWidth + child.getPaddingRight();
			}
		}
	}
	
	public synchronized void scrollTo(int x) {
		scroller.startScroll(nextX, 0, x - nextX, 0);
		requestLayout();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean handled = super.dispatchTouchEvent(ev);
		handled |= gestureDetector.onTouchEvent(ev);
		return handled;
	}
	
	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
		synchronized(HorizontalListView.this){
			scroller.fling(nextX, 0, (int)-velocityX, 0, 0, maxX, 0, 0);
		}
		requestLayout();
		
		return true;
	}
	
	protected boolean onDown(MotionEvent e) {
		scroller.forceFinished(true);
		return true;
	}
	
	
	/*
	 * GestureListener for the listview
	 */
	private OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDown(MotionEvent e) {
			return HorizontalListView.this.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			
			synchronized(HorizontalListView.this){
				nextX += (int)distanceX;
			}
			requestLayout();
			
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			for(int i=0;i<getChildCount();i++){
				View child = getChildAt(i);
				if (isEventWithinView(e, child)) {
					if(onItemClickListener != null){
						onItemClickListener.onItemClick(HorizontalListView.this, child, leftViewIndex + 1 + i, adapter.getItemId( leftViewIndex + 1 + i ));
					}
					if(onItemSelectListenered != null){
						onItemSelectListenered.onItemSelected(HorizontalListView.this, child, leftViewIndex + 1 + i, adapter.getItemId( leftViewIndex + 1 + i ));
					}
					break;
				}
				
			}
			return true;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				if (isEventWithinView(e, child)) {
					if (onItemLongClickListener != null) {
						onItemLongClickListener.onItemLongClick(HorizontalListView.this, child, leftViewIndex + 1 + i, adapter.getItemId(leftViewIndex + 1 + i));
					}
					break;
				}

			}
		}
		
		/*
		 * Check which list items is on focus
		 */
		private boolean isEventWithinView(MotionEvent e, View child) {
            Rect viewRect = new Rect();
            int[] childPosition = new int[2];
            child.getLocationOnScreen(childPosition);
            int left = childPosition[0];
            int right = left + child.getWidth();
            int top = childPosition[1];
            int bottom = top + child.getHeight();
            viewRect.set(left, top, right, bottom);
            return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
        }
	};
}

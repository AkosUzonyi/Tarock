package com.tisza.tarock.gui;
import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.animation.*;
import android.widget.*;

import com.tisza.tarock.*;

public class PlacedCardView extends ImageView
{
	private int orientation;

	public PlacedCardView(Context context, int width, int orientation)
	{
		super(context);
		this.orientation = orientation;
		setImageResource(R.drawable.testcard);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		setLayoutParams(lp);
	}

	public PlacedCardView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlacedCardView, 0, 0);
		
		try
		{
			orientation = a.getInteger(R.styleable.PlacedCardView_orientation, 0);
		}
		finally
		{
			a.recycle();
		}
	}
	
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		clearAnimation();
		
		AnimationSet animSet = new AnimationSet(true);
		animSet.setDuration(0);
		animSet.setFillAfter(true);	
		
		Animation a = new RotateAnimation(0, orientation * 90, w / 2, h / 2);
		animSet.addAnimation(a);
		
		float tx = 0;
		float ty = 0;
		if (orientation == 0)
		{
			ty = 1;
		}
		else if (orientation == 1)
		{
			tx = 1;
		}
		else if (orientation == 2)
		{
			ty = -1;
		}
		else if (orientation == 3)
		{
			tx = -1;
		}
		tx *= h / 4;
		ty *= h / 4;
		a = new TranslateAnimation(0, tx, 0, ty);
		animSet.addAnimation(a);
		
		startAnimation(animSet);
	}
}
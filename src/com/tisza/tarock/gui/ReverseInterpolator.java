package com.tisza.tarock.gui;

import android.view.animation.*;

public class ReverseInterpolator implements Interpolator
{
	public static final ReverseInterpolator instance = new ReverseInterpolator();
	
	private ReverseInterpolator(){}
	
	public float getInterpolation(float input)
	{
		return input - 1;
	}
}

package com.tisza.tarock.gui;

import android.view.animation.Interpolator;

public class ReverseInterpolator implements Interpolator
{
	public float getInterpolation(float input)
	{
		return 1 - input;
	}
}

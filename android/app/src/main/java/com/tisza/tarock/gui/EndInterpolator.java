package com.tisza.tarock.gui;

import android.view.animation.*;

public class EndInterpolator implements Interpolator
{
	@Override
	public float getInterpolation(float input)
	{
		return 1;
	}
}

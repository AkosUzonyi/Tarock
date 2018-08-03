package com.tisza.tarock.gui;

import android.content.*;
import android.support.v4.view.*;
import android.view.*;
import com.tisza.tarock.*;

public class CenterViewPagerAdapter extends PagerAdapter
{
	private final Context context;
	private final View[] views;
	private final int[] titles;

	public CenterViewPagerAdapter(Context context, View[] views, int[] titles)
	{
		if (views.length != titles.length)
			throw new IllegalArgumentException();

		this.context = context;
		this.views = views;
		this.titles = titles;
	}

	@Override
	public int getCount()
	{
		return views.length;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		View view = views[position];
		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		container.removeView((View)object);
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		return context.getResources().getString(titles[position]);
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view == object;
	}
}

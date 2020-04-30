package com.tisza.tarock.gui;

import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.tisza.tarock.*;

import java.util.*;

public class StatisticsPointsAdapter extends RecyclerView.Adapter<StatisticsPointsAdapter.ViewHolder>
{
	private List<String> names = new ArrayList<>();
	private List<Integer> points = new ArrayList<>();

	public void setNames(List<String> names)
	{
		this.names.clear();
		this.names.addAll(names);
		notifyDataSetChanged();
	}

	public void setPoints(List<Integer> points)
	{
		this.points.clear();
		this.points.addAll(points);
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount()
	{
		return names.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.statistics_points_entry, parent, false);

		ViewHolder holder = new ViewHolder(view);
		holder.nameView = view.findViewById(R.id.statistics_points_name);
		holder.pointsView = view.findViewById(R.id.statistics_points_value);

		return holder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		holder.nameView.setText(names.get(position));
		holder.pointsView.setText(String.valueOf(position < points.size() ? points.get(position) : 0));
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public TextView nameView;
		public TextView pointsView;

		public ViewHolder(View view)
		{
			super(view);
		}
	}
}

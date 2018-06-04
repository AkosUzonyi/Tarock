package com.tisza.tarock.gui;

import android.content.*;
import android.view.*;
import android.widget.*;
import com.tisza.tarock.*;

import java.util.*;

import static com.tisza.tarock.gui.UltimoSelector.*;

public class UltimoViewManager
{
	private final Context context;
	private final LayoutInflater inflater;
	private final LinearLayout spinnerListView;
	private final UltimoSelector ultimoSelector = new UltimoSelector();

	public UltimoViewManager(Context context, LayoutInflater inflater, LinearLayout spinnerListView)
	{
		this.context = context;
		this.inflater = inflater;
		this.spinnerListView = spinnerListView;
	}

	public void takeAnnouncements(Collection<Announcement> announcements)
	{
		ultimoSelector.takeAnnouncements(announcements);
		spinnerListView.removeAllViews();
		updateSpinners();
	}

	private void selectProperty(int position, UltimoProperty property)
	{
		if (ultimoSelector.selectProperty(position, property))
		{
			int removeStartIndex = position + 1;
			int removeCount = spinnerListView.getChildCount() - removeStartIndex;
			spinnerListView.removeViews(removeStartIndex, removeCount);

			updateSpinners();
		}
	}

	private void updateSpinners()
	{
		int spinnerPosition = spinnerListView.getChildCount();
		if (spinnerPosition < ultimoSelector.getAvailableProperties().size())
		{
			List<UltimoProperty> items = ultimoSelector.getAvailableProperties().get(spinnerPosition);

			Spinner spinner = (Spinner)inflater.inflate(R.layout.ultimo_spinner, spinnerListView, false);
			ArrayAdapter<UltimoProperty> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int itemPosition, long id)
				{
					selectProperty(spinnerPosition, items.get(itemPosition));
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent)
				{
					selectProperty(spinnerPosition, null);
				}
			});
			spinnerListView.addView(spinner);
		}
	}

	public boolean hasAnyUltimo()
	{
		return ultimoSelector.hasAnyUltimo();
	}

	public Announcement getCurrentSelectedAnnouncement()
	{
		return ultimoSelector.getCurrentSelectedAnnouncement();
	}
}

package com.tisza.tarock.gui.fragment;

import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import com.tisza.tarock.*;

public class DonationFragment extends MainActivityFragment
{
	public static final String TAG = "donate";

	private static final String donationURL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=UY3YJEJCFUXA8&source=url";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.donate, container, false);
		View donateButton = view.findViewById(R.id.donate_button);
		donateButton.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(donationURL))));
		return view;
	}
}

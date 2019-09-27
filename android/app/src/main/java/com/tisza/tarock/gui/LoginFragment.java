package com.tisza.tarock.gui;

import android.os.*;
import android.view.*;
import android.widget.*;
import com.facebook.*;
import com.facebook.login.widget.*;
import com.google.firebase.iid.*;
import com.tisza.tarock.R;

import java.io.*;

public class LoginFragment extends MainActivityFragment
{
	private Button playButton;

	private final AccessTokenTracker accessTokenTracker = new AccessTokenTracker()
	{
		@Override
		protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
		{
			if (playButton != null)
				playButton.setEnabled(currentAccessToken != null);

			if (currentAccessToken == null)
			{
				getMainActivity().disconnect();
				new FCMDeleteTokenAsyncTask().execute();
			}
		}
	};

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.login, container, false);

		playButton = view.findViewById(R.id.play_button);
		playButton.setOnClickListener(v -> getMainActivity().onPlayButtonClicked());
		playButton.setEnabled(AccessToken.getCurrentAccessToken() != null);
		accessTokenTracker.startTracking();

		LoginButton loginButton = view.findViewById(R.id.fb_login_button);
		loginButton.setPermissions("public_profile, user_friends");

		return view;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		accessTokenTracker.stopTracking();
	}

	private class FCMDeleteTokenAsyncTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... voids)
		{
			try
			{
				FirebaseInstanceId.getInstance().deleteInstanceId();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}
}

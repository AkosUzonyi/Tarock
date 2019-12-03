package com.tisza.tarock.gui;

import android.app.*;
import android.os.*;
import androidx.lifecycle.*;
import com.facebook.*;
import com.google.firebase.iid.*;

import java.io.*;

public class LoginViewModel extends AndroidViewModel
{
	private MutableLiveData<LoginState> loginState = new MutableLiveData<>();
	private MutableLiveData<AccessToken> fbAccessToken = new MutableLiveData<>();

	private final AccessTokenTracker accessTokenTracker = new AccessTokenTracker()
	{
		@Override
		protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
		{
			fbAccessToken.setValue(currentAccessToken);
			loginState.setValue(currentAccessToken == null ? LoginState.LOGGED_OUT : LoginState.LOGGED_IN);

			if (currentAccessToken == null)
				new FCMDeleteTokenAsyncTask().execute();
		}
	};

	public LoginViewModel(Application application)
	{
		super(application);
		fbAccessToken.setValue(AccessToken.getCurrentAccessToken());
		loginState.setValue(AccessToken.getCurrentAccessToken() == null ? LoginState.LOGGED_OUT : LoginState.LOGGED_IN);
		accessTokenTracker.startTracking();
	}

	public LiveData<LoginState> getLoginState()
	{
		return loginState;
	}

	public LiveData<AccessToken> getFbAccessToken()
	{
		return fbAccessToken;
	}

	@Override
	protected void onCleared()
	{
		super.onCleared();
		accessTokenTracker.stopTracking();
	}

	public enum LoginState
	{
		LOGGED_IN, LOGGED_OUT;
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

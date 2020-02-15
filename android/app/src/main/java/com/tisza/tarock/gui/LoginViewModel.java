package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.os.*;
import androidx.lifecycle.*;
import com.facebook.*;
import com.facebook.login.*;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.tasks.*;
import com.google.firebase.iid.*;
import com.tisza.tarock.R;

import java.io.*;

public class LoginViewModel extends AndroidViewModel
{
	private LiveData<LoginState> loginState;
	private LiveData<String> loginName;
	private LiveData<Profile> fbProfile = new FbProfileLiveData();
	private LiveData<AccessToken> fbAccessToken = new FbAccessTokenLiveData();
	private MutableLiveData<GoogleSignInAccount> googleAccount = new MutableLiveData<>();

	private GoogleSignInClient googleSignInClient;

	public LoginViewModel(Application application)
	{
		super(application);

		loginState =
			Transformations.switchMap(fbAccessToken, fbAccessTokenValue ->
			Transformations.map(googleAccount, googleAccountValue ->
			{
				if (fbAccessTokenValue != null)
					return LoginState.FACEBOOK;

				if (googleAccountValue != null)
					return LoginState.GOOGLE;

				return LoginState.LOGGED_OUT;
			}));

		loginState.observeForever(this::onLoginStateChanged);

		loginName = Transformations.switchMap(loginState, loginStateValue ->
		{
			switch (loginStateValue)
			{
				case LOGGED_OUT: return new MutableLiveData<>(null);
				case FACEBOOK: return Transformations.map(fbProfile, profile -> profile == null ? null : profile.getName());
				case GOOGLE: return Transformations.map(googleAccount, account -> account == null ? null : account.getDisplayName());
			}

			throw new IllegalArgumentException("unknown login state: " + loginStateValue);
		});

		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(application.getString(R.string.google_server_client_id))
				.build();
		googleSignInClient = GoogleSignIn.getClient(application, gso);
		googleAccount.setValue(GoogleSignIn.getLastSignedInAccount(application));
	}

	public LiveData<LoginState> getLoginState()
	{
		return loginState;
	}

	public LiveData<String> getLoginName()
	{
		return loginName;
	}

	public LiveData<AccessToken> getFbAccessToken()
	{
		return fbAccessToken;
	}

	public LiveData<GoogleSignInAccount> getGoogleAccount()
	{
		return googleAccount;
	}

	public Intent getGoogleLoginIntent()
	{
		return googleSignInClient.getSignInIntent();
	}

	public void googleLoginResult(int resultCode, Intent intent)
	{
		Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
		try
		{
			googleAccount.setValue(task.getResult(ApiException.class));
		}
		catch (ApiException e)
		{
			googleAccount.setValue(null);
			e.printStackTrace();
		}
	}

	private void onLoginStateChanged(LoginState loginStateValue)
	{
		new FCMDeleteTokenAsyncTask().execute();
	}

	public void logOut()
	{
		googleSignInClient.signOut().addOnCompleteListener(result -> googleAccount.setValue(null));
		LoginManager.getInstance().logOut();
	}

	public enum LoginState
	{
		LOGGED_OUT, FACEBOOK, GOOGLE;
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

	private class FbAccessTokenLiveData extends LiveData<AccessToken>
	{
		private final AccessTokenTracker accessTokenTracker = new AccessTokenTracker()
		{
			@Override
			protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
			{
				setValue(currentAccessToken);
			}
		};

		@Override
		protected void onActive()
		{
			setValue(AccessToken.getCurrentAccessToken());
			accessTokenTracker.startTracking();
		}

		@Override
		protected void onInactive()
		{
			accessTokenTracker.stopTracking();
		}
	}

	private class FbProfileLiveData extends LiveData<Profile>
	{
		private final ProfileTracker facebookProfileTracker = new ProfileTracker()
		{
			@Override
			protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile)
			{
				setValue(currentProfile);
			}
		};

		@Override
		protected void onActive()
		{
			setValue(Profile.getCurrentProfile());
			facebookProfileTracker.startTracking();
		}

		@Override
		protected void onInactive()
		{
			facebookProfileTracker.stopTracking();
		}
	}
}

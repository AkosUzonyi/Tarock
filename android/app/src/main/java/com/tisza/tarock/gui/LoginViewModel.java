package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.os.*;
import androidx.lifecycle.*;
import com.facebook.*;
import com.facebook.login.*;
import com.google.android.gms.auth.api.*;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.tasks.*;
import com.tisza.tarock.R;

public class LoginViewModel extends AndroidViewModel
{
	private LiveData<LoginState> loginState;
	private LiveData<String> loginName;
	private LiveData<Profile> fbProfile = new FbProfileLiveData();
	private LiveData<AccessToken> fbAccessToken = new FbAccessTokenLiveData();
	private MutableLiveData<GoogleSignInAccount> googleAccount = new MutableLiveData<>();

	private final GoogleApiClient googleApiClient;
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
		googleApiClient = new GoogleApiClient.Builder(application)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();

		new GoogleSilentSignInAsyncTask().execute();
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

	public void refresh()
	{
		new GoogleSilentSignInAsyncTask().execute();
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

	public void logOut()
	{
		googleSignInClient.signOut().addOnCompleteListener(result -> googleAccount.setValue(null));
		LoginManager.getInstance().logOut();
	}

	public enum LoginState
	{
		LOGGED_OUT, FACEBOOK, GOOGLE;
	}

	private class GoogleSilentSignInAsyncTask extends AsyncTask<Void, Void, GoogleSignInAccount>
	{
		@Override
		protected GoogleSignInAccount doInBackground(Void... voids)
		{
			try
			{
				ConnectionResult connectionResult = googleApiClient.blockingConnect();
				if (!connectionResult.isSuccess())
					return null;

				GoogleSignInResult signInResult = Auth.GoogleSignInApi.silentSignIn(googleApiClient).await();
				if (!signInResult.isSuccess())
					return null;

				return signInResult.getSignInAccount();
			}
			finally
			{
				googleApiClient.disconnect();
			}
		}

		@Override
		protected void onPostExecute(GoogleSignInAccount googleSignInAccount)
		{
			googleAccount.setValue(googleSignInAccount);
		}
	}

	private static class FbAccessTokenLiveData extends LiveData<AccessToken>
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

	private static class FbProfileLiveData extends LiveData<Profile>
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

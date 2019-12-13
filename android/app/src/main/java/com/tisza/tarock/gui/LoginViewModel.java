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
	private MediatorLiveData<LoginState> loginState = new MediatorLiveData<>();
	private MutableLiveData<AccessToken> fbAccessToken = new MutableLiveData<>();
	private MutableLiveData<GoogleSignInAccount> googleAccount = new MutableLiveData<>();

	private GoogleSignInClient googleSignInClient;

	private final AccessTokenTracker accessTokenTracker = new AccessTokenTracker()
	{
		@Override
		protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
		{
			fbAccessToken.setValue(currentAccessToken);
		}
	};

	public LoginViewModel(Application application)
	{
		super(application);

		loginState.setValue(LoginState.LOGGED_OUT);
		loginState.addSource(fbAccessToken, accessToken ->
		{
			if (accessToken != null && loginState.getValue() == LoginState.LOGGED_OUT)
				loginState.setValue(LoginState.FACEBOOK);
			if (accessToken == null && loginState.getValue() == LoginState.FACEBOOK)
				loginState.setValue(LoginState.LOGGED_OUT);
		});
		loginState.addSource(googleAccount, account ->
		{
			if (account != null && loginState.getValue() == LoginState.LOGGED_OUT)
				loginState.setValue(LoginState.GOOGLE);
			if (account == null && loginState.getValue() == LoginState.GOOGLE)
				loginState.setValue(LoginState.LOGGED_OUT);
		});
		loginState.observeForever(this::onLoginStateChanged);

		fbAccessToken.setValue(AccessToken.getCurrentAccessToken());
		accessTokenTracker.startTracking();

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

	@Override
	protected void onCleared()
	{
		super.onCleared();
		accessTokenTracker.stopTracking();
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
}

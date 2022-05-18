package com.tisza.tarock.gui.viewmodel;

import android.app.*;
import androidx.lifecycle.*;
import com.facebook.*;
import com.google.android.gms.auth.api.signin.*;
import com.tisza.tarock.api.*;
import com.tisza.tarock.api.model.*;
import io.reactivex.android.schedulers.*;

public class AuthenticationViewModel extends AndroidViewModel
{
	private MutableLiveData<LoginState> connectionState = new MutableLiveData<>(LoginState.LOGGED_OUT);
	private MutableLiveData<User> user = new MutableLiveData<>(null);

	private APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);

	public AuthenticationViewModel(Application application)
	{
		super(application);
	}

	public LiveData<LoginState> getLoginState()
	{
		return connectionState;
	}

	public LiveData<User> getLoggedInUser()
	{
		return user;
	}

	public void login()
	{
		switch (connectionState.getValue())
		{
			case LOGGING_IN:
				break;
			case LOGGED_IN:
				connectionState.setValue(LoginState.LOGGED_IN);
				break;
			case LOGGED_OUT:
				connectionState.setValue(LoginState.LOGGING_IN);
				AccessToken facebookToken = AccessToken.getCurrentAccessToken();
				GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(getApplication());
				LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
				if (facebookToken != null)
				{
					loginRequestDTO.provider = "facebook";
					loginRequestDTO.token = facebookToken.getToken();
				}
				if (googleAccount != null)
				{
					loginRequestDTO.provider = "google";
					loginRequestDTO.token = googleAccount.getIdToken();
				}

				apiInterface.login(loginRequestDTO).doOnError(error -> logout()).subscribe(loginResponseDTO ->
				{
					user.setValue(loginResponseDTO.user);
					AuthInterceptor.authToken = loginResponseDTO.token;
					connectionState.setValue(LoginState.LOGGED_IN);
				});
				break;
		}
	}

	public void logout()
	{
		user.setValue(null);
		AuthInterceptor.authToken = null;
		connectionState.setValue(LoginState.LOGGED_OUT);
	}

	public enum LoginState
	{
		LOGGED_OUT, LOGGING_IN, LOGGED_IN;
	}
}

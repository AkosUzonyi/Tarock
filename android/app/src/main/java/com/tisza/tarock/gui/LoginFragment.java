package com.tisza.tarock.gui;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.lifecycle.*;
import com.facebook.login.widget.*;
import com.google.android.gms.common.*;
import com.tisza.tarock.R;

public class LoginFragment extends MainActivityFragment
{
	public static final String TAG = "login";
	public static final int REQUEST_CODE_GOOGLE_LOGIN = 0;

	private LoginButton facebookButton;
	private SignInButton googleButton;
	private Button playButton, logoutButton;
	private ConnectionViewModel connectionViewModel;
	private LoginViewModel loginViewModel;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		connectionViewModel = ViewModelProviders.of(getActivity()).get(ConnectionViewModel.class);
		loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.login, container, false);

		facebookButton = view.findViewById(R.id.fb_login_button);
		googleButton = view.findViewById(R.id.google_login_button);
		playButton = view.findViewById(R.id.play_button);
		logoutButton = view.findViewById(R.id.logout_button);

		facebookButton.setPermissions("public_profile");
		googleButton.setOnClickListener(v -> startActivityForResult(loginViewModel.getGoogleLoginIntent(), REQUEST_CODE_GOOGLE_LOGIN));
		playButton.setOnClickListener(v -> connectionViewModel.login());
		logoutButton.setOnClickListener(v -> loginViewModel.logOut());

		loginViewModel.getLoginState().observe(this, loginState ->
		{
			boolean loggedIn = loginState != LoginViewModel.LoginState.LOGGED_OUT;
			facebookButton.setVisibility(!loggedIn ? View.VISIBLE : View.GONE);
			googleButton.setVisibility(!loggedIn ? View.VISIBLE : View.GONE);
			playButton.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
			logoutButton.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

			if (!loggedIn)
				connectionViewModel.disconnect();
		});

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_GOOGLE_LOGIN)
			loginViewModel.googleLoginResult(resultCode, data);
	}
}

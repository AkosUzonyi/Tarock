package com.tisza.tarock.gui;

import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.lifecycle.*;
import com.facebook.*;
import com.facebook.login.widget.*;
import com.tisza.tarock.R;

public class LoginFragment extends MainActivityFragment
{
	public static final String TAG = "login";

	private Button playButton;
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

		playButton = view.findViewById(com.tisza.tarock.R.id.play_button);
		playButton.setOnClickListener(v -> connectionViewModel.login());

		LoginButton loginButton = view.findViewById(R.id.fb_login_button);
		loginButton.setPermissions("public_profile, user_friends");

		loginViewModel.getLoginState().observe(this, loginState ->
		{
			playButton.setEnabled(loginState == LoginViewModel.LoginState.LOGGED_IN);

			if (loginState == LoginViewModel.LoginState.LOGGED_OUT)
				connectionViewModel.disconnect();
		});

		return view;
	}
}

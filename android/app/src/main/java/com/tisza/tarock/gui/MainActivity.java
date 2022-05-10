package com.tisza.tarock.gui;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.*;
import androidx.core.app.*;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.*;
import com.tisza.tarock.R;
import com.tisza.tarock.api.*;
import com.tisza.tarock.gui.adapter.*;
import com.tisza.tarock.gui.fragment.*;
import com.tisza.tarock.gui.misc.*;
import com.tisza.tarock.gui.viewmodel.*;
import io.reactivex.plugins.*;
import okhttp3.*;
import retrofit2.*;

import java.io.*;
import java.net.*;

public class MainActivity extends AppCompatActivity
{
	private static final int DISCONNECT_DELAY_SEC = 40;
	private static final int DOWNLOAD_CSV_REQUEST_CODE = 1;

	private final APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);;
	private AuthenticationViewModel authenticationViewModel;
	private ProgressDialog progressDialog;
	private Vibrator vibrator;

	private Handler handler;
	private Runnable disconnectRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(null);

		LocaleManager.updateLocale(this);
		setContentView(R.layout.main);

		authenticationViewModel = ViewModelProviders.of(this).get(AuthenticationViewModel.class);
		authenticationViewModel.getLoginState().observe(this, this::loginStateChanged);
		progressDialog = new ProgressDialog(this);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		handler = new Handler();
		//TODO: finish timeout
		//disconnectRunnable = connectionViewModel::disconnect;

		RxJavaPlugins.setErrorHandler(throwable ->
		{
			if (throwable instanceof Error)
				throw (Error) throwable;

			Exception exception = (Exception) throwable;
			handler.post(() -> handleError(exception));
		});

		LoginFragment loginFragment = new LoginFragment();
		getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, loginFragment, LoginFragment.TAG)
				.commit();

		if (getIntent().hasExtra(GameFragment.KEY_GAME_SESSION_ID))
			authenticationViewModel.login();
	}

	private void handleError(Exception exception)
	{
		if (exception instanceof HttpException)
		{
			HttpException httpException = (HttpException) exception;
			String errorBodyString = "";
			ResponseBody errorBody = httpException.response().errorBody();
			if (errorBody != null)
			{
				try
				{
					errorBodyString = errorBody.string();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			switch (httpException.code())
			{
				case 503:
					showErrorDialog(R.string.error_server_down_title, getString(R.string.error_server_down_message) + " (" + errorBodyString + ")");
					break;
				case 410:
					break;
				case 422:
					vibrator.vibrate(100);
					break;
				default:
					showErrorDialog(R.string.error_server_error_title, httpException.code() + " " + httpException.message() + "\n" + errorBodyString);
					break;
			}
		}
		else if (exception instanceof ConnectException)
		{
			authenticationViewModel.logout();
			showErrorDialog(R.string.error_server_down_title, getString(R.string.error_server_down_message) + " (" + exception.getMessage() + ")");
		}
		else
		{
			Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), exception);
		}
	}

	public void openSettings()
	{
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, new SettingsFragment(), SettingsFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	public void openDonationFragment()
	{
		DonationFragment donationFragment = new DonationFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, donationFragment, DonationFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	private void loginStateChanged(AuthenticationViewModel.LoginState loginState)
	{
		switch (loginState)
		{
			case LOGGED_OUT:
				popBackToLoginScreen();
				break;
			case LOGGING_IN:
				progressDialog.setMessage(getResources().getString(R.string.logging_in));
				progressDialog.show();
				break;
			case LOGGED_IN:
				onSuccessfulLogin();
				break;
		}
	}

	private void showErrorDialog(int title, int message)
	{
		showErrorDialog(title, getString(message));
	}

	private void showErrorDialog(int title, String message)
	{
		popBackToLoginScreen();

		new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setNeutralButton(R.string.ok, (dialog, which) -> {})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}

	private void onSuccessfulLogin()
	{
		Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		if (currentFragment != null && !LoginFragment.TAG.equals(currentFragment.getTag()))
			return;

		popBackToLoginScreen();

		GameListFragment gameListFragment = new GameListFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, gameListFragment, GameListFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();

		String gameSessionID = getIntent().getStringExtra(GameFragment.KEY_GAME_SESSION_ID);
		if (gameSessionID != null)
		{
			getIntent().removeExtra(GameFragment.KEY_GAME_SESSION_ID);
			joinGameSession(Integer.parseInt(gameSessionID));
		}
	}

	public void createNewGame()
	{
		CreateGameFragment createGameFragment = new CreateGameFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, createGameFragment, CreateGameFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	public void joinGameSession(int gameSessionID)
	{
		GameFragment gameFragment = new GameFragment();

		Bundle args = new Bundle();
		args.putInt(GameFragment.KEY_GAME_SESSION_ID, gameSessionID);
		gameFragment.setArguments(args);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, gameFragment, GameFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	public void downloadCsv()
	{
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
		    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_CSV_REQUEST_CODE);
			return;
		}

		DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse("https://tarokk.net/cgi-bin/tarock/tarokk_pontok.csv?user_id=" + authenticationViewModel.getLoggedInUser().getValue()));
		downloadRequest.allowScanningByMediaScanner();
		downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "tarokk_pontok.csv");
		DownloadManager downloadmanager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
		downloadmanager.enqueue(downloadRequest);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if (requestCode == DOWNLOAD_CSV_REQUEST_CODE)
		{
			for (int grantResult : grantResults)
				if (grantResult != PackageManager.PERMISSION_GRANTED)
					return;

			downloadCsv();
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		handler.removeCallbacks(disconnectRunnable);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		handler.postDelayed(disconnectRunnable,  DISCONNECT_DELAY_SEC * 1000);
	}

	private void popBackToLoginScreen()
	{
		progressDialog.setOnDismissListener(null);
		if (progressDialog.isShowing())
			progressDialog.dismiss();

		Fragment loginFragment = getSupportFragmentManager().findFragmentByTag(LoginFragment.TAG);
		if (loginFragment == null)
			loginFragment = new LoginFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, loginFragment, LoginFragment.TAG)
				.commit();
	}

	private void requireUpdate()
	{
		new AlertDialog.Builder(this)
				.setTitle(R.string.update_available)
				.setMessage(R.string.update_please)
				.setPositiveButton(R.string.update_accept, (dialog, which) ->
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()))))
				.setNegativeButton(R.string.update_deny, (dialog, which) ->
						finish())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}
}

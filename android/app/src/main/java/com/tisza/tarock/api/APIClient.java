package com.tisza.tarock.api;

import com.tisza.tarock.BuildConfig;
import io.reactivex.plugins.*;
import io.reactivex.schedulers.*;
import me.linshen.retrofit2.adapter.*;
import okhttp3.*;
import retrofit2.*;
import retrofit2.HttpException;
import retrofit2.adapter.rxjava2.*;
import retrofit2.converter.gson.*;

import java.net.*;
import java.util.concurrent.*;

public class APIClient
{
	private static Retrofit retrofit = null;

	public static Retrofit getClient()
	{
		/*HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);*/
		OkHttpClient client = new OkHttpClient.Builder()
				.connectTimeout(5, TimeUnit.SECONDS)
				.readTimeout(0, TimeUnit.MILLISECONDS)
				.addInterceptor(new AuthInterceptor())
				.addInterceptor(new TimeoutInterceptor())
				.build();


		retrofit = new Retrofit.Builder()
				.baseUrl(BuildConfig.SERVER_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
				//.addCallAdapterFactory(new LiveDataCallAdapterFactory())
				.client(client)
				.build();

		return retrofit;
	}
}

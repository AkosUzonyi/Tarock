package com.tisza.tarock.api;

import io.reactivex.schedulers.*;
import okhttp3.*;
import retrofit2.*;
import retrofit2.adapter.rxjava2.*;
import retrofit2.converter.gson.*;

public class APIClient
{
	private static Retrofit retrofit = null;

	public static Retrofit getClient()
	{
		/*HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);*/
		OkHttpClient client = new OkHttpClient.Builder()/*.addInterceptor(interceptor)*/.build();


		retrofit = new Retrofit.Builder()
				.baseUrl("http://dell:8080/")
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
				.client(client)
				.build();

		return retrofit;
	}
}

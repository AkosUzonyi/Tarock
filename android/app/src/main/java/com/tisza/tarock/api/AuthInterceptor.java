package com.tisza.tarock.api;

import okhttp3.*;

import java.io.*;

public class AuthInterceptor implements Interceptor
{
	public static String authToken = ""; //TODO

	@Override
	public Response intercept(Chain chain) throws IOException
	{
		Request original = chain.request();

		Request request = original.newBuilder()
				.header("Authorization", "Bearer " + authToken)
				.build();

		return chain.proceed(request);
	}
}

package com.tisza.tarock.api;

import okhttp3.*;

import java.io.*;

public class TimeoutInterceptor implements Interceptor
{
	@Override
	public Response intercept(Chain chain) throws IOException
	{
		Request request = chain.request();
		Response response;
		while ((response = chain.proceed(request)).code() == 408);
		return response;
	}
}

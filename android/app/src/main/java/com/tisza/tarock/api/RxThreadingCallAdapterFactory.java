package com.tisza.tarock.api;

import io.reactivex.*;
import io.reactivex.android.schedulers.*;
import io.reactivex.schedulers.*;
import retrofit2.*;
import retrofit2.adapter.rxjava2.*;

import java.lang.annotation.*;
import java.lang.reflect.*;

public class RxThreadingCallAdapterFactory extends CallAdapter.Factory
{
	private final RxJava2CallAdapterFactory original;

	public RxThreadingCallAdapterFactory()
	{
		original = RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io());
	}

	public static CallAdapter.Factory create()
	{
		return new RxThreadingCallAdapterFactory();
	}

	@Override
	public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit)
	{
		return new RxCallAdapterWrapper(original.get(returnType, annotations, retrofit));
	}

	private static class RxCallAdapterWrapper<R> implements CallAdapter<R, Object>
	{
		private final CallAdapter<R, ?> wrapped;

		public RxCallAdapterWrapper(CallAdapter<R, ?> wrapped)
		{
			this.wrapped = wrapped;
		}

		@Override
		public Type responseType()
		{
			return wrapped.responseType();
		}

		@Override
		public Object adapt(Call<R> call)
		{
			Object wrappedAdaptped = wrapped.adapt(call);
			if (wrappedAdaptped instanceof Completable)
				return ((Completable) wrappedAdaptped).observeOn(AndroidSchedulers.mainThread());
			else
				return ((Observable) wrappedAdaptped).observeOn(AndroidSchedulers.mainThread());
		}
	}
}

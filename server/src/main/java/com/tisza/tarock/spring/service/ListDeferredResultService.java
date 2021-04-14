package com.tisza.tarock.spring.service;

import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.context.request.async.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

@Service
public class ListDeferredResultService<T>
{
	private final long TIMEOUT = 30 * 1000;
	private final Map<Integer, Collection<Request<T>>> requestsById = new ConcurrentHashMap<>();

	private Collection<Request<T>> getRequestList(int id)
	{
		return requestsById.computeIfAbsent(id, i -> Collections.synchronizedCollection(new ArrayList<>()));
	}

	public void notifyNewResult(int id)
	{
		Collection<Request<T>> requests = getRequestList(id);
		synchronized (requests)
		{
			Iterator<Request<T>> it = requests.iterator();
			while (it.hasNext())
			{
				Request<T> request = it.next();
				try
				{
					List<T> result = request.listSupplier.get();
					if (!result.isEmpty())
					{
						request.deferredResult.setResult(result);
						it.remove();
					}
				}
				catch (Exception e)
				{
					request.deferredResult.setErrorResult(e);
					it.remove();
				}
			}
		}
	}

	public Object getDeferredResult(int id, Supplier<List<T>> listSupplier)
	{
		DeferredResult<List<T>> deferredResult = new DeferredResult<>(TIMEOUT, new ResponseEntity<Void>(HttpStatus.REQUEST_TIMEOUT));

		CompletableFuture.runAsync(() -> {
			List<T> result = listSupplier.get();
			if (!result.isEmpty())
			{
				deferredResult.setResult(result);
			}
			else
			{
				Request<T> request = new Request<>();
				request.deferredResult = deferredResult;
				request.listSupplier = listSupplier;
				getRequestList(id).add(request);
			}
		});

		return deferredResult;
	}

	private static class Request<T>
	{
		private DeferredResult<List<T>> deferredResult;
		private Supplier<List<T>> listSupplier;
	}
}

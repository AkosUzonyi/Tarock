package com.tisza.tarock.spring.service;

import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.context.request.async.*;

import java.util.*;
import java.util.concurrent.*;

@Service
public class ListDeferredResultService<T>
{
	private final long TIMEOUT = 30 * 1000;
	private final Map<Integer, Collection<DeferredResult<List<T>>>> deferredResultsById = new ConcurrentHashMap<>();

	private Collection<DeferredResult<List<T>>> getDeferredResultList(int id)
	{
		return deferredResultsById.computeIfAbsent(id, i -> Collections.synchronizedCollection(new ArrayList<>()));
	}

	public void notifyNewResult(int id, T result)
	{
		Collection<DeferredResult<List<T>>> deferredResults = getDeferredResultList(id);
		synchronized (deferredResults)
		{
			for (DeferredResult<List<T>> deferredResult : deferredResults)
				deferredResult.setResult(Collections.singletonList(result));

			deferredResults.clear();
		}
	}

	public DeferredResult<List<T>> getDeferredResult(int id)
	{
		DeferredResult<List<T>> deferredResult = new DeferredResult<>(TIMEOUT, new ResponseEntity<Void>(HttpStatus.REQUEST_TIMEOUT));
		getDeferredResultList(id).add(deferredResult);
		return deferredResult;
	}
}

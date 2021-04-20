package com.tisza.tarock.spring.service;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;
import org.springframework.web.context.request.async.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

@Service
public class ListDeferredResultService<T>
{
	private final Map<Integer, Collection<Request<T>>> requestsById = new ConcurrentHashMap<>();

	@Autowired
	private PlatformTransactionManager transactionManager;

	private Collection<Request<T>> getRequestList(int id)
	{
		return requestsById.computeIfAbsent(id, i -> Collections.synchronizedCollection(new ArrayList<>()));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyNewResult(int id)
	{
		getRequestList(id).removeIf(Request::checkResult);
	}

	public Object getDeferredResult(int id, Supplier<List<T>> listSupplier)
	{
		Request<T> request = new Request<>(listSupplier);

		CompletableFuture.runAsync(() ->
		{
			new TransactionTemplate(transactionManager).executeWithoutResult(status ->
			{
				if (!request.checkResult())
					getRequestList(id).add(request);
			});
		});

		return request.getDeferredResult();
	}
}

class Request<T>
{
	private static final long TIMEOUT = 30 * 1000;

	private final Supplier<List<T>> listSupplier;
	private final DeferredResult<List<T>> deferredResult;

	public Request(Supplier<List<T>> listSupplier)
	{
		this.listSupplier = listSupplier;
		this.deferredResult = new DeferredResult<>(TIMEOUT, new ResponseEntity<Void>(HttpStatus.REQUEST_TIMEOUT));
	}

	public DeferredResult<List<T>> getDeferredResult()
	{
		return deferredResult;
	}

	public boolean checkResult()
	{
		List<T> resultTmp = null;
		Exception exceptionTmp = null;

		try
		{
			resultTmp = listSupplier.get();
			Objects.requireNonNull(resultTmp);
		}
		catch (Exception e)
		{
			exceptionTmp = e;
		}

		Exception exception = exceptionTmp;
		List<T> result = resultTmp;

		if (result != null && result.isEmpty())
			return false;

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
		{
			@Override
			public void afterCompletion(int status)
			{
				if (status != TransactionSynchronization.STATUS_COMMITTED)
					deferredResult.setErrorResult(new ResponseEntity<Void>(HttpStatus.REQUEST_TIMEOUT));
				else if (exception != null)
					deferredResult.setErrorResult(exception);
				else
					deferredResult.setResult(result);
			}
		});

		return true;
	}
}

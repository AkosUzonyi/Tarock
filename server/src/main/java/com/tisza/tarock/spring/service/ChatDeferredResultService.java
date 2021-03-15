package com.tisza.tarock.spring.service;

import com.tisza.tarock.spring.model.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.context.request.async.*;

import java.util.*;
import java.util.concurrent.*;

@Service
public class ChatDeferredResultService
{
	private final long TIMEOUT = 10000;
	private final Map<Integer, Collection<DeferredResult<List<ChatDB>>>> pendingResultsByGameSessionID = new ConcurrentHashMap<>();

	private Collection<DeferredResult<List<ChatDB>>> getDeferredResultList(int gameSessionID)
	{
		return pendingResultsByGameSessionID.computeIfAbsent(gameSessionID, id -> Collections.synchronizedCollection(new ArrayList<>()));
	}

	public void newChat(int gameSessionID, ChatDB chat)
	{
		//TODO: multiple result in same millisec
		Collection<DeferredResult<List<ChatDB>>> deferredResults = getDeferredResultList(gameSessionID);
		synchronized (deferredResults)
		{
			for (DeferredResult<List<ChatDB>> deferredResult : deferredResults)
				deferredResult.setResult(Collections.singletonList(chat));

			deferredResults.clear();
		}
	}

	public DeferredResult<List<ChatDB>> getDeferredResult(int gameSessionID, long from)
	{
		//TODO: from paramter
		DeferredResult<List<ChatDB>> deferredResult = new DeferredResult<>(TIMEOUT, new ResponseEntity<Void>(HttpStatus.REQUEST_TIMEOUT));
		getDeferredResultList(gameSessionID).add(deferredResult);
		return deferredResult;
	}
}

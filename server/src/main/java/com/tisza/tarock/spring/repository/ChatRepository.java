package com.tisza.tarock.spring.repository;

import com.tisza.tarock.spring.model.*;
import org.springframework.data.repository.*;

import java.util.*;

public interface ChatRepository extends CrudRepository<ChatDB, Integer>
{
	//TODO: paging
	List<ChatDB> findTop100ByGameSessionIdAndTimeGreaterThanEqual(int gameSessionId, long time);
}

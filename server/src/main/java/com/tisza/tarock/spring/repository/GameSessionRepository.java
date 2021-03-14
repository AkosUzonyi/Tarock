package com.tisza.tarock.spring.repository;

import com.tisza.tarock.spring.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.*;

import java.util.*;

public interface GameSessionRepository extends CrudRepository<GameSessionDB, Integer>
{
	@Query("SELECT g FROM GameSessionDB g WHERE g.state <> 'deleted'")
	List<GameSessionDB> findActive();
}

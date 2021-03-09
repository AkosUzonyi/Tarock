package com.tisza.tarock.spring.repository;

import com.tisza.tarock.spring.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.*;

public interface UserRepository extends CrudRepository<User, Integer>
{

}

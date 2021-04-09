package com.tisza.tarock.spring.repository;

import com.tisza.tarock.spring.model.*;
import org.springframework.data.repository.*;

public interface IdpUserRepository extends CrudRepository<IdpUserDB, Integer>
{
	IdpUserDB findByIdpServiceIdAndIdpUserId(String idpServiceId, String idpUserId);
}

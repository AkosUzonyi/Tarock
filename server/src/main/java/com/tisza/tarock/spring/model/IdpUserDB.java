package com.tisza.tarock.spring.model;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

@Entity
public class IdpUserDB
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int id;
	public String idpServiceId;
	public String idpUserId;
	public int userId;
}

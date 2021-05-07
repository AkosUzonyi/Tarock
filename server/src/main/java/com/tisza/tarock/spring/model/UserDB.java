package com.tisza.tarock.spring.model;

import javax.persistence.*;

@Entity
@Table(name = "user")
public class UserDB
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int id;

	public String name;

	public String imgUrl;

	public boolean getIsBot()
	{
		return id < 4;
	}

	public long registrationTime;
}

package com.tisza.tarock.spring.model;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

@Entity
@Table(name = "chat")
public class ChatDB
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	public int id;

	@JsonIgnore
	public int gameSessionId;

	public int userId;

	public String message;

	public long time;
}

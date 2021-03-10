package com.tisza.tarock.spring.model;

import javax.persistence.*;

@Entity
@Table(name = "chat")
public class ChatDB
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int id;
	public int gameSessionId;
	public int userId;
	public String message;
	public long time;
}

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
	@ManyToOne
	@JoinColumn(name = "game_session_id")
	public GameSessionDB gameSession;

	public int userId;

	public String message;

	public long time;
}

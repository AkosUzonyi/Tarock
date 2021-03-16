package com.tisza.tarock.spring.model;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

@Entity
@Table(name = "player")
@IdClass(PlayerId.class)
public class PlayerDB
{
	@Id
	@JsonIgnore
	public int gameSessionId;

	@Id
	@JsonIgnore
	public int ordinal;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	public UserDB user;

	public int points;
}

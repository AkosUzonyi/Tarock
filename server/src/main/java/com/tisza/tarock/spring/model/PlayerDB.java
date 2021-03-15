package com.tisza.tarock.spring.model;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.io.*;

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

	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	public UserDB user;

	@JsonIgnore
	public int userId;

	public int points;
}

package com.tisza.tarock.spring.model;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

@Entity
@Table(name = "action")
@IdClass(ActionId.class)
public class ActionDB
{
	@Id
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "game_id")
	public GameDB game;

	@Id
	public int ordinal;

	public int seat;

	public String action;

	public long time;
}

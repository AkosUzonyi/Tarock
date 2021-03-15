package com.tisza.tarock.spring.model;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

@Entity
@Table(name = "deck_card")
@IdClass(ActionId.class)
public class ActionDB
{
	@Id
	@JsonIgnore
	public int gameId;

	@Id
	public int ordinal;

	public int seat;

	public String action;

	public long time;
}

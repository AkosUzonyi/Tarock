package com.tisza.tarock.spring.model;

import javax.persistence.*;

@Entity
@Table(name = "deck_card")
@IdClass(ActionId.class)
public class ActionDB
{
	@Id
	public int gameId;

	@Id
	public int ordinal;

	public int seat;

	public String action;

	public long time;
}

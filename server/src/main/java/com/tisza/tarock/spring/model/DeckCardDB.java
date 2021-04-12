package com.tisza.tarock.spring.model;

import javax.persistence.*;

@Entity
@Table(name = "deck_card")
@IdClass(DeckCardId.class)
public class DeckCardDB
{
	@Id
	@ManyToOne
	@JoinColumn(name = "game_id")
	public GameDB game;

	@Id
	public int ordinal;

	public String card;
}

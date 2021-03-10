package com.tisza.tarock.spring.model;

import javax.persistence.*;

@Entity
@Table(name = "deck_card")
@IdClass(DeckCardId.class)
public class DeckCardDB
{
	@Id
	public int gameId;

	@Id
	public int ordinal;

	public String card;
}

package com.tisza.tarock.spring.model;

import com.fasterxml.jackson.annotation.*;

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

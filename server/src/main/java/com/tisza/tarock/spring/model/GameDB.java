package com.tisza.tarock.spring.model;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "game")
public class GameDB
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int id;

	@ManyToOne
	public GameSessionDB gameSession;

	public int beginnerPlayer;

	public long createTime;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="gameId")
	@OrderBy("ordinal")
	@JsonIgnore
	public List<DeckCardDB> deckCards;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="gameId")
	@OrderBy("ordinal")
	@JsonIgnore
	public List<ActionDB> actions;
}

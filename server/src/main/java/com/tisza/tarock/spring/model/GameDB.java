package com.tisza.tarock.spring.model;

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
	public List<DeckCardDB> deckCards;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="gameId")
	@OrderBy("ordinal")
	public List<ActionDB> actions;
}

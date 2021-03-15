package com.tisza.tarock.spring.model;

import com.fasterxml.jackson.annotation.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.doubleround.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "game_session")
public class GameSessionDB
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int id;

	@Enumerated(EnumType.STRING) //TODO: lowercase
	public GameType type;

	public String state;

	@Enumerated(EnumType.STRING)
	public DoubleRoundType doubleRoundType;

	@JsonIgnore
	public int doubleRoundData;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="gameSessionId")
	@OrderBy("ordinal")
	public List<PlayerDB> players;

	public Integer currentGameId;

	public long createTime;
}

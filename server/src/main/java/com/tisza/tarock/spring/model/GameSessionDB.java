package com.tisza.tarock.spring.model;

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

	@Enumerated(EnumType.STRING)
	public GameType type;

	public String state;

	@Enumerated(EnumType.STRING)
	public DoubleRoundType doubleRoundType;

	public int doubleRoundData;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="gameSessionId", referencedColumnName = "id")
	@OrderBy("ordinal")
	public List<PlayerDB> players;

	public Integer currentGameId;

	public long createTime;
}

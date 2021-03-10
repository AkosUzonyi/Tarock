package com.tisza.tarock.spring.model;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "game_session")
public class GameSessionDB
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) //TODO
	public int id;

	public String type;

	public String doubleRoundType;

	public int doubleRoundData;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="game_session_id", referencedColumnName = "id")
	@OrderBy("ordinal")
	public List<PlayerDB> players;

	public Integer currentGameId;

	public long createTime;
}

package com.tisza.tarock.spring.model;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "game_session")
//@SecondaryTable(name = "player", pkJoinColumns = @PrimaryKeyJoinColumn(name = "game_session_id"))
public class GameSessionDB
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) //TODO
	public int id;

	public String type;

	public String doubleRoundType;

	public int doubleRoundData;

	/*@Column(table="player", name="user_id")
	public List<Integer> users;

	@Column(table="player", name="points")
	public List<Integer> playerPoints;*/

	@OneToMany(targetEntity = PlayerDB.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL/*, mappedBy = "gameSession"*/)
	@JoinColumn(name="game_session_id", referencedColumnName = "id")
	public List<PlayerDB> player;

	public Integer currentGameId;

	public long createTime;
}

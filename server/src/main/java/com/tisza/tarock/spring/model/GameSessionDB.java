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

	//@Enumerated(EnumType.STRING) //TODO: lowercase
	public String type;

	public String state;

	//@Enumerated(EnumType.STRING)  //TODO: lowercase
	public String doubleRoundType;

	@JsonIgnore
	public int doubleRoundData;

	@OneToMany(mappedBy = "gameSession", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("ordinal")
	public List<PlayerDB> players;

	public Integer currentGameId;

	public long createTime;
}

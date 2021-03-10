package com.tisza.tarock.spring.model;

import javax.persistence.*;

@Entity
@Table(name = "game")
public class GameDB
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int id;

	@ManyToOne
	@JoinColumn(name = "game_session_id", referencedColumnName = "id")
	public GameSessionDB gameSession;

	public int beginnerPlayer;

	public long createTime;
}

package com.tisza.tarock.spring.model;

import javax.persistence.*;
import java.io.*;
import java.util.*;

@Entity
@Table(name = "player")
public class PlayerDB implements Serializable
{
	@Id
	@Column(name="game_session_id")
	public Integer gameSessionId;

	@Column(name="seat")
	@Id
	public Integer seat;

	/*@ManyToOne
	@JoinColumn(name="game_session_id")
	public GameSessionDB gameSession;*/

	public int userId;

	public int points;
}

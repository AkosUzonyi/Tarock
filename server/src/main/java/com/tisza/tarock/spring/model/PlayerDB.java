package com.tisza.tarock.spring.model;

import javax.persistence.*;
import java.io.*;

@Entity
@Table(name = "player")
@IdClass(PlayerId.class)
public class PlayerDB
{
	@Id
	//@Column(name = "game_session_id")
	public int gameSessionId;

	@Id
	public int ordinal;

	public int userId;

	public int points;
}

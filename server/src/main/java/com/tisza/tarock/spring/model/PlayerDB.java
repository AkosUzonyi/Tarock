package com.tisza.tarock.spring.model;

import javax.persistence.*;
import java.io.*;

@Entity
@Table(name = "player")
@IdClass(PlayerId.class)
public class PlayerDB
{
	@Id
	public int gameSessionId;

	@Id
	public int ordinal;

	public int userId;

	public int points;
}

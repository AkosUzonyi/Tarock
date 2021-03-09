package com.tisza.tarock.spring.dto;

import javax.persistence.*;
import java.util.*;

public class GameSessionDTO
{
	public int id;

	public String type;

	public String doubleRoundType;

	public List<Integer> users;

	public List<Integer> playerPoints;

	public Integer currentGameID;
}

package com.tisza.tarock.spring.dto;

import java.util.*;

public class GameStateDTO
{
	public List<String> cards;
	public String phase;
	public List<Boolean> turn;
	public boolean canThrowCards;
	public List<String> teamInfo;
	public List<String> availableActions;
	public List<Integer> tarockFoldCount;
	public List<String> currentTrick;
	public List<String> previousTrick;
	public int previousTrickWinner;
	public Statistics statistics;

	public static class Statistics
	{
		public int callerCardPoints;
		public int opponentCardPoints;
		public List<AnnouncementResult> announcementResults;
		public int sumPoints;
		public int pointMultiplier;
	}

	public static class AnnouncementResult
	{
		public String announcement;
		public int points;
		public String team;
	}
}

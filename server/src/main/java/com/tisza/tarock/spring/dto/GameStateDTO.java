package com.tisza.tarock.spring.dto;

import java.util.*;

public class GameStateDTO
{
	public List<List<String>> cards = new ArrayList<>();
	public String phase;
	public List<Boolean> turn = new ArrayList<>();
	public boolean canThrowCards;
	public List<String> teamInfo = new ArrayList<>();
	public List<String> availableActions = new ArrayList<>();
	public List<String> callerTarockFold = new ArrayList<>();
	public List<Integer> tarockFoldCount = new ArrayList<>();
	public List<String> currentTrick = new ArrayList<>();
	public List<String> previousTrick = new ArrayList<>();
	public Integer previousTrickWinner;
	public Statistics statistics = new Statistics();

	public static class Statistics
	{
		public int callerCardPoints;
		public int opponentCardPoints;
		public List<AnnouncementResult> announcementResults = new ArrayList<>();
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

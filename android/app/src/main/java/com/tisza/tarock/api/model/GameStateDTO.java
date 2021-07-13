package com.tisza.tarock.api.model;

import java.util.*;

public class GameStateDTO
{
	public String phase;
	public boolean canThrowCards;
	public List<String> availableActions = new ArrayList<>();
	public Integer previousTrickWinner;
	public List<PlayerInfo> playerInfos = new ArrayList<>();
	public Statistics statistics = new Statistics();

	public static class PlayerInfo
	{
		public User user;
		public List<String> cards;
		public boolean turn;
		public String team;
		public int tarockFoldCount;
		public List<String> visibleFoldedCards = new ArrayList<>();
		public String currentTrickCard;
		public String previousTrickCard;
	}

	public static class Statistics
	{
		public int callerCardPoints;
		public int opponentCardPoints;
		public List<AnnouncementResult> callerAnnouncementResults = new ArrayList<>();
		public List<AnnouncementResult> opponentAnnouncementResults = new ArrayList<>();
		public int sumPoints;
		public int pointMultiplier;
	}

	public static class AnnouncementResult
	{
		public String announcement;
		public int points;
	}
}

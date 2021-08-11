package com.tisza.tarock.spring.dto;

import com.tisza.tarock.spring.model.*;

import java.util.*;

public class GameDTO
{
	public int id;
	public String type;
	public int gameSessionId;
	public long createTime;
	public String phase;
	public boolean canThrowCards;
	public List<String> availableActions = new ArrayList<>();
	public Integer previousTrickWinner;
	public List<PlayerInfo> players = new ArrayList<>();
	public Statistics statistics = new Statistics();

	public static class PlayerInfo
	{
		public UserDB user;
		public List<String> cards;
		public boolean turn;
		public String team;
		public int tarockFoldCount;
		public List<String> visibleFoldedCards = new ArrayList<>();
		public String currentTrickCard;
		public String previousTrickCard;
		public int points;
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

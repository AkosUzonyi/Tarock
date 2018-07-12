package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class GameState
{
	public static final int ROUND_COUNT = 9;

	private final GameType gameType;
	private final List<String> playerNames;
	private final PlayerSeat beginnerPlayer;

	private final GameFinishedListener gameFinishedListener;

	private TeamInfoTracker teamInfoTracker;

	private GameHistory history;
	private List<EventInstance> events = new ArrayList<>();
	private List<EventInstance> newEvents = new ArrayList<>();

	private Phase currentPhase;

	private PlayerSeatMap<PlayerCards> playersCards = new PlayerSeatMap<>();
	private List<Card> talon;

	private Invitation invitSent = Invitation.NONE;
	private PlayerSeat invitingPlayer = null;
	private PlayerSeat bidWinnerPlayer = null;
	private int winnerBid;

	private Map<Team, List<Card>> skartForTeams = new HashMap<>();
	private PlayerSeat playerSkarted20 = null;

	private PlayerPairs playerPairs = null;
	private boolean isSoloIntentional = false;
	private Invitation invitAccepted = Invitation.NONE;
	private PlayerSeat playerToAnnounceSolo = null;

	private AnnouncementsState announcementsState = new AnnouncementsState();

	private List<Round> roundsPassed = new ArrayList<>();
	private PlayerSeatMap<Collection<Card>> wonCards = new PlayerSeatMap<>();

	private boolean inGameStatisticsCalculated = false;
	private boolean statisticsCalculated = false;
	private final int pointMultiplier;
	private int pointsForCallerTeam;
	private List<AnnouncementResult> announcementResults = new ArrayList<>();
	private int callerGamePoints, opponentGamePoints;

	{
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			playersCards.put(player, new PlayerCards());
			wonCards.put(player, new ArrayList<>());
		}

		for (Team t : Team.values())
		{
			skartForTeams.put(t, new ArrayList<>());
		}
	}

	public GameState(GameType gameType, List<String> playerNames, PlayerSeat beginnerPlayer, GameFinishedListener gameFinishedListener, int pointMultiplier)
	{
		this.gameType = gameType;
		this.playerNames = playerNames;
		this.beginnerPlayer = beginnerPlayer;
		this.gameFinishedListener = gameFinishedListener;
		this.pointMultiplier = pointMultiplier;

		teamInfoTracker = new TeamInfoTracker(this);
		history = new GameHistory();
	}

	public List<EventInstance> start()
	{
		List<Card> cardsToDeal = new ArrayList<>(Card.getAll());
		Collections.shuffle(cardsToDeal);
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			for (int i = 0; i < ROUND_COUNT; i++)
			{
				getPlayerCards(player).addCard(cardsToDeal.remove(0));
			}
		}
		setTalon(cardsToDeal);

		broadcastEvent(Event.startGame(playerNames, gameType, beginnerPlayer));
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			sendEvent(player, Event.seat(player));
			sendEvent(player, Event.playerCards(playersCards.get(player)));
			history.setOriginalPlayersCards(player, new ArrayList<>(playersCards.get(player).getCards()));
		}

		changePhase(new Bidding(this));
		return newEvents;
	}

	public List<EventInstance> processAction(Action action)
	{
		newEvents.clear();
		action.handle(currentPhase);
		return newEvents;
	}

	public List<EventInstance> getEvents()
	{
		return events;
	}

	public void finish()
	{
		switch (currentPhase.asEnum())
		{
			case INTERRUPTED:
				gameFinishedListener.gameInterrupted();
				break;
			case END:
				gameFinishedListener.gameFinished();
				break;
			default:
				throw new IllegalStateException();
		}
	}

	public GameType getGameType()
	{
		return gameType;
	}

	public PlayerCards getPlayerCards(PlayerSeat player)
	{
		return playersCards.get(player);
	}

	public PlayerSeat getBeginnerPlayer()
	{
		return beginnerPlayer;
	}

	void setTalon(List<Card> talon)
	{
		this.talon = talon;
	}

	public List<Card> getTalon()
	{
		return talon;
	}

	public TeamInfoTracker getTeamInfoTracker()
	{
		return teamInfoTracker;
	}

	public GameHistory getHistory()
	{
		return history;
	}

	public void broadcastEvent(Event event)
	{
		events.add(new EventInstance(null, event));
		newEvents.add(new EventInstance(null, event));
		event.handle(teamInfoTracker);
	}

	public void sendEvent(PlayerSeat player, Event event)
	{
		events.add(new EventInstance(player, event));
		newEvents.add(new EventInstance(player, event));
	}

	void changePhase(Phase phase)
	{
		currentPhase = phase;
		broadcastEvent(Event.phaseChanged(currentPhase.asEnum()));
		currentPhase.onStart();
	}

	public Phase getCurrentPhase()
	{
		return currentPhase;
	}

	void setInvitationSent(Invitation invitSent, PlayerSeat invitingPlayer)
	{
		if (invitSent == null)
			throw new NullPointerException();

		this.invitSent = invitSent;
		this.invitingPlayer = invitingPlayer;
	}

	public Invitation getInvitSent()
	{
		return invitSent;
	}

	public PlayerSeat getInvitingPlayer()
	{
		return invitingPlayer;
	}

	void setBidResult(PlayerSeat bidWinnerPlayer, int winnerBid)
	{
		this.bidWinnerPlayer = bidWinnerPlayer;
		this.winnerBid = winnerBid;
	}

	public PlayerSeat getBidWinnerPlayer()
	{
		return bidWinnerPlayer;
	}

	public int getWinnerBid()
	{
		if (bidWinnerPlayer == null)
			throw new IllegalStateException();

		return winnerBid;
	}

	void addCardToSkart(Team team, Card card)
	{
		skartForTeams.get(team).add(card);
	}

	public List<Card> getSkartForTeam(Team team)
	{
		return skartForTeams.get(team);
	}

	void setPlayerSkarted20(PlayerSeat playerSkarted20)
	{
		this.playerSkarted20 = playerSkarted20;
	}

	public PlayerSeat getPlayerSkarted20()
	{
		return playerSkarted20;
	}

	void setPlayerPairs(PlayerPairs playerPairs)
	{
		this.playerPairs = playerPairs;
	}

	public PlayerPairs getPlayerPairs()
	{
		return playerPairs;
	}

	void setSoloIntentional()
	{
		isSoloIntentional = true;

		List<Card> callerSkart = skartForTeams.get(Team.CALLER);
		List<Card> opponentSkart = skartForTeams.get(Team.OPPONENT);
		callerSkart.addAll(opponentSkart);
		opponentSkart.clear();
	}

	public boolean isSoloIntentional()
	{
		return isSoloIntentional;
	}

	void invitAccepted()
	{
		invitAccepted = invitSent;
	}

	public Invitation getInvitAccepted()
	{
		return invitAccepted;
	}

	void setPlayerToAnnounceSolo(PlayerSeat playerToAnnounceSolo)
	{
		this.playerToAnnounceSolo = playerToAnnounceSolo;
	}

	public PlayerSeat getPlayerToAnnounceSolo()
	{
		return playerToAnnounceSolo;
	}

	public AnnouncementsState getAnnouncementsState()
	{
		return announcementsState;
	}

	void addRound(Round round)
	{
		roundsPassed.add(round);
	}

	boolean areAllRoundsPassed()
	{
		return roundsPassed.size() >= ROUND_COUNT;
	}

	public Round getRound(int index)
	{
		return roundsPassed.get(index);
	}

	void addWonCards(PlayerSeat player, Collection<Card> collection)
	{
		wonCards.get(player).addAll(collection);
	}

	public Collection<Card> getWonCards(PlayerSeat player)
	{
		return wonCards.get(player);
	}

	public int calculateGamePoints(Team team)
	{
		int points = 0;

		for (PlayerSeat player : playerPairs.getPlayersInTeam(team))
		{
			for (Card c : getWonCards(player))
			{
				points += c.getPoints();
			}
		}

		for (Card c : getSkartForTeam(team))
		{
			points += c.getPoints();
		}

		return points;
	}

	void calculateInGameStatistics()
	{
		inGameStatisticsCalculated = true;

		announcementResults.clear();

		for (Team team : Team.values())
		{
			for (Announcement announcement : Announcements.getAll())
			{
				if (!gameType.hasParent(announcement.getGameType()))
					continue;

				if (!announcementsState.isAnnounced(team, announcement))
					continue;

				int acl = announcementsState.getContraLevel(team, announcement);
				AnnouncementContra ac = new AnnouncementContra(announcement, acl);
				announcementResults.add(new AnnouncementResult(ac, 0, team));
			}
		}
	}

	void sendInGameStatistics()
	{
		if (!inGameStatisticsCalculated)
			throw new IllegalStateException();

		int callerGamePoints = 0;
		int opponentGamePoints = 0;
		int sumPoints = 0;
		broadcastEvent(Event.announcementStatistics(callerGamePoints, opponentGamePoints, announcementResults, sumPoints, pointMultiplier));
	}

	void calculateStatistics()
	{
		if (statisticsCalculated)
			throw new IllegalStateException();

		statisticsCalculated = true;

		int[] points = new int[4];

		announcementResults.clear();
		pointsForCallerTeam = 0;

		for (Team team : Team.values())
		{
			if (team == Team.CALLER)
				callerGamePoints = calculateGamePoints(team);
			else
				opponentGamePoints = calculateGamePoints(team);

			for (Announcement announcement : Announcements.getAll())
			{
				if (!gameType.hasParent(announcement.getGameType()))
					continue;

				int annoucementPoints = announcement.calculatePoints(this, team);
				annoucementPoints *= pointMultiplier;

				pointsForCallerTeam += annoucementPoints * (team == Team.CALLER ? 1 : -1);

				if (annoucementPoints != 0)
				{
					int acl = announcementsState.isAnnounced(team, announcement) ? announcementsState.getContraLevel(team, announcement) : -1;
					AnnouncementContra ac = new AnnouncementContra(announcement, acl);
					announcementResults.add(new AnnouncementResult(ac, annoucementPoints, team));
				}
			}
		}

		int allTarockCountPoints = 0;
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			TarockCount tarockCountAnnouncement = announcementsState.getTarockCountAnnounced(player);
			int tarockCountPoints = tarockCountAnnouncement == null ? 0 : tarockCountAnnouncement.getPoints();
			tarockCountPoints *= pointMultiplier;
			points[player.asInt()] += tarockCountPoints * 4;
			allTarockCountPoints += tarockCountPoints;
		}

		for (int i = 0; i < 4; i++)
		{
			points[i] -= pointsForCallerTeam;
			points[i] -= allTarockCountPoints;
		}
		points[playerPairs.getCaller().asInt()] += pointsForCallerTeam * 2;
		points[playerPairs.getCalled().asInt()] += pointsForCallerTeam * 2;

		gameFinishedListener.pointsEarned(points);
	}

	void sendStatistics()
	{
		if (!statisticsCalculated)
			throw new IllegalStateException();

		broadcastEvent(Event.announcementStatistics(callerGamePoints, opponentGamePoints, announcementResults, pointsForCallerTeam, pointMultiplier));
	}

	void sendPlayerPoints()
	{
		broadcastEvent(Event.playerPoints(gameFinishedListener.getPlayerPoints()));
	}
}

package com.tisza.tarock.game;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.Bidding.*;
import com.tisza.tarock.message.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class GameState
{
	public static final int ROUND_COUNT = 9;

	private final GameType gameType;
	private final PlayerSeat.Map<Player> players;
	private final PlayerSeat beginnerPlayer;

	private final GameFinishedListener gameFinishedListener;
	private EventSender broadcastEventSender;

	private GameHistory history;

	private Phase currentPhase;

	private PlayerSeat.Map<PlayerCards> playersCards = new PlayerSeat.Map<>();
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
	private PlayerSeat.Map<Collection<Card>> wonCards = new PlayerSeat.Map<>();

	private int[] points;
	private int pointsForCallerTeam;
	private Map<Team, List<AnnouncementStaticticsEntry>> statEntriesForTeams;
	private Map<Team, Integer> gamePointsForTeams;

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

	public GameState(GameType gameType, PlayerSeat.Map<Player> players, PlayerSeat beginnerPlayer, GameFinishedListener gameFinishedListener)
	{
		this.gameType = gameType;
		this.players = players;
		this.beginnerPlayer = beginnerPlayer;
		this.gameFinishedListener = gameFinishedListener;

		history = new GameHistory();
		broadcastEventSender = new BroadcastEventSender(players.values().stream().map(Player::getEventSender).collect(Collectors.toList()));
	}

	public void start()
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

		for (PlayerSeat player : PlayerSeat.getAll())
		{
			getPlayerEventSender(player).startGame(player, getPlayerNames(), gameType, beginnerPlayer);
			getPlayerEventSender(player).playerCards(playersCards.get(player));
			history.setOriginalPlayersCards(player, new ArrayList<>(playersCards.get(player).getCards()));
		}

		changePhase(new Bidding(this));
	}

	public List<String> getPlayerNames()
	{
		return players.values().stream().map(Player::getName).collect(Collectors.toList());
	}

	public void finish()
	{
		switch (currentPhase.asEnum())
		{
			case INTERRUPTED:
				gameFinishedListener.gameInterrupted();
				break;
			case END:
				gameFinishedListener.gameFinished(points);
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

	public GameHistory getHistory()
	{
		return history;
	}

	EventSender getBroadcastEventSender()
	{
		return broadcastEventSender;
	}

	EventSender getPlayerEventSender(PlayerSeat player)
	{
		return players.get(player).getEventSender();
	}

	void changePhase(Phase phase)
	{
		currentPhase = phase;
		getBroadcastEventSender().phaseChanged(currentPhase.asEnum());
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

	void calculateStatistics()
	{
		points = new int[4];

		try
		{
			history.writeJSON(new OutputStreamWriter(System.out));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		statEntriesForTeams = new HashMap<>();
		gamePointsForTeams = new HashMap<>();
		pointsForCallerTeam = 0;

		for (Team team : Team.values())
		{
			gamePointsForTeams.put(team, calculateGamePoints(team));

			List<AnnouncementStaticticsEntry> entriesForTeam = new ArrayList<>();
			statEntriesForTeams.put(team, entriesForTeam);

			for (Announcement announcement : Announcements.getAll())
			{
				if (!gameType.hasParent(announcement.getGameType()))
					continue;

				int annoucementPoints = announcement.calculatePoints(this, team);

				pointsForCallerTeam += annoucementPoints * (team == Team.CALLER ? 1 : -1);

				if (annoucementPoints != 0)
				{
					int acl = announcementsState.isAnnounced(team, announcement) ? announcementsState.getContraLevel(team, announcement) : -1;
					AnnouncementContra ac = new AnnouncementContra(announcement, acl);
					entriesForTeam.add(new AnnouncementStaticticsEntry(ac, annoucementPoints));
				}
			}
		}

		int allTarockCountPoints = 0;
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			TarockCount tarockCountAnnouncement = announcementsState.getTarockCountAnnounced(player);
			int tarockCountPoints = tarockCountAnnouncement == null ? 0 : tarockCountAnnouncement.getPoints();
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
	}

	void sendStatistics()
	{
		if (points == null)
			throw new IllegalStateException();

		for (PlayerSeat player : PlayerSeat.getAll())
		{
			Team team = playerPairs.getTeam(player);
			int selfGamePoints = gamePointsForTeams.get(team);
			int opponentGamePoints = gamePointsForTeams.get(team.getOther());
			List<AnnouncementStaticticsEntry> selfEntries = statEntriesForTeams.get(team);
			List<AnnouncementStaticticsEntry> opponentEntries = statEntriesForTeams.get(team.getOther());
			int sumPoints = pointsForCallerTeam * (team == Team.CALLER ? 1 : -1);
			getPlayerEventSender(player).announcementStatistics(selfGamePoints, opponentGamePoints, selfEntries, opponentEntries, sumPoints, points);
		}
	}
}

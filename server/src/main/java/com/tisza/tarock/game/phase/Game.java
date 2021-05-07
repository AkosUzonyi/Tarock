package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

public class Game
{
	public static final int ROUND_COUNT = 9;

	private final GameType gameType;
	private final List<Card> deck;

	private Set<TeamInfo> teamInfos = new HashSet<>();
	private PlayerSeatMap<Boolean> turns = new PlayerSeatMap<>();

	private Phase currentPhase;

	private PlayerSeatMap<PlayerCards> playersCards = new PlayerSeatMap<>();
	private List<Card> talon;

	private Invitation invitSent = null;
	private PlayerSeat invitingPlayer = null;
	private PlayerSeat bidWinnerPlayer = null;
	private int winnerBid;

	private PlayerSeatMap<Collection<Card>> foldedCards = new PlayerSeatMap<>();

	private Card calledCard = null;
	private PlayerPairs playerPairs = null;
	private boolean isSoloIntentional = false;
	private Invitation invitAccepted = null;
	private PlayerSeat playerToAnnounceSolo = null;

	private AnnouncementsState announcementsState = new AnnouncementsState();

	private List<Trick> tricks = new ArrayList<>();
	private PlayerSeatMap<Collection<Card>> wonCards = new PlayerSeatMap<>();

	private int callerCardPoints = 0;
	private int opponentCardPoints = 0;
	private List<AnnouncementResult> announcementResults = new ArrayList<>();
	private int sumPoints = 0;
	private int[] points = new int[4];
	private final int pointMultiplier;

	private boolean finished = false;
	private boolean normalFinish;

	{
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			playersCards.put(player, new PlayerCards());
			wonCards.put(player, new ArrayList<>());
		}
	}

	public Game(GameType gameType, List<Card> deck, int pointMultiplier)
	{
		this.gameType = gameType;
		this.deck = deck;
		this.pointMultiplier = pointMultiplier;

		List<Card> cardsToDeal = new ArrayList<>(deck);
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			for (int i = 0; i < ROUND_COUNT; i++)
			{
				getPlayerCards(player).addCard(cardsToDeal.remove(0));
			}
		}
		talon = cardsToDeal;

		changePhase(new Bidding(this));
	}

	public boolean processAction(PlayerSeat player, Action action)
	{
		return action.handle(player, currentPhase);
	}

	void finish()
	{
		switch (currentPhase.asEnum())
		{
			case INTERRUPTED:
				normalFinish = false;
				break;
			case END:
				normalFinish = true;
				break;
			default:
				throw new IllegalStateException();
		}
		finished = true;
	}

	public boolean isFinished()
	{
		return finished;
	}

	public boolean isNormalFinish()
	{
		return normalFinish;
	}

	public GameType getGameType()
	{
		return gameType;
	}

	public PlayerCards getPlayerCards(PlayerSeat player)
	{
		return playersCards.get(player);
	}

	public List<Card> getTalon()
	{
		return talon;
	}

	void changePhase(Phase phase)
	{
		currentPhase = phase;
		currentPhase.onStart();
	}

	Phase getCurrentPhase()
	{
		return currentPhase;
	}

	public PhaseEnum getCurrentPhaseEnum()
	{
		return currentPhase.asEnum();
	}

	public List<Action> getAvailableActions()
	{
		return currentPhase.getAvailableActions();
	}

	public boolean canThrowCards(PlayerSeat player)
	{
		return currentPhase.canThrowCards(player);
	}

	void turn(PlayerSeat player)
	{
		turns.fill(false);
		turns.put(player, true);
	}

	void setTurnOf(PlayerSeat player, boolean turn)
	{
		turns.put(player, turn);
	}

	void setAllTurn()
	{
		turns.fill(true);
	}

	public boolean getTurn(PlayerSeat player)
	{
		return turns.get(player);
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

	void setSkart(PlayerSeat player, Collection<Card> cards)
	{
		foldedCards.put(player, cards);
	}

	public Collection<Card> getSkart(PlayerSeat player)
	{
		return foldedCards.get(player);
	}

	public Card getCalledCard()
	{
		return calledCard;
	}

	void setCalledCard(Card card)
	{
		calledCard = card;
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

	public AnnouncementsState getAnnouncementsState()
	{
		return announcementsState;
	}

	void addTrick(Trick trick)
	{
		tricks.add(trick);
	}

	public boolean areAllTricksPassed()
	{
		return tricks.size() >= ROUND_COUNT && tricks.get(ROUND_COUNT - 1).isFinished();
	}

	public Trick getTrick(int index)
	{
		return tricks.get(index);
	}

	public int getTrickCount()
	{
		return tricks.size();
	}

	void addWonCards(PlayerSeat player, Collection<Card> collection)
	{
		wonCards.get(player).addAll(collection);
	}

	public Collection<Card> getWonCards(PlayerSeat player)
	{
		return wonCards.get(player);
	}

	void addNewTeamInfo(PlayerSeat player, PlayerSeat otherPlayer)
	{
		teamInfos.add(new TeamInfo(player, otherPlayer));
	}

	void revealAllTeamInfoOf(PlayerSeat player)
	{
		for (PlayerSeat p : PlayerSeat.getAll())
			addNewTeamInfo(p, player);
	}

	void revealAllTeamInfoFor(PlayerSeat player)
	{
		for (PlayerSeat p : PlayerSeat.getAll())
			addNewTeamInfo(player, p);
	}

	void revealAllTeamInfo()
	{
		for (PlayerSeat player : PlayerSeat.getAll())
			revealAllTeamInfoOf(player);
	}

	public boolean isTeamInfoGlobalOf(PlayerSeat player)
	{
		for (PlayerSeat p : PlayerSeat.getAll())
		{
			if (!hasTeamInfo(p, player))
				return false;
		}
		return true;
	}

	public boolean hasTeamInfo(PlayerSeat player, PlayerSeat otherPlayer)
	{
		return teamInfos.contains(new TeamInfo(player, otherPlayer));
	}

	public int calculateCardPoints(Team team)
	{
		boolean countSelfTeamSkart = true;
		boolean countOpponentTeamSkart = false;

		if (playerPairs.isSolo() && !isSoloIntentional)
		{
			if (team == Team.CALLER)
				countOpponentTeamSkart = true;

			if (team == Team.OPPONENT)
				countSelfTeamSkart = false;
		}

		int points = 0;

		for (PlayerSeat player : playerPairs.getPlayersInTeam(team))
			for (Card c : getWonCards(player))
				points += c.getPoints();

		if (countSelfTeamSkart)
			for (PlayerSeat player : playerPairs.getPlayersInTeam(team))
				for (Card c : foldedCards.get(player))
					points += c.getPoints();

		if (countOpponentTeamSkart)
			for (PlayerSeat player : playerPairs.getPlayersInTeam(team.getOther()))
				for (Card c : foldedCards.get(player))
					points += c.getPoints();

		return points;
	}

	void calculateInGameStatistics()
	{
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

	void calculateStatistics()
	{
		announcementResults.clear();
		Arrays.fill(points, 0);
		sumPoints = 0;

		for (Team team : Team.values())
		{
			if (team == Team.CALLER)
				callerCardPoints = calculateCardPoints(team);
			else
				opponentCardPoints = calculateCardPoints(team);

			for (Announcement announcement : Announcements.getAll())
			{
				if (!gameType.hasParent(announcement.getGameType()))
					continue;

				int announcementPoints = announcement.calculatePoints(this, team);
				announcementPoints *= pointMultiplier;

				sumPoints += announcementPoints * (team == Team.CALLER ? 1 : -1);

				if (announcementPoints != 0)
				{
					int acl = announcementsState.isAnnounced(team, announcement) ? announcementsState.getContraLevel(team, announcement) : -1;
					AnnouncementContra ac = new AnnouncementContra(announcement, acl);
					announcementResults.add(new AnnouncementResult(ac, announcementPoints, team));
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
			points[i] -= sumPoints;
			points[i] -= allTarockCountPoints;
		}
		points[playerPairs.getCaller().asInt()] += sumPoints * 2;
		points[playerPairs.getCalled().asInt()] += sumPoints * 2;
	}

	public int getCallerCardPoints()
	{
		return callerCardPoints;
	}

	public int getOpponentCardPoints()
	{
		return opponentCardPoints;
	}

	public List<AnnouncementResult> getAnnouncementResults()
	{
		return announcementResults;
	}

	public int getSumPoints()
	{
		return sumPoints;
	}

	public int getPointMultiplier()
	{
		return pointMultiplier;
	}

	public int getPoints(PlayerSeat seat)
	{
		return points[seat.asInt()];
	}
}

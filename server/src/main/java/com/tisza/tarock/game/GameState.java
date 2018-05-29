package com.tisza.tarock.game;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.Bidding.*;
import com.tisza.tarock.player.*;

import java.util.*;
import java.util.stream.*;

public class GameState
{
	private final GameType gameType;

	private final PlayerSeat beginnerPlayer;

	private PlayerSeat.Map<PlayerCards> playersCards = new PlayerSeat.Map<>();
	private List<Card> talon;

	private Invitation invitSent = Invitation.NONE;
	private PlayerSeat invitingPlayer = null;
	private PlayerSeat bidWinnerPlayer = null;
	private int winnerBid;

	private Map<Team, List<Card>> skartForTeams = new HashMap<Team, List<Card>>();
	private PlayerSeat playerSkarted20 = null;
	
	private PlayerPairs playerPairs = null;
	private boolean isSoloIntentional = false;
	private Invitation invitAccepted = Invitation.NONE;
	private PlayerSeat playerToAnnounceSolo = null;

	private AnnouncementsState announcementsState = new AnnouncementsState();

	private List<Round> roundsPassed = new ArrayList<>();
	private PlayerSeat.Map<Collection<Card>> wonCards = new PlayerSeat.Map<>();
	
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

	public GameState(GameType gameType, PlayerSeat beginnerPlayer)
	{
		this.gameType = gameType;
		this.beginnerPlayer = beginnerPlayer;

		List<Card> cardsToDeal = new ArrayList<>(Card.getAll());
		Collections.shuffle(cardsToDeal);
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			for (int i = 0; i < 9; i++)
			{
				getPlayerCards(player).addCard(cardsToDeal.remove(0));
			}
		}
		setTalon(cardsToDeal);
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

	void setSoloIntentional(boolean isSoloIntentional)
	{
		this.isSoloIntentional = isSoloIntentional;
	}

	public boolean isSoloIntentional()
	{
		return isSoloIntentional;
	}

	public void invitAccepted()
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
		return roundsPassed.size() >= GameSession.ROUND_COUNT;
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
}

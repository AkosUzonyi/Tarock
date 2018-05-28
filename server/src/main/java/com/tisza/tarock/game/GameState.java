package com.tisza.tarock.game;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.game.Bidding.Invitation;

import java.util.*;

public class GameState
{
	private final int beginnerPlayer;

	private List<PlayerCards> playersCards = new ArrayList<>();
	private List<Card> talon;

	private Invitation invitSent = Invitation.NONE;
	private int invitingPlayer = -1;
	private int bidWinnerPlayer = -1;
	private int winnerBid;

	private Map<Team, List<Card>> skartForTeams = new HashMap<Team, List<Card>>();
	private int playerSkarted20 = -1;
	
	private PlayerPairs playerPairs = null;
	private boolean isSoloIntentional = false;
	private Invitation invitAccepted = Invitation.NONE;
	private int playerToAnnounceSolo = -1;

	private AnnouncementsState announcementsState = new AnnouncementsState();

	private List<Round> roundsPassed = new ArrayList<>();
	private List<Collection<Card>> wonCards = new ArrayList<>();
	
	{
		for (Team t : Team.values())
		{
			skartForTeams.put(t, new ArrayList<>());
		}

		for (int i = 0; i < 4; i++)
		{
			playersCards.add(new PlayerCards());
			wonCards.add(new ArrayList<>());
		}
	}

	public GameState(int beginnerPlayer)
	{
		this.beginnerPlayer = beginnerPlayer;
	}

	public PlayerCards getPlayerCards(int player)
	{
		return playersCards.get(player);
	}

	public int getBeginnerPlayer()
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

	void setInvitationSent(Invitation invitSent, int invitingPlayer)
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

	public int getInvitingPlayer()
	{
		return invitingPlayer;
	}
	
	void setBidResult(int bidWinnerPlayer, int winnerBid)
	{
		this.bidWinnerPlayer = bidWinnerPlayer;
		this.winnerBid = winnerBid;
	}

	public int getBidWinnerPlayer()
	{
		return bidWinnerPlayer;
	}

	public int getWinnerBid()
	{
		if (bidWinnerPlayer < 0)
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

	void setPlayerSkarted20(int playerSkarted20)
	{
		this.playerSkarted20 = playerSkarted20;
	}
	
	public int getPlayerSkarted20()
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

	void setPlayerToAnnounceSolo(int playerToAnnounceSolo)
	{
		this.playerToAnnounceSolo = playerToAnnounceSolo;
	}

	public int getPlayerToAnnounceSolo()
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
	
	void addWonCards(int player, Collection<Card> collection)
	{
		wonCards.get(player).addAll(collection);
	}

	public Collection<Card> getWonCards(int player)
	{
		return wonCards.get(player);
	}

	public int calculateGamePoints(Team team)
	{
		int points = 0;
		for (int player : playerPairs.getPlayersInTeam(team))
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

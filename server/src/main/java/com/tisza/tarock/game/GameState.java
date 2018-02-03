package com.tisza.tarock.game;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.game.Bidding.Invitation;

import java.util.*;

public class GameState
{
	private final int beginnerPlayer;

	private List<PlayerCards> playersCards = new ArrayList<>();
	private PhaseEnum currentPhaseEnum;
	
	private Invitation invitSent = Invitation.NONE;
	private int invitingPlayer = -1;
	private int bidWinnerPlayer, winnerBid;

	private List<Card> talon;
	private Map<Team, List<Card>> skartForTeams = new HashMap<Team, List<Card>>();
	private int playerSkarted20 = -1;
	
	private PlayerPairs playerPairs = null;
	private boolean isSoloIntentional;
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

	void setPhase(PhaseEnum phase)
	{
		currentPhaseEnum = phase;
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

	void setInvitationSent(Invitation invitSent, int invitingPlayer)
	{
		this.invitSent = invitSent;
		this.invitingPlayer = invitingPlayer;
	}

	Invitation getInvitSent()
	{
		checkPhasePassed(PhaseEnum.BIDDING);
		return invitSent;
	}

	public int getInvitingPlayer()
	{
		checkPhasePassed(PhaseEnum.BIDDING);
		return invitingPlayer;
	}
	
	void setBidResult(int bidWinnerPlayer, int winnerBid)
	{
		this.bidWinnerPlayer = bidWinnerPlayer;
		this.winnerBid = winnerBid;
	}

	public int getBidWinnerPlayer()
	{
		checkPhasePassed(PhaseEnum.BIDDING);
		return bidWinnerPlayer;
	}

	public int getWinnerBid()
	{
		checkPhasePassed(PhaseEnum.BIDDING);
		return winnerBid;
	}
	
	public List<Card> getTalon()
	{
		return talon;
	}
	
	void addCardToSkart(Team team, Card card)
	{
		skartForTeams.get(team).add(card);
	}

	public List<Card> getSkartForTeam(Team team)
	{
		checkPhasePassed(PhaseEnum.CHANGING);
		return skartForTeams.get(team);
	}

	void setPlayerSkarted20(int playerSkarted20)
	{
		this.playerSkarted20 = playerSkarted20;
	}
	
	public int getPlayerSkarted20()
	{
		checkPhasePassed(PhaseEnum.CHANGING);
		return playerSkarted20;
	}

	void setPlayerPairs(PlayerPairs playerPairs)
	{
		this.playerPairs = playerPairs;
	}

	public PlayerPairs getPlayerPairs()
	{
		checkPhasePassed(PhaseEnum.CALLING);
		return playerPairs;
	}

	void setSoloIntentional(boolean isSoloIntentional)
	{
		this.isSoloIntentional = isSoloIntentional;
	}

	public boolean isSoloIntentional()
	{
		checkPhasePassed(PhaseEnum.CALLING);
		return isSoloIntentional;
	}

	public void invitAccepted()
	{
		this.invitAccepted = invitSent;
	}

	public Invitation getInvitAccepted()
	{
		checkPhasePassed(PhaseEnum.CALLING);
		return invitAccepted;
	}

	void setPlayerToAnnounceSolo(int playerToAnnounceSolo)
	{
		this.playerToAnnounceSolo = playerToAnnounceSolo;
	}

	public int getPlayerToAnnounceSolo()
	{
		checkPhasePassed(PhaseEnum.CALLING);
		return playerToAnnounceSolo;
	}
	
	public AnnouncementsState getAnnouncementsState()
	{
		checkPhasePassed(PhaseEnum.CALLING);
		return announcementsState;
	}
	
	void addRound(Round round)
	{
		checkPhasePassed(PhaseEnum.ANNOUNCING);
		roundsPassed.add(round);
	}
	
	boolean areAllRoundsPassed()
	{
		checkPhasePassed(PhaseEnum.ANNOUNCING);
		return roundsPassed.size() >= GameSession.ROUND_COUNT;
	}

	public Round getRound(int index)
	{
		checkPhasePassed(PhaseEnum.ANNOUNCING);
		return roundsPassed.get(index);
	}
	
	void addWonCards(int player, Collection<Card> collection)
	{
		checkPhasePassed(PhaseEnum.ANNOUNCING);
		wonCards.get(player).addAll(collection);
	}

	public Collection<Card> getWonCards(int player)
	{
		checkPhasePassed(PhaseEnum.GAMEPLAY);
		return wonCards.get(player);
	}

	public int calculateGamePoints(Team team)
	{
		checkPhasePassed(PhaseEnum.GAMEPLAY);
		
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
	
	void checkPhasePassed(PhaseEnum phase)
	{
		if (!currentPhaseEnum.isAfter(phase))
			throw new IllegalStateException();
	}
}

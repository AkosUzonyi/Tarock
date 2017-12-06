package com.tisza.tarock.game;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.Bidding.*;
import com.tisza.tarock.message.event.*;
import com.tisza.tarock.message.event.EventAnnouncementStatistics.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.server.*;
import com.tisza.tarock.proto.ActionProto.*;

import java.util.*;
import java.util.stream.*;

public class GameState
{
	public static final int ROUND_COUNT = 9;
	
	private GameSession gameSession;
	
	private int beginnerPlayer;

	private AllPlayersCards playersCards = new AllPlayersCards();
	private Phase currentPhase;
	
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

	private List<Round> roundsPassed = new ArrayList<Round>();
	private List<Collection<Card>> wonCards = new ArrayList<Collection<Card>>();
	
	{
		for (Team t : Team.values())
		{
			skartForTeams.put(t, new ArrayList<Card>());
		}

		for (int i = 0; i < 4; i++)
		{
			wonCards.add(new ArrayList<Card>());
		}
	}

	public GameState(GameSession gameSession, int beginnerPlayer)
	{
		this.gameSession = gameSession;
		this.beginnerPlayer = beginnerPlayer;
	}
	
	public void startNewGame(boolean doubleRound)
	{
		if (!doubleRound)
			beginnerPlayer++;
		
		List<Card> cardsToDeal = new ArrayList<Card>(Card.all);
		Collections.shuffle(cardsToDeal);
		for (int p = 0; p < 4; p++)
		{
			for (int i = 0; i < 9; i++)
			{
				playersCards.getPlayerCards(p).addCard(cardsToDeal.remove(0));
			}
		}
		talon = cardsToDeal;
		
		for (int i = 0; i < 4; i++)
		{
			sendEvent(i, new EventStartGame(i, gameSession.playerNames));
			sendEvent(i, new EventPlayerCards(getPlayerCards(i)));
		}
		changePhase(new Bidding(this));
	}
	
	void changePhase(Phase p)
	{
		currentPhase = p;
		broadcastEvent(new EventPhase(currentPhase.asEnum()));
		currentPhase.onStart();
	}
	
	void broadcastEvent(Event event)
	{
		gameSession.broadcastPacket(new PacketEvent(event));
	}
	
	void sendEvent(int target, Event event)
	{
		gameSession.sendPacketToPlayer(target, new PacketEvent(event));
	}

	public void handleAction(int player, Action action)
	{
	}

	/*public AllPlayersCards getAllPlayersCards()
	{
		return playersCards;
	}*/

	public PlayerCards getPlayerCards(int player)
	{
		return playersCards.getPlayerCards(player);
	}

	public int getBeginnerPlayer()
	{
		return beginnerPlayer;
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
		return roundsPassed.size() >= ROUND_COUNT;
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

	public void sendStatistics()
	{
		checkPhasePassed(PhaseEnum.GAMEPLAY);
				
		Map<Team, List<Entry>> statEntriesForTeams = new HashMap<Team, List<Entry>>();
		Map<Team, Integer> gamePointsForTeams = new HashMap<Team, Integer>();
		int pointsForCallerTeam = 0;
		
		for (Team team : Team.values())
		{
			gamePointsForTeams.put(team, calculateGamePoints(team));
			
			List<Entry> entriesForTeam = new ArrayList<Entry>();
			statEntriesForTeams.put(team, entriesForTeam);
			
			for (Announcement announcement : Announcements.getAll())
			{
				int annoucementPoints = announcement.calculatePoints(this, team);
				
				pointsForCallerTeam += annoucementPoints * (team == Team.CALLER ? 1 : -1);
				
				if (annoucementPoints != 0)
				{
					int acl = getAnnouncementsState().isAnnounced(team, announcement) ? getAnnouncementsState().getContraLevel(team, announcement) : -1;
					AnnouncementContra ac = new AnnouncementContra(announcement, acl);
					entriesForTeam.add(new EventAnnouncementStatistics.Entry(ac, annoucementPoints));
				}
			}
		}
		
		int[] points = new int[4];
		for (int i = 0; i < 4; i++)
		{
			points[i] = -pointsForCallerTeam;
		}
		points[playerPairs.getCaller()] += pointsForCallerTeam * 2;
		points[playerPairs.getCalled()] += pointsForCallerTeam * 2;
		gameSession.addPoints(points);
		
		for (int player = 0; player < 4; player++)
		{
			Team team = playerPairs.getTeam(player);
			int selfGamePoints = gamePointsForTeams.get(team);
			int opponentGamePoints = gamePointsForTeams.get(team.getOther());
			List<Entry> selfEntries = statEntriesForTeams.get(team);
			List<Entry> opponentEntries = statEntriesForTeams.get(team.getOther());
			int sumPoints = pointsForCallerTeam * (team == Team.CALLER ? 1 : -1);
			sendEvent(player, new EventAnnouncementStatistics(selfGamePoints, opponentGamePoints, selfEntries, opponentEntries, sumPoints, gameSession.getPoints()));
		}
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
	
	private void checkPhasePassed(PhaseEnum phase)
	{
		if (!currentPhase.asEnum().isAfter(phase))
			throw new IllegalStateException();
	}
}

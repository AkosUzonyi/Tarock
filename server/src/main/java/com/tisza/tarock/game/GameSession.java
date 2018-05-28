package com.tisza.tarock.game;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.player.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class GameSession implements Runnable
{
	public static final int ROUND_COUNT = 9;

	private int[] points = new int[4];

	private List<Player> players;
	private EventSender broadcastEventSender;
	private BlockingQueue<Action> actionQueue = new LinkedBlockingQueue<>();

	private GameState state;
	private Phase currentPhase;

	private Thread gameThread;

	public GameSession(List<Player> players)
	{
		if (players.size() != 4)
			throw new IllegalArgumentException();

		this.players = players;
	}

	public void startSession()
	{
		if (gameThread != null)
			throw new IllegalArgumentException();

		gameThread = new Thread(this, "GameSession thread");
		gameThread.start();
	}

	public void stopSession()
	{
		if (gameThread == null)
			throw new IllegalArgumentException();

		gameThread.interrupt();
	}

	@Override
	public void run()
	{
		broadcastEventSender = new BroadcastEventSender(players.stream().map(Player::getEventSender).collect(Collectors.toList()));
		for (int i = 0; i < 4; i++)
		{
			players.get(i).onJoinedToGame(actionQueue, i);
		}

		startNewGame(false);

		while (true)
		{
			try
			{
				actionQueue.take().handle(currentPhase);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}

		gameThread = null;
		state = null;
		broadcastEventSender = null;
		for (Player p : players)
		{
			p.onDisconnectedFromGame();
		}
	}

	public GameState getCurrentGame()
	{
		return state;
	}

	void changePhase(Phase phase)
	{
		currentPhase = phase;
		getBroadcastEventSender().phaseChanged(currentPhase.asEnum());
		currentPhase.onStart();
	}

	EventSender getBroadcastEventSender()
	{
		return broadcastEventSender;
	}

	EventSender getPlayerEventQueue(int player)
	{
		return players.get(player).getEventSender();
	}

	private int getNextBeginnerPlayer(boolean doubleRound)
	{
		if (state == null)
			return 0;

		return (state.getBeginnerPlayer() + (doubleRound ? 0 : 1)) % 4;
	}

	void startNewGame(boolean doubleRound)
	{
		state = new GameState(getNextBeginnerPlayer(doubleRound));

		List<Card> cardsToDeal = new ArrayList<>(Card.getAll());
		Collections.shuffle(cardsToDeal);
		for (int p = 0; p < 4; p++)
		{
			for (int i = 0; i < 9; i++)
			{
				state.getPlayerCards(p).addCard(cardsToDeal.remove(0));
			}
		}
		state.setTalon(cardsToDeal);

		for (int i = 0; i < 4; i++)
		{
			getPlayerEventQueue(i).startGame(i, players.stream().map(Player::getName).collect(Collectors.toList()));
			getPlayerEventQueue(i).playerCards(state.getPlayerCards(i));
		}

		changePhase(new Bidding(this));
	}

	void sendStatistics()
	{
		Map<Team, List<AnnouncementStaticticsEntry>> statEntriesForTeams = new HashMap<>();
		Map<Team, Integer> gamePointsForTeams = new HashMap<>();
		int pointsForCallerTeam = 0;

		for (Team team : Team.values())
		{
			gamePointsForTeams.put(team, state.calculateGamePoints(team));

			List<AnnouncementStaticticsEntry> entriesForTeam = new ArrayList<>();
			statEntriesForTeams.put(team, entriesForTeam);

			for (Announcement announcement : Announcements.getAll())
			{
				int annoucementPoints = announcement.calculatePoints(state, team);

				pointsForCallerTeam += annoucementPoints * (team == Team.CALLER ? 1 : -1);

				if (annoucementPoints != 0)
				{
					int acl = state.getAnnouncementsState().isAnnounced(team, announcement) ? state.getAnnouncementsState().getContraLevel(team, announcement) : -1;
					AnnouncementContra ac = new AnnouncementContra(announcement, acl);
					entriesForTeam.add(new AnnouncementStaticticsEntry(ac, annoucementPoints));
				}
			}
		}

		int[] points = new int[4];
		for (int i = 0; i < 4; i++)
		{
			points[i] = -pointsForCallerTeam;
		}
		points[state.getPlayerPairs().getCaller()] += pointsForCallerTeam * 2;
		points[state.getPlayerPairs().getCalled()] += pointsForCallerTeam * 2;
		addPoints(points);

		for (int player = 0; player < 4; player++)
		{
			Team team = state.getPlayerPairs().getTeam(player);
			int selfGamePoints = gamePointsForTeams.get(team);
			int opponentGamePoints = gamePointsForTeams.get(team.getOther());
			List<AnnouncementStaticticsEntry> selfEntries = statEntriesForTeams.get(team);
			List<AnnouncementStaticticsEntry> opponentEntries = statEntriesForTeams.get(team.getOther());
			int sumPoints = pointsForCallerTeam * (team == Team.CALLER ? 1 : -1);
			getPlayerEventQueue(player).announcementStatistics(selfGamePoints, opponentGamePoints, selfEntries, opponentEntries, sumPoints, points);
		}
	}

	private void addPoints(int[] pointsToAdd)
	{
		for (int i = 0; i < 4; i++)
		{
			points[i] += pointsToAdd[i];
		}
	}
}

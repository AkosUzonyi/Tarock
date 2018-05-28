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

	private PlayerSeat.Map<Integer> points = new PlayerSeat.Map<>(0);

	private PlayerSeat.Map<Player> players = new PlayerSeat.Map<>();
	private EventSender broadcastEventSender;
	private BlockingQueue<Action> actionQueue = new LinkedBlockingQueue<>();

	private GameState state;
	private Phase currentPhase;

	private GameType gameType;

	private Thread gameThread;

	public GameSession(GameType gameType, List<Player> playerList)
	{
		if (players.size() != 4)
			throw new IllegalArgumentException();

		this.gameType = gameType;

		for (int i = 0; i < 4; i++)
		{
			players.put(PlayerSeat.fromInt(i), playerList.get(i));
		}
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

	public GameType getGameType()
	{
		return gameType;
	}

	@Override
	public void run()
	{
		broadcastEventSender = new BroadcastEventSender(players.values().stream().map(Player::getEventSender).collect(Collectors.toList()));
		for (PlayerSeat seat : PlayerSeat.getAll())
		{
			players.get(seat).onJoinedToGame(actionQueue, seat);
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

	EventSender getPlayerEventQueue(PlayerSeat player)
	{
		return players.get(player).getEventSender();
	}

	private PlayerSeat getNextBeginnerPlayer(boolean doubleRound)
	{
		if (state == null)
			return PlayerSeat.SEAT0;

		return doubleRound ? state.getBeginnerPlayer() : state.getBeginnerPlayer().nextPlayer();
	}

	void startNewGame(boolean doubleRound)
	{
		state = new GameState(gameType, getNextBeginnerPlayer(doubleRound));

		List<Card> cardsToDeal = new ArrayList<>(Card.getAll());
		Collections.shuffle(cardsToDeal);
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			for (int i = 0; i < 9; i++)
			{
				state.getPlayerCards(player).addCard(cardsToDeal.remove(0));
			}
		}
		state.setTalon(cardsToDeal);

		for (PlayerSeat player : PlayerSeat.getAll())
		{
			getPlayerEventQueue(player).startGame(player, players.values().stream().map(Player::getName).collect(Collectors.toList()));
			getPlayerEventQueue(player).playerCards(state.getPlayerCards(player));
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
			points[i] -= pointsForCallerTeam;
		}
		points[state.getPlayerPairs().getCaller().asInt()] += pointsForCallerTeam * 2;
		points[state.getPlayerPairs().getCalled().asInt()] += pointsForCallerTeam * 2;
		addPoints(points);

		for (PlayerSeat player : PlayerSeat.getAll())
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
		for (PlayerSeat p : PlayerSeat.getAll())
		{
			points.compute(p, (k, v) -> v + pointsToAdd[p.asInt()]);
		}
	}
}

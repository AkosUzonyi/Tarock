package com.tisza.tarock.game;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.message.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class GameSession
{
	public static final int ROUND_COUNT = 9;

	private int[] points = new int[4];

	private PlayerSeat.Map<Player> players = new PlayerSeat.Map<>();
	private EventSender broadcastEventSender;

	private GameState state;
	private Phase currentPhase;

	private GameHistory history;

	private GameType gameType;

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
		broadcastEventSender = new BroadcastEventSender(players.values().stream().map(Player::getEventSender).collect(Collectors.toList()));
		for (PlayerSeat seat : PlayerSeat.getAll())
		{
			players.get(seat).onAddedToGame(new GameSessionActionHandler(this), seat);
		}

		startNewGame(false);
	}

	public void stopSession()
	{
		state = null;
		broadcastEventSender = null;
		for (Player p : players)
		{
			p.onRemovedFromGame();
		}
	}

	public GameType getGameType()
	{
		return gameType;
	}

	public GameState getCurrentGame()
	{
		return state;
	}

	public GameHistory getCurrentHistory()
	{
		return history;
	}

	public Phase getCurrentPhase()
	{
		return currentPhase;
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

	EventSender getPlayerEventSender(PlayerSeat player)
	{
		return players.get(player).getEventSender();
	}

	public List<String> getPlayerNames()
	{
		return players.values().stream().map(Player::getName).collect(Collectors.toList());
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
		history = new GameHistory();

		for (PlayerSeat player : PlayerSeat.getAll())
		{
			getPlayerEventSender(player).startGame(player, getPlayerNames());
			getPlayerEventSender(player).playerCards(state.getPlayerCards(player));
			history.setOriginalPlayersCards(player, new ArrayList<>(state.getPlayerCards(player).getCards()));
		}

		changePhase(new Bidding(this));
	}

	void sendStatistics()
	{
		try
		{
			history.writeJSON(new OutputStreamWriter(System.out));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

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
				if (!gameType.hasParent(announcement.getGameType()))
					continue;

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

		int allTarockCountPoints = 0;
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			TarockCount tarockCountAnnouncement = state.getAnnouncementsState().getTarockCountAnnounced(player);
			int tarockCountPoints = tarockCountAnnouncement == null ? 0 : tarockCountAnnouncement.getPoints();
			points[player.asInt()] += tarockCountPoints * 4;
			allTarockCountPoints += tarockCountPoints;
		}

		for (int i = 0; i < 4; i++)
		{
			points[i] -= pointsForCallerTeam;
			points[i] -= allTarockCountPoints;
		}
		points[state.getPlayerPairs().getCaller().asInt()] += pointsForCallerTeam * 2;
		points[state.getPlayerPairs().getCalled().asInt()] += pointsForCallerTeam * 2;

		for (PlayerSeat player : PlayerSeat.getAll())
		{
			Team team = state.getPlayerPairs().getTeam(player);
			int selfGamePoints = gamePointsForTeams.get(team);
			int opponentGamePoints = gamePointsForTeams.get(team.getOther());
			List<AnnouncementStaticticsEntry> selfEntries = statEntriesForTeams.get(team);
			List<AnnouncementStaticticsEntry> opponentEntries = statEntriesForTeams.get(team.getOther());
			int sumPoints = pointsForCallerTeam * (team == Team.CALLER ? 1 : -1);
			getPlayerEventSender(player).announcementStatistics(selfGamePoints, opponentGamePoints, selfEntries, opponentEntries, sumPoints, points);
		}
	}
}

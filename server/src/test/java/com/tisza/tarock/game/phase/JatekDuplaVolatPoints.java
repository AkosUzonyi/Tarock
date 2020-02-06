package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.util.*;
import java.util.function.*;

import static junit.framework.TestCase.*;
import static org.junit.runners.Parameterized.*;

@RunWith(Parameterized.class)
public class JatekDuplaVolatPoints
{
	@Parameter(0)
	public int callerGamePoints;
	@Parameter(1)
	public int expectedResult;
	@Parameter(2)
	public Consumer<AnnouncementsState> announcerFunction;

	private Game game;

	// creates the test data
	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				{0, -3, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatek)},
				{20, -2, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatek)},
				{40, -1, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatek)},
				{60, 1, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatek)},
				{80, 2, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatek)},
				{94, 3, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatek)},
				{0, -7, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::dupla)},
				{20, -6, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::dupla)},
				{40, -5, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::dupla)},
				{60, -4, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::dupla)},
				{80, 4, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::dupla)},
				{94, 7, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::dupla)},
				{0, -9, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::volat)},
				{20, -8, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::volat)},
				{40, -7, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::volat)},
				{60, -6, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::volat)},
				{80, -6, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::volat)},
				{94, 6, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::volat)},
				{0, -13, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::duplaVolat)},
				{20, -12, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::duplaVolat)},
				{40, -11, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::duplaVolat)},
				{60, -10, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::duplaVolat)},
				{80, -2, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::duplaVolat)},
				{94, 10, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::duplaVolat)},
				{0, -5, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatek)},
				{20, -4, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatek)},
				{40, -2, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatek)},
				{60, 2, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatek)},
				{80, 4, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatek)},
				{94, 5, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatek)},
				{0, -9, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekOpponentDupla)},
				{20, -6, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekOpponentDupla)},
				{40, 2, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekOpponentDupla)},
				{60, 6, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekOpponentDupla)},
				{80, 8, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekOpponentDupla)},
				{94, 9, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekOpponentDupla)},
				{0, -11, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatekContradupla)},
				{20, -10, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatekContradupla)},
				{40, -9, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatekContradupla)},
				{60, -8, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatekContradupla)},
				{80, 8, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatekContradupla)},
				{94, 11, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::jatekContradupla)},
				{0, -13, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekContradupla)},
				{20, -12, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekContradupla)},
				{40, -10, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekContradupla)},
				{60, -6, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekContradupla)},
				{80, 10, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekContradupla)},
				{94, 13, (Consumer<AnnouncementsState>)(JatekDuplaVolatPoints::contrajatekContradupla)},
		};
		return Arrays.asList(data);
	}

	@Before
	public void createGameState()
	{
		game = new Game(GameType.PASKIEVICS, PlayerSeat.SEAT0, new ArrayList<>(Card.getAll()), new int[4], 1)
		{
			@Override
			public int calculateGamePoints(Team team)
			{
				return team == Team.CALLER ? callerGamePoints : 94 - callerGamePoints;
			}

			@Override
			public PlayerPairs getPlayerPairs()
			{
				return new PlayerPairs(PlayerSeat.SEAT0, PlayerSeat.SEAT1);
			}

			@Override
			public int getWinnerBid()
			{
				return 3;
			}

			@Override
			public Collection<Card> getWonCards(PlayerSeat player)
			{
				if (callerGamePoints == 0 || callerGamePoints == 94)
				{
					boolean isPlayerInCallerTeam = player.asInt() < 2;
					boolean isVolatForCaller = callerGamePoints == 94;
					return isPlayerInCallerTeam == isVolatForCaller ? Card.getAll() : Collections.EMPTY_LIST;
				}
				else
				{
					return Collections.singletonList(Card.getTarockCard(1));
				}
			}
		};
	}

	@Test
	public void test()
	{
		announcerFunction.accept(game.getAnnouncementsState());
		assertEquals(expectedResult, announcementPoints(game));
	}

	private static void jatek(AnnouncementsState as)
	{
		as.setContraLevel(Team.CALLER, Announcements.jatek, 0);
	}

	private static void dupla(AnnouncementsState as)
	{
		as.setContraLevel(Team.CALLER, Announcements.jatek, 0);
		as.setContraLevel(Team.CALLER, Announcements.dupla, 0);
	}

	private static void contrajatek(AnnouncementsState as)
	{
		as.setContraLevel(Team.CALLER, Announcements.jatek, 1);
	}

	private static void volat(AnnouncementsState as)
	{
		as.setContraLevel(Team.CALLER, Announcements.jatek, 0);
		as.setContraLevel(Team.CALLER, Announcements.volat, 0);
	}

	private static void duplaVolat(AnnouncementsState as)
	{
		as.setContraLevel(Team.CALLER, Announcements.jatek, 0);
		as.setContraLevel(Team.CALLER, Announcements.dupla, 0);
		as.setContraLevel(Team.CALLER, Announcements.volat, 0);
	}

	private static void contrajatekOpponentDupla(AnnouncementsState as)
	{
		as.setContraLevel(Team.CALLER, Announcements.jatek, 1);
		as.setContraLevel(Team.OPPONENT, Announcements.dupla, 0);
	}

	private static void contrajatekContradupla(AnnouncementsState as)
	{
		as.setContraLevel(Team.CALLER, Announcements.jatek, 1);
		as.setContraLevel(Team.CALLER, Announcements.dupla, 1);
	}

	private static void jatekContradupla(AnnouncementsState as)
	{
		as.setContraLevel(Team.CALLER, Announcements.jatek, 0);
		as.setContraLevel(Team.CALLER, Announcements.dupla, 1);
	}

	private static int announcementPoints(Game state)
	{
		final Announcement[] announcements = {Announcements.jatek, Announcements.dupla, Announcements.volat};

		int pointsForCallerTeam = 0;

		for (Team team : Team.values())
		{
			for (Announcement announcement : announcements)
			{
				int annoucementPoints = announcement.calculatePoints(state, team);
				pointsForCallerTeam += annoucementPoints * (team == Team.CALLER ? 1 : -1);
			}
		}

		return pointsForCallerTeam;
	}
}

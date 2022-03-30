package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.*;
import static org.junit.runners.Parameterized.*;

@RunWith(Parameterized.class)
public class GameLogicTest
{
	@Parameter(0)
	public GameType gameType;
	@Parameter(1)
	public String[] cards;
	@Parameter(2)
	public int[] seats;
	@Parameter(3)
	public String[] actions;
	@Parameter(4)
	public int[] expectedPoints;


	// creates the test data
	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				{
						GameType.PASKIEVICS,
						new String[] {"b4","t20","t18","t17","a4","b3","t10","t15","t7","d1","t9","t8","b1","d4","c1","t12","c2","c4","t16","t1","a5","t2","d5","d2","c3","t4","a1","t22","t3","t14","t21","a2","b5","t6","c5","d3","t19","t11","t13","t5","a3","b2",},
						new int[] {0,1,2,3,1,2,0,3,3,3,3,3,0,0,0,1,2,3,0,1,2,3,3,0,1,2,0,1,2,3,3,0,1,2,0,1,2,3,3,0,1,2,3,0,1,2,0,1,2,3,3,0,1,2,3,2,0,1},
						new String[] {"bid:p", "bid:p", "bid:p", "bid:3", "fold:a3", "fold:c3", "fold:a4", "fold:a2,d3,t6", "call:t20", "announce:trull", "announce:dupla", "announce:passz", "announce:negykiraly", "announce:volat", "announce:passz", "announce:passz", "announce:passz", "announce:passz", "play:t15", "play:t8", "play:t16", "play:t19", "play:t3", "play:t17", "play:t9", "play:t4", "play:t5", "play:t12", "play:t2", "play:t13", "play:t11", "play:t18", "play:d1", "play:t1", "play:t7", "play:b1", "play:b2", "play:t14", "play:b5", "play:b3", "play:c1", "play:a1", "play:c5", "play:t20", "play:c2", "play:d2", "play:b4", "play:d4", "play:d5", "play:t21", "play:t22", "play:t10", "play:c4", "play:a5", "newgame:", "newgame:", "newgame:", "newgame:",},
						new int[]{14,-14,-14,14},
				},
				{
						GameType.ILLUSZTRALT,
						new String[] {"t17","b5","t7","t8","d1","a3","t18","b4","t1","t13","t6","c5","c2","t4","c1","b1","a4","t10","t19","d2","t21","d5","t20","t15","d4","t12","b3","c4","c3","a5","a2","a1","t2","t3","t9","d3","b2","t22","t14","t11","t16","t5"},
						new int[] {0,1,2,3,2,1,3,0,0,0,1,2,2,2,3,0,0,0,1,2,2,2,3,0,0,0,1,2,3,0,1,2,3,2,3,0,1,0,1,2,3,0,1,2,3,2,3,0,1,2,3,0,1,2,3,0,1,0,1,2,3,0,1,2,3,3,0},
						new String[] {"bid:3","bid:p","bid:p","bid:p","fold:b3","fold:a4","fold:c4","fold:a3,b4,d1","call:t20","announce:passz","announce:passz","announce:trull","announce:negykiraly","announce:passz","announce:passz","announce:kezbevacakT4","announce:dupla","announce:passz","announce:passz","announce:kezbevacakT5","announce:volat","announce:passz","announce:passz","announce:kezbevacakT6","announce:ultimoCb5T7","announce:passz","announce:passz","announce:passz","announce:passz","play:t1","play:t4","play:t16","play:t2","play:t12","play:t3","play:t17","play:t6","play:t18","play:t10","play:t15","play:t5","play:t7","play:t11","play:t19","play:t9","play:t20","play:a1","play:t8","play:t13","play:t21","play:a2","play:t14","play:b1","play:d5","play:d3","play:t22","play:c1","play:b5","play:c2","play:d2","play:c3","play:b2","play:c5","play:d4","play:a5","newgame:","newgame:"},
						new int[]{64,-64,64,-64},
				},
				{
						GameType.MAGAS,
						new String[] {"d1","c5","b4","c3","t20","t21","c1","t15","a1","t8","t5","t17","t9","d4","t4","c2","t19","t2","b5","t3","t10","b1","t1","b2","a3","a5","t16","d5","t12","d3","t6","t7","t22","d2","t13","t18","a4","t14","a2","b3","t11","c4"},
						new int[] {0,1,2,3,3,2,1,0,3,3,0,0,1,2,3,3,0,1,1,2,3,0,0,1,2,3,2,3,0,1,1,2,3,0,0,1,2,3,3,0,1,2,0,1,2,3,3,0,1,2,3,0,1,2,1,2,3,0,3,2},
						new String[] {"bid:p","bid:p","bid:p","bid:3","fold:a4,a2,d3","fold:c4","fold:d4","fold:d1","call:t20","announce:passz","announce:trull","announce:passz","announce:passz","announce:passz","announce:dupla","announce:passz","announce:passz","announce:duplaK1","announce:passz","announce:passz","announce:passz","announce:passz","play:c1","play:c2","play:t16","play:t6","play:b2","play:t14","play:b4","play:t17","play:t4","play:t10","play:t13","play:t15","play:c3","play:t11","play:t3","play:t12","play:t7","play:t21","play:t5","play:t1","play:c5","play:t9","play:b1","play:t18","play:t22","play:t20","play:t8","play:a3","play:d5","play:a1","play:t19","play:b5","play:t2","play:a5","play:d2","play:b3","newgame:","newgame:"},
						new int[]{-11,11,11,-11},
				},
		};
		return Arrays.asList(data);
	}

	private static final Collection<PhaseEnum> availableActionCheckPhases = Arrays.asList(PhaseEnum.BIDDING, PhaseEnum.CALLING, PhaseEnum.ANNOUNCING);

	@Test
	public void test()
	{
		List<Card> deck = Arrays.stream(cards).map(Card::fromId).collect(Collectors.toList());
		Game game = new Game(gameType, deck);

		for (int i = 0; i < actions.length; i++)
		{
			PlayerSeat seat = PlayerSeat.fromInt(seats[i]);
			Action action = new Action(actions[i]);

			assertTrue("game.getTurn is false for the player executing the action: " + action.getId(), game.getTurn(seat));
			if (!action.equals(Action.announcePassz()) && availableActionCheckPhases.contains(game.getCurrentPhaseEnum()))
				assertTrue("executed action is not in the available actions list: " + action.getId(), game.getAvailableActions().contains(action));
			assertTrue("action is unsuccessful: " + action.getId(), game.processAction(seat, action));
		}

		int[] actualPoints = new int[4];
		for (int i = 0; i < 4; i++)
			actualPoints[i] = game.getPoints(PlayerSeat.fromInt(i));

		assertArrayEquals("mismatch in points", expectedPoints, actualPoints);
		assertEquals("trick count is wrong at the end", Game.TRICK_COUNT, game.getTrickCount());
	}
}

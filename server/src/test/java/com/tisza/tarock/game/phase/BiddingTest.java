package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.*;
import static org.junit.runners.Parameterized.*;

@RunWith(Parameterized.class)
public class BiddingTest
{
	@Parameter(0)
	public String[] bids;
	@Parameter(1)
	public String expectedBidWinnerPlayer;
	@Parameter(2)
	public String expectedWinnerBid;
	@Parameter(3)
	public String expectedInvit;
	@Parameter(4)
	public String expectedInviter;


	// creates the test data
	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				{new String[]{"3","-","-","-","","","","","","","","",""},"A","3","-","-"},
				{new String[]{"3","2","-","-","-","","","","","","","",""},"B","2","XX","A"},
				{new String[]{"3","2","-","-","2","-","","","","","","",""},"A","2","-","-"},
				{new String[]{"3","2","-","-","2","1","","","-","","","",""},"B","1","-","-"},
				{new String[]{"3","2","-","-","2","1","","","1","-","","",""},"A","1","-","-"},
				{new String[]{"3","2","-","-","2","1","","","1","0","","","-"},"B","0","-","-"},
				{new String[]{"3","2","-","-","2","1","","","1","0","","","0"},"A","0","-","-"},
				{new String[]{"3","2","-","-","2","0","","","-","","","",""},"B","0","XIX","B"},
				{new String[]{"3","2","-","-","2","0","","","0","","","",""},"A","0","XIX","B"},
				{new String[]{"3","2","-","-","1","-","","","","","","",""},"A","1","XIX","A"},
				{new String[]{"3","2","-","-","1","0","","","-","","","",""},"B","0","XIX","A"},
				{new String[]{"3","2","-","-","1","0","","","0","","","",""},"A","0","XIX","A"},
				{new String[]{"3","2","-","-","0","-","","","","","","",""},"A","0","XVIII","A"},
				{new String[]{"3","1","-","-","-","","","","","","","",""},"B","1","XIX","B"},
				{new String[]{"3","1","-","-","1","-","","","","","","",""},"A","1","XIX","B"},
				{new String[]{"3","1","-","-","1","0","","","-","","","",""},"B","0","XIX","B"},
				{new String[]{"3","1","-","-","1","0","","","0","","","",""},"A","0","XIX","B"},
				{new String[]{"3","1","-","-","0","-","","","","","","",""},"A","0","XIX","B"},
				{new String[]{"3","0","","","-","","","","","","","",""},"B","0","XVIII","B"},
				{new String[]{"3","0","","","0","","","","","","","",""},"A","0","XVIII","B"},
				{new String[]{"3","2","1","-","-","-","","","","","","",""},"C","1","-","-"},
				{new String[]{"3","2","1","-","1","-","-","","","","","",""},"A","1","-","-"},
				{new String[]{"3","2","1","-","1","0","-","","-","","","",""},"B","0","-","-"},
				{new String[]{"3","2","1","-","1","0","-","","0","","","",""},"A","0","-","-"},
				{new String[]{"3","2","1","-","1","-","0","","-","","","",""},"C","0","-","-"},
				{new String[]{"3","2","1","-","1","-","0","","0","","","",""},"A","0","-","-"},
				{new String[]{"3","2","1","-","0","-","-","","","","","",""},"A","0","XIX","A"},
				{new String[]{"3","2","1","-","-","1","-","","","","","",""},"B","1","-","-"},
				{new String[]{"3","2","1","-","-","1","0","","","-","","",""},"C","0","-","-"},
				{new String[]{"3","2","1","-","-","1","0","","","0","","",""},"B","0","-","-"},
				{new String[]{"3","2","1","-","-","0","-","","","","","",""},"B","0","XIX","B"},
				{new String[]{"3","2","0","","-","-","","","","","","",""},"C","0","XIX","C"},
				{new String[]{"3","2","0","","0","","","","","","","",""},"A","0","XIX","C"},
				{new String[]{"3","2","0","","-","0","","","","","","",""},"B","0","XIX","C"},
				{new String[]{"3","1","0","","-","-","","","","","","",""},"C","0","XIX","B"},
				{new String[]{"3","1","0","","0","","","","","","","",""},"A","0","XIX","B"},
				{new String[]{"3","1","0","","-","0","","","","","","",""},"B","0","XIX","B"},
				{new String[]{"2","-","-","-","","","","","","","","",""},"A","2","XIX","A"},
				{new String[]{"2","1","-","-","-","","","","","","","",""},"B","1","XIX","A"},
				{new String[]{"2","1","-","-","1","-","","","","","","",""},"A","1","XIX","A"},
				{new String[]{"2","1","-","-","1","0","","","-","","","",""},"B","0","XIX","A"},
				{new String[]{"2","1","-","-","1","0","","","0","","","",""},"A","0","XIX","A"},
				{new String[]{"2","1","-","-","0","-","","","","","","",""},"A","0","XIX","A"},
				{new String[]{"2","0","","","-","","","","","","","",""},"B","0","XIX","A"},
				{new String[]{"2","0","","","0","","","","","","","",""},"A","0","XIX","A"},
				{new String[]{"2","1","0","","-","-","","","","","","",""},"C","0","XIX","A"},
				{new String[]{"2","1","0","","0","","","","","","","",""},"A","0","XIX","A"},
				{new String[]{"2","1","0","","-","0","","","","","","",""},"B","0","XIX","A"},
				{new String[]{"1","-","-","-","","","","","","","","",""},"A","1","XVIII","A"},
				{new String[]{"1","0","","","-","","","","","","","",""},"B","0","XVIII","A"},
				{new String[]{"1","0","","","0","","","","","","","",""},"A","0","XVIII","A"},
				{new String[]{"0","","","","","","","","","","","",""},"A","0","-","-"},
				{new String[]{"-","-","-","-","","","","","","","","",""},"-","-","-","-"},
		};
		return Arrays.asList(data);
	}

	private String getPlayerName(PlayerSeat seat)
	{
		return seat == null ? "-" : "ABCD".substring(seat.asInt(), seat.asInt() + 1);
	}

	@Test
	public void test()
	{
		GameState game = new GameState(GameType.PASKIEVICS, Arrays.stream(PlayerSeat.getAll()).map(this::getPlayerName).collect(Collectors.toList()), PlayerSeat.SEAT0, new ArrayList<>(Card.getAll()), new int[4], 1);
		game.start();

		for (PlayerSeat seat : PlayerSeat.getAll())
		{
			for (int i = 0; i < 5; i++)
				game.getPlayerCards(seat).addCard(Card.getTarockCard(18 + i));
		}


		for (int i = 0; i < bids.length; i++)
		{
			String bidString = bids[i];

			if (bidString.isEmpty())
				continue;

			assertEquals(PhaseEnum.BIDDING, game.getCurrentPhase().asEnum());

			int bid = bidString.equals("-") ? -1 : Integer.parseInt(bidString);

			game.processAction(Action.bid(PlayerSeat.fromInt(i % 4), bid));
		}

		assertNotEquals(PhaseEnum.BIDDING, game.getCurrentPhase().asEnum());

		if (game.getCurrentPhase().asEnum() == PhaseEnum.INTERRUPTED)
		{
			assertEquals(expectedBidWinnerPlayer, "-");
			assertEquals(expectedWinnerBid, "-");
			assertEquals(expectedInvit, "-");
			assertEquals(expectedInviter, "-");
		}
		else
		{
			PlayerSeat bidWinnerPlayer = game.getBidWinnerPlayer();
			int winnerBid = game.getWinnerBid();
			Invitation invitation = game.getInvitSent();
			PlayerSeat invitingPlayer = game.getInvitingPlayer();

			assertEquals(expectedBidWinnerPlayer, getPlayerName(bidWinnerPlayer));
			assertEquals(Integer.parseInt(expectedWinnerBid), winnerBid);
			assertEquals(expectedInvit, invitation == null ? "-" : invitation.toString());
			assertEquals(expectedInviter, getPlayerName(invitingPlayer));
		}
	}
}

package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import org.junit.*;

import java.util.*;

public class CountBiddings
{
	private int[] x = new int[53];

	@Test
	public void test()
	{
		List<List<Integer>> biddings = countBiddings(Collections.EMPTY_LIST);
		System.out.println(biddings.size());
		for (List<Integer> bidding : biddings)
		{
			if (!isTested(bidding))
				System.out.println(bidding);
		}
		System.out.println(Arrays.toString(x));
	}

	private boolean isTested(List<Integer> bids)
	{
		bids = new ArrayList<>(bids);
		while (bids.remove((Integer)(-1)));

		int i = 0;

		for (Object[] testData : BiddingTest.data())
		{
			String[] testBidStrings = (String[])testData[0];
			List<Integer> testBids = new ArrayList<>();
			for (String testBidString : testBidStrings)
			{
				if (!testBidString.isEmpty() && !testBidString.equals("-"))
					testBids.add(Integer.parseInt(testBidString));
			}

			if (bids.equals(testBids))
			{
				x[i]++;
				return true;
			}

			i++;
		}
		return false;
	}

	private List<List<Integer>> countBiddings(List<Integer> bids)
	{
		List<List<Integer>> result = new ArrayList<>();

		Game game = createNewGame();

		Bidding bidding = (Bidding)game.getCurrentPhase();
		for (int bid : bids)
		{
			bidding.bid(bidding.getCurrentPlayer(), bid);
		}

		if (game.getCurrentPhaseEnum() != PhaseEnum.BIDDING)
		{
			result.add(bids);
			return result;
		}

		for (int newBid : bidding.getAvailableBids())
		{
			List<Integer> newBids = new ArrayList<>(bids);
			newBids.add(newBid);
			result.addAll(countBiddings(newBids));
		}

		return result;
	}

	private Game createNewGame()
	{
		Game game = new Game(GameType.PASKIEVICS, new ArrayList<>(Card.getAll()));

		for (PlayerSeat seat : PlayerSeat.getAll())
		{
			for (int i = 0; i < 5; i++)
				game.getPlayerCards(seat).addCard(Card.getTarockCard(18 + i));
		}

		return game;
	}
}

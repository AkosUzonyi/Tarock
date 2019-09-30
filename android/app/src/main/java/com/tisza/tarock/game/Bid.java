package com.tisza.tarock.game;

import com.tisza.tarock.gui.*;
import com.tisza.tarock.message.*;

public class Bid implements ActionButtonItem
{
	private int bid;

	public Bid(int bid)
	{
		if (bid < -1 || bid >= 4)
			throw new IllegalArgumentException("invalid bid: " + bid);

		this.bid = bid;
	}

	@Override
	public Action getAction()
	{
		return Action.bid(bid);
	}

	@Override
	public String toString()
	{
		return ResourceMappings.bidToName.get(bid);
	}
}

package com.tisza.tarock.game;

import com.tisza.tarock.gui.*;
import com.tisza.tarock.message.*;

public class Bid implements ActionButtonItem
{
	private int bid;
	private boolean keep;

	public Bid(int bid, boolean keep)
	{
		if (bid < -1 || bid >= 4)
			throw new IllegalArgumentException("invalid bid: " + bid);

		this.bid = bid;
		this.keep = keep;
	}

	@Override
	public Action getAction()
	{
		return Action.bid(bid);
	}

	@Override
	public String toString()
	{
		return ResourceMappings.bidToName.get(keep ? -2 : bid);
	}
}

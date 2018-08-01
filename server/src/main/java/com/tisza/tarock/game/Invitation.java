package com.tisza.tarock.game;

import com.tisza.tarock.game.card.*;

public enum Invitation
{
	NONE, XVIII, XIX, XX;

	public Card getCard()
	{
		switch (this)
		{
			case XVIII: return Card.getTarockCard(18);
			case XIX: return Card.getTarockCard(19);
			case XX: return Card.getTarockCard(20);
		}
		return null;
	}
}

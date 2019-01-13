package com.tisza.tarock.game.phase;

public enum PhaseEnum
{
	BIDDING, CHANGING, CALLING, ANNOUNCING, GAMEPLAY, END, INTERRUPTED;
	
	public boolean isAfter(PhaseEnum phase)
	{
		if (this == INTERRUPTED)
			return false;
		
		return ordinal() > phase.ordinal();
	}

	public String getID()
	{
		switch (this)
		{
			case BIDDING: return "bidding";
			case CHANGING: return "changing";
			case CALLING: return "calling";
			case ANNOUNCING: return "announcing";
			case GAMEPLAY: return "gameplay";
			case END: return "end";
			case INTERRUPTED: return "interrupted";
		}
		throw new RuntimeException();
	}

	public static PhaseEnum fromID(String id)
	{
		switch (id)
		{
			case "bidding": return BIDDING;
			case "changing": return CHANGING;
			case "calling": return CALLING;
			case "announcing": return ANNOUNCING;
			case "gameplay": return GAMEPLAY;
			case "end": return END;
			case "interrupted": return INTERRUPTED;
		}
		throw new IllegalArgumentException();
	}
}

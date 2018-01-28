package com.tisza.tarock;

public enum PhaseEnum
{
	BIDDING, CHANGING, CALLING, ANNOUNCING, GAMEPLAY, FINISHED, END, INTERRUPTED;
	
	public boolean isAfter(PhaseEnum phase)
	{
		if (this == INTERRUPTED)
			return false;
		
		return ordinal() > phase.ordinal();
	}
}

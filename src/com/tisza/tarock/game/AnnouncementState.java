package com.tisza.tarock.game;

public class AnnouncementState
{
	private PerTeam callerTeam = new PerTeam(Team.CALLER);
	private PerTeam opponentTeam = new PerTeam(Team.OPPONENT);
	
	public PerTeam team(Team t)
	{
		switch (t)
		{
			case CALLER:
				return callerTeam;
			case OPPONENT:
				return opponentTeam;
			default:
				throw new IllegalArgumentException(); 
		}
	}
	
	public static class PerTeam
	{
		private final Team team;
		private int contraLevel = -1;
		
		public PerTeam(Team t)
		{
			team = t;
		}
		
		public boolean announce()
		{
			if (!isAnnounced())
			{
				contraLevel = 0;
				return true;
			}
			else
			{
				return false;
			}
		}
		
		public boolean isAnnounced()
		{
			return contraLevel >= 0;
		}
		
		public void contra()
		{
			if (!isAnnounced())
				throw new IllegalStateException();
			
			contraLevel++;
		}
		
		public int getContraLevel()
		{
			if (!isAnnounced())
				throw new IllegalStateException();
			
			return contraLevel;
		}
		
		public Team getNextTeamToContra()
		{
			return getContraLevel() % 2 == 0 ? team.getOther() : team;
		}
	}
}

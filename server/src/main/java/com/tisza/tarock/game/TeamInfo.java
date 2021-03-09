package com.tisza.tarock.game;

public class TeamInfo
{
	private final PlayerSeat subject, target;

	public TeamInfo(PlayerSeat subject, PlayerSeat target)
	{
		if (subject == null || target == null)
			throw new NullPointerException();

		this.subject = subject;
		this.target = target;
	}

	public PlayerSeat getSubject()
	{
		return subject;
	}

	public PlayerSeat getTarget()
	{
		return target;
	}

	@Override
	public int hashCode()
	{
		return subject.asInt() << 16 | target.asInt();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof TeamInfo))
			return false;

		TeamInfo other = (TeamInfo)obj;
		return subject == other.subject && target == other.target;
	}
}

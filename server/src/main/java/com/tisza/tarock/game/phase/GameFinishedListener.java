package com.tisza.tarock.game.phase;

public interface GameFinishedListener
{
	public int[] getPlayerPoints();
	public void pointsEarned(int[] points);
	public void gameFinished();
	public void gameInterrupted();
}

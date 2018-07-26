package com.tisza.tarock.game.phase;

public interface GameFinishedListener
{
	public int[] pointsEarned(int[] points);
	public void gameFinished();
	public void gameInterrupted();
}

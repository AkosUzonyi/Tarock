package com.tisza.tarock.game.phase;

public interface GameFinishedListener
{
	public void gameFinished(int[] points);
	public void gameInterrupted();
}

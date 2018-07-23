package com.tisza.tarock.game;

public interface GameFinishedListener
{
	public void gameFinished(int[] points);
	public void gameInterrupted();
}

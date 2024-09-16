package com.tisza.tarock.server.player.bot;

import lombok.*;

@Data
public class Personality
{
	private Intensity selfCalling;
	private Intensity bidWithPagat;
	private Intensity bidWhenWeek; //TODO use it
	private Intensity keepCardsWhenWeak;
	private Intensity invite; //TODO use it

	public enum Intensity {
		NONE, RARE, FEW, OFTEN, ALWAYS
	}
}

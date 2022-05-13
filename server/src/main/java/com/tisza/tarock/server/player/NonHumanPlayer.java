package com.tisza.tarock.server.player;

import com.tisza.tarock.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.server.database.*;

import java.util.concurrent.*;

public abstract class NonHumanPlayer extends Player
{
	private EventHandler eventHandler;

	protected final int delay, extraDelay;
	protected boolean historyMode;
	protected Action lastActionInHistoryMode;

	public NonHumanPlayer(User user, int delay, int extraDelay)
	{
		super(user);
		this.delay = delay;
		this.extraDelay = extraDelay;
	}

	protected void enqueueActionDelayed(Action action, int delayMillis)
	{
		if (historyMode)
		{
			lastActionInHistoryMode = action;
			return;
		}

		Main.GAME_EXECUTOR_SERVICE.schedule(() -> doAction(action), delayMillis, TimeUnit.MILLISECONDS);
	}

	@Override
	public void handleEvent(Event event)
	{
		event.handle(eventHandler);
	}

	protected void setEventHandler(EventHandler eventHandler){
		this.eventHandler = eventHandler;
	}
}

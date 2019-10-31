package com.tisza.tarock.message;

import com.tisza.tarock.game.*;
import com.tisza.tarock.server.player.*;

public interface Game
{
	public void action(PlayerSeat seat, Action action);
	public void requestHistory(Player player);
}

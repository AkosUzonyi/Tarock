package com.tisza.tarock.server;

import java.util.*;

import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;

public class LoginManager
{
	private GameSession game;
	private Map<LoginHandler, Connection> pendingLoginHandlers = new HashMap<LoginHandler, Connection>();

	public LoginManager(GameSession g)
	{
		game = g;
	}
	
	public void newConnection(Connection c)
	{
		LoginHandler handler = new LoginHandler(this);
		c.addPacketHandler(handler);
		pendingLoginHandlers.put(handler, c);
	}

	public void loginAuthorized(LoginHandler loginHandler, String name)
	{
		Connection authorizedConnection = pendingLoginHandlers.remove(loginHandler);
		authorizedConnection.removePacketHandler(loginHandler);
		game.loginAuthorized(name, authorizedConnection);
	}

	public void loginFailed(LoginHandler loginHandler)
	{
		Connection failedConnection = pendingLoginHandlers.remove(loginHandler);
		if (failedConnection.isAlive())
		{
			failedConnection.sendPacket(new PacketLoginFailed("Authetication error"));
			failedConnection.closeRequest();
		}
	}

	public void close()
	{
		for (Connection connectionToClose : pendingLoginHandlers.values())
		{
			connectionToClose.closeRequest();
		}
	}
}

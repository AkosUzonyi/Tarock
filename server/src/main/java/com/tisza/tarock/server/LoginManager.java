package com.tisza.tarock.server;

import com.tisza.tarock.net.Connection;
import com.tisza.tarock.net.packet.PacketLoginFailed;
import com.tisza.tarock.net.packet.PacketLoginFailed.Reason;

import java.util.HashMap;
import java.util.Map;

public class LoginManager
{
	private ClientManager game;
	private Map<LoginHandler, Connection> pendingLoginHandlers = new HashMap<LoginHandler, Connection>();

	public LoginManager(ClientManager g)
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

	public void loginFailed(LoginHandler loginHandler, Reason reason)
	{
		Connection failedConnection = pendingLoginHandlers.remove(loginHandler);
		if (failedConnection.isAlive())
		{
			failedConnection.sendPacket(new PacketLoginFailed(reason));
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

package com.tisza.tarock.server;

import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;

public class LoginHandler implements PacketHandler
{
	private GameSession game;
	private Connection connection;
	private String name = null;
	
	public LoginHandler(GameSession g, Connection c)
	{
		game = g;
		connection = c;
	}
	
	public void handlePacket(Packet p)
	{
		if (p instanceof PacketLogin)
		{
			PacketLogin pl = (PacketLogin)p;
			name = pl.getName();
			game.loginAuthorized(name, connection);
		}
	}
	
	private void loginFailed(String desc)
	{
		name = null;
		connection.sendPacket(new PacketLoginFailed(desc));
		connection.closeRequest();
	}

	public void connectionClosed()
	{
		if (name != null)
		{
			game.logout(name);
		}
	}
}

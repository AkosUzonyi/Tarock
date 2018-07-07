package com.tisza.tarock;

import com.tisza.tarock.server.*;

import java.io.*;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		Server server = new Server(8128);
		server.start();
		System.in.read();
		server.stop();
	}
}

package com.tisza.tarock;

import com.tisza.tarock.server.*;

import java.io.*;
import java.sql.*;

public class Main
{
	public static void main(String[] args)
	{
		File keystoreFile = new File("/usr/share/tarock/keystore");
		File fbUsersDBFile = new File("/var/lib/tarock/fbusers.db");
		Server server = new Server(8128, keystoreFile, fbUsersDBFile);
		server.start();
	}
}

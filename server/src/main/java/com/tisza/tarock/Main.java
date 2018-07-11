package com.tisza.tarock;

import com.tisza.tarock.server.*;
import sun.rmi.runtime.*;

import java.io.*;

public class Main
{
	public static void main(String[] args)
	{
		File keystoreFile = new File("/usr/share/tarock/keystore");
		Server server = new Server(8128, keystoreFile);
		server.start();
	}
}

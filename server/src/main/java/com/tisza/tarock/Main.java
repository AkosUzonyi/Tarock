package com.tisza.tarock;

import com.tisza.tarock.server.*;
import io.reactivex.*;

import java.io.*;

public class Main
{
	public static void main(String[] args)
	{
		File staticDir = new File("/usr/share/tarock");
		File dataDir = new File("/var/lib/tarock");
		Server server = new Server(8128, staticDir, dataDir);
		server.start();
	}
}

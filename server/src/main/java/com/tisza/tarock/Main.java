package com.tisza.tarock;

import com.tisza.tarock.server.*;
import com.tisza.tarock.server.net.*;
import org.apache.log4j.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.util.concurrent.*;

public class Main
{
	private static final Logger log = Logger.getLogger(Main.class);

	public static final File STATIC_DIR = new File("/usr/share/tarock");
	public static final File DATA_DIR = new File("/var/lib/tarock");
	public static final ScheduledExecutorService GAME_EXECUTOR_SERVICE = new GameExecutorService();

	public static void main(String[] args) throws Exception
	{
		Server server = new Server(8128);
		server.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			GAME_EXECUTOR_SERVICE.shutdownNow();
			server.stop();
			try
			{
				GAME_EXECUTOR_SERVICE.awaitTermination(1, TimeUnit.SECONDS);
				server.awaitTermination(1000);
			}
			catch (InterruptedException e)
			{
				log.warn("Shutdown hook interrupted");
			}
		}));

		Thread.sleep(5000);

		stressTest();
	}

	private static void stressTest() throws Exception
	{
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
			}
		} };

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		SSLSocketFactory socketFactory = sc.getSocketFactory();
		for (int i = 0; i < 20; i++)
		{
			Thread.sleep(200);
			Socket socket = socketFactory.createSocket();
			socket.connect(new InetSocketAddress("dell", 8128), 1000);
			int userID = i + 100;
			ProtoConnection protoConnection = new ProtoConnection(socket, GAME_EXECUTOR_SERVICE, false);
			StressConnection stressConnection = new StressConnection(protoConnection, userID, i % 4 == 0);
			protoConnection.start();
			stressConnection.start();
		}
	}
}

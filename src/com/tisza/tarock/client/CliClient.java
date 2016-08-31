package com.tisza.tarock.client;

import java.net.*;
import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;

public class CliClient implements PacketHandler
{
	private Connection conncection;
	private Scanner sc = new Scanner(System.in);
	private List<String> names;
	private PlayerCards pc;
	int player = -1;
	
	public CliClient(String host, int port, String name) throws Exception
	{
		conncection = new Connection(new Socket(host, port));
		conncection.sendPacket(new PacketLogin(name));
		conncection.addPacketHandler(this);
	}

	public void handlePacket(Packet p)
	{
		if (p instanceof PacketStartGame)
		{
			PacketStartGame packet = ((PacketStartGame)p);
			player = packet.getPlayerID();
			System.out.println("Game started");
			System.out.println("Players: ");
			names = packet.getNames();
			for (String name : names)
			{
				System.out.print(name + ", ");
			}
			System.out.println();
		}
		if (p instanceof PacketPlayerCards)
		{
			PacketPlayerCards packet = ((PacketPlayerCards)p);
			pc = packet.getPlayerCards();
			System.out.println(pc.getCards());
		}
		if (p instanceof PacketAvailableBids)
		{
			PacketAvailableBids packet = ((PacketAvailableBids)p);
			List<Integer> bids = packet.getAvailableBids();
			System.out.println("Choose bid: " + bids);
			conncection.sendPacket(new PacketBid(sc.nextInt(), player));
		}
		if (p instanceof PacketBid)
		{
			PacketBid packet = ((PacketBid)p);
			System.out.println(names.get(packet.getPlayer()) + " bidded: " + packet.getBid());
		}
		if (p instanceof PacketChange)
		{
			PacketChange packet = ((PacketChange)p);
			System.out.println("Cards from talon: " + packet.getCards());
			List<Card> cardsFromTalon = new ArrayList<Card>(packet.getCards());
			List<Card> cardsToSkart = new ArrayList<Card>();
			for (int i = 0; i < packet.getCards().size(); i++)
			{
				int index = sc.nextInt();
				cardsToSkart.add(index < 9 ? pc.getCards().get(index) : cardsFromTalon.get(index - 9));
			}
			conncection.sendPacket(new PacketChange(cardsToSkart, player));
			pc.getCards().removeAll(cardsToSkart);
			cardsFromTalon.removeAll(cardsToSkart);
			pc.getCards().addAll(cardsFromTalon);
		}
		if (p instanceof PacketAvailableCalls)
		{
			PacketAvailableCalls packet = ((PacketAvailableCalls)p);
			List<Card> calls = packet.getAvailableCalls();
			System.out.println("Choose card to call: " + calls);
			int tarock = sc.nextInt();
			conncection.sendPacket(new PacketCall(new TarockCard(tarock), player));
		}
		if (p instanceof PacketCall)
		{
			PacketCall packet = ((PacketCall)p);
			System.out.println(names.get(packet.getPlayer()) + "called: " + packet.getCalledCard());
		}
		if (p instanceof PacketTurn)
		{
			PacketTurn packet = ((PacketTurn)p);
			if (packet.getPlayer() == player)
			{
				if (packet.getType() == PacketTurn.Type.ANNOUNCE)
				{
					conncection.sendPacket(new PacketAnnounce(null, player));
				}
				if (packet.getType() == PacketTurn.Type.PLAY_CARD)
				{
					System.out.println("Choose a card to play:");
					System.out.println(pc.getCards());
					int index = sc.nextInt();
					conncection.sendPacket(new PacketPlayCard(pc.getCards().get(index), player));
				}
			}
		}
		if (p instanceof PacketPlayCard)
		{
			PacketPlayCard packet = ((PacketPlayCard)p);
			String name = names.get(packet.getPlayer());
			Card c = packet.getCard();
			System.out.println(name + " played: " + c);
			cardsPlayed++;
			if (cardsPlayed % 4 == 0)
			{
				System.out.println("---");
			}
		}
	}
	
	int cardsPlayed = 0;

	public void connectionClosed()
	{
	}
}

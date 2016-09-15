package com.tisza.tarock.cli;

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
	List<Card> cardsFromTalon;
	
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
			System.out.println("Available bids: " + bids);
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
			cardsFromTalon = new ArrayList<Card>(packet.getCards());
		}
		if (p instanceof PacketAvailableCalls)
		{
			PacketAvailableCalls packet = ((PacketAvailableCalls)p);
			List<Card> calls = packet.getAvailableCalls();
			System.out.println("Availabe calls : " + calls);
		}
		if (p instanceof PacketCall)
		{
			PacketCall packet = ((PacketCall)p);
			System.out.println(names.get(packet.getPlayer()) + " called: " + packet.getCalledCard());
		}
		if (p instanceof PacketTurn)
		{
			PacketTurn packet = ((PacketTurn)p);
			if (packet.getPlayer() == player)
			{
				if (packet.getType() == PacketTurn.Type.BID)
				{
					System.out.print("Choose bid: ");
					conncection.sendPacket(new PacketBid(sc.nextInt(), player));
				}
				if (packet.getType() == PacketTurn.Type.CHANGE)
				{
					List<Card> cardsToSkart = new ArrayList<Card>();
					for (int i = 0; i < cardsFromTalon.size(); i++)
					{
						int index = sc.nextInt();
						int cardsSize = pc.getCards().size();
						cardsToSkart.add(index < cardsSize ? pc.getCards().get(index) : cardsFromTalon.get(index - cardsSize));
					}
					conncection.sendPacket(new PacketChange(cardsToSkart, player));
					pc.getCards().removeAll(cardsToSkart);
					cardsFromTalon.removeAll(cardsToSkart);
					pc.getCards().addAll(cardsFromTalon);
				}
				if (packet.getType() == PacketTurn.Type.CALL)
				{
					System.out.print("Choose card to call: ");
					int tarock = sc.nextInt();
					conncection.sendPacket(new PacketCall(new TarockCard(tarock), player));
				}
				if (packet.getType() == PacketTurn.Type.ANNOUNCE)
				{
					conncection.sendPacket(new PacketAnnounce(null, player));
				}
				if (packet.getType() == PacketTurn.Type.PLAY_CARD)
				{
					//conncection.sendPacket(new PacketPlayCard(pc.getCards().get(0), player));
					
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
			
			if (packet.getPlayer() == player)
			{
				pc.removeCard(packet.getCard());
			}
			
			String name = names.get(packet.getPlayer());
			Card c = packet.getCard();
			System.out.println(name + " played: " + c);
			cardsPlayed++;
			if (cardsPlayed % 4 == 0)
			{
				System.out.println("---");
			}
		}
		if (p instanceof PacketAnnouncementStatistics)
		{
			/*PacketAnnouncementStatistics packet = ((PacketAnnouncementStatistics)p);
			for (PacketAnnouncementStatistics.Entry entry : packet.getEntries())
			{
				System.out.println(entry.getAnnouncement().getClass().getSimpleName());
				System.out.println(entry.getContraLevel());
				System.out.println(entry.getPoints());
			}*/
		}
		if (p instanceof PacketSkartTarock)
		{
			PacketSkartTarock packet = ((PacketSkartTarock)p);
			for (int i = 0; i < 4; i++)
			{
				
				System.out.print(packet.getCounts()[i] + " ");
			}
			System.out.println();
		}
		if (p instanceof PacketReadyForNewGame)
		{
			System.out.print("Press enter for new game...");
			sc.nextLine();
			conncection.sendPacket(new PacketReadyForNewGame());
		}
	}
	
	int cardsPlayed = 0;

	public void connectionClosed()
	{
	}
}

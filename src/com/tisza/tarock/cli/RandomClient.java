package com.tisza.tarock.cli;

import java.net.*;
import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;

public class RandomClient implements PacketHandler
{
	private Connection conncection;
	private PlayerCards myCards;
	private Card firstCard = null;
	private int playedCardCount = 0;
	int player = -1;
	
	public RandomClient(String host, int port, String name) throws Exception
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
		}
		if (p instanceof PacketPlayerCards)
		{
			PacketPlayerCards packet = ((PacketPlayerCards)p);
			myCards = packet.getPlayerCards();
		}
		if (p instanceof PacketAvailableBids)
		{
			PacketAvailableBids packet = ((PacketAvailableBids)p);
			List<Integer> bids = packet.getAvailableBids();
			conncection.sendPacket(new PacketBid(bids.get(new Random().nextInt(bids.size())), player));
		}
		if (p instanceof PacketChange)
		{
			PacketChange packet = ((PacketChange)p);
			myCards.getCards().addAll(packet.getCards());
			List<Card> cardsToSkart = new ArrayList<Card>();
			List<Card> skartableCards = myCards.filter(new SkartableCardFilter());
			while (cardsToSkart.size() < packet.getCards().size())
			{
				cardsToSkart.add(skartableCards.remove(0));
			}
			myCards.getCards().removeAll(cardsToSkart);
			conncection.sendPacket(new PacketChange(cardsToSkart, player));
		}
		if (p instanceof PacketAvailableCalls)
		{
			PacketAvailableCalls packet = ((PacketAvailableCalls)p);
			List<Card> calls = packet.getAvailableCalls();
			conncection.sendPacket(new PacketCall(calls.get(0), player));
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
					try
					{
						Thread.sleep(300);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					
					conncection.sendPacket(new PacketPlayCard(myCards.getPlaceableCards(firstCard).get(0), player));
				}
			}
		}
		if (p instanceof PacketPlayCard)
		{
			PacketPlayCard packet = ((PacketPlayCard)p);
			
			if (playedCardCount % 4 == 0)
			{
				firstCard = packet.getCard();
			}
			
			if (packet.getPlayer() == player)
			{
				myCards.removeCard(packet.getCard());
			}
			
			playedCardCount++;
		}
		if (p instanceof PacketReadyForNewGame)
		{
			conncection.sendPacket(new PacketReadyForNewGame());
		}
	}
	
	int cardsPlayed = 0;

	public void connectionClosed()
	{
	}
}

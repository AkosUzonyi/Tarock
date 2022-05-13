package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.util.*;
import com.tisza.tarock.message.*;
import org.junit.*;

import java.util.*;
import java.util.stream.*;

public class BotPlayerTest
{

	@Test
	public void testCardPlayNotBeat()
	{
		ExtendedBotPlayer player = new ExtendedBotPlayer();

		player.setGame(null,PlayerSeat.SEAT0);
		player.handleEvent(Event.phaseChanged(PhaseEnum.GAMEPLAY));
		player.handleEvent(Event.playerCards(new PlayerCards(Util.createCardList(
				"t22", "b3", "a2", "t2", "c4", "t5", "d5", "t18", "t20")), false));

		player.handleEvent(Event.playCard(PlayerSeat.SEAT1, Card.fromId("a1")));
		player.handleEvent(Event.playCard(PlayerSeat.SEAT2, Card.fromId("t3")));
		player.handleEvent(Event.playCard(PlayerSeat.SEAT3, Card.fromId("a5")));
		player.handleEvent(Event.turn(PlayerSeat.SEAT0));

		assertActions(player, Collections.singletonList(Action.play(Card.fromId("a2"))));
	}

	@Test
	public void testCardPlayBeat()
	{
		ExtendedBotPlayer player = new ExtendedBotPlayer();

		player.setGame(null,PlayerSeat.SEAT0);
		player.handleEvent(Event.phaseChanged(PhaseEnum.GAMEPLAY));
		player.handleEvent(Event.playerCards(new PlayerCards(Util.createCardList(
				"t22", "b3", "c2", "t2", "c4", "t5", "d5", "t18", "t20")), false));

		player.handleEvent(Event.playCard(PlayerSeat.SEAT1, Card.fromId("a1")));
		player.handleEvent(Event.playCard(PlayerSeat.SEAT2, Card.fromId("t3")));
		player.handleEvent(Event.playCard(PlayerSeat.SEAT3, Card.fromId("a5")));
		player.handleEvent(Event.turn(PlayerSeat.SEAT0));

		assertActions(player, Collections.singletonList(Action.play(Card.fromId("t5"))));
	}

	@Test
	public void testCardPlayBeatColor()
	{
		ExtendedBotPlayer player = new ExtendedBotPlayer();

		player.setGame(null,PlayerSeat.SEAT0);
		player.handleEvent(Event.phaseChanged(PhaseEnum.GAMEPLAY));
		player.handleEvent(Event.playerCards(new PlayerCards(Util.createCardList(
				"t22", "a4", "a5", "t2", "c4", "t5", "d5", "t18", "t20")), false));

		player.handleEvent(Event.playCard(PlayerSeat.SEAT1, Card.fromId("a1")));
		player.handleEvent(Event.playCard(PlayerSeat.SEAT2, Card.fromId("a3")));
		player.handleEvent(Event.playCard(PlayerSeat.SEAT3, Card.fromId("a2")));
		player.handleEvent(Event.turn(PlayerSeat.SEAT0));

		assertActions(player, Collections.singletonList(Action.play(Card.fromId("a4"))));
	}
	@Test
	public void testInvokeCall19()
	{
		ExtendedBotPlayer player = new ExtendedBotPlayer();

		player.handleEvent(Event.playerCards(new PlayerCards(Util.createCardList(
				"t22", "a3", "a2", "t2", "c4", "t5", "d5", "t18", "t20")), false));

		availableCalls(player);

		assertActions(player,Collections.singletonList(Action.call(Card.fromId("t19"))));
	}

	@Test
	public void testInvokeCall20()
	{
		ExtendedBotPlayer player = new ExtendedBotPlayer();

		player.handleEvent(Event.playerCards(new PlayerCards(Util.createCardList(
				"t22", "a3", "a2", "t2", "c4", "t5", "d5", "t18", "a1")), false));

		availableCalls(player);

		assertActions(player,Collections.singletonList(Action.call(Card.fromId("t20"))));
	}

	@Test
	public void testInvokeCall16()
	{
		ExtendedBotPlayer player = new ExtendedBotPlayer();

		player.handleEvent(Event.playerCards(new PlayerCards(Util.createCardList(
				"t22", "a3", "t14", "t2", "t15", "t20", "t19", "t18", "t17")), false));

		availableCalls(player);

		assertActions(player,Collections.singletonList(Action.call(Card.fromId("t16"))));
	}

	private void availableCalls(ExtendedBotPlayer player){
		List<String> ids = new ArrayList<>();
		for(int i = 2;i<21;i++){
			ids.add("t"+i);
		}
		player.handleEvent(Event.availableCalls(Util.createCardList(ids.toArray(new String[0]))));
	}

	private void assertActions(ExtendedBotPlayer player,List<Action> expectedActions)
	{
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			Assert.fail("sleep failed.");
		}

		Assert.assertEquals("wrong actions or wrong order",
				expectedActions.stream().map(Action::getId).collect(Collectors.toList()),
				player.getActions().stream().map(Action::getId).collect(Collectors.toList()));
	}
}

package com.tisza.tarock.game;

import com.tisza.tarock.api.model.*;

import java.util.*;

public class GameState
{
	public int beginnerDirection;
	public GameType type;
	public PhaseEnum phase;
	public boolean canThrowCards;
	public List<String> availableActions = new ArrayList<>();
	public Integer previousTrickWinnerDirection;
	public List<GameStateDTO.PlayerInfo> playersRotated = new ArrayList<>();
	public GameStateDTO.Statistics statistics = new GameStateDTO.Statistics();
}

package com.tisza.tarock.spring;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.spring.dto.*;
import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.model.UserDB;
import com.tisza.tarock.spring.repository.*;
import com.tisza.tarock.spring.service.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.*;
import org.springframework.web.util.*;

import java.net.*;
import java.util.*;
import java.util.stream.*;

@RestController
@Scope("session")
public class TestController
{
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private IdpUserRepository idpUserRepository;
	@Autowired
	private GameSessionRepository gameSessionRepository;
	@Autowired
	private PlayerRepository playerRepository;
	@Autowired
	private GameRepository gameRepository;
	@Autowired
	private ChatRepository chatRepository;
	@Autowired
	private ListDeferredResultService<ChatDB> chatDeferredResultService;
	@Autowired
	private ListDeferredResultService<ActionDB> actionDeferredResultService;

	Logger logger = LoggerFactory.getLogger(TestController.class);

	@GetMapping("/idp")
	public ResponseEntity<IdpUserDB> idp() throws InterruptedException
	{
		Thread.sleep(1000);
		Iterable<IdpUserDB> idpusers = idpUserRepository.findAll();
		return new ResponseEntity<>(idpusers.iterator().next(), HttpStatus.OK);
	}

	private GameSessionDB findGameSessionOrThrow(int gameSessionId)
	{
		Optional<GameSessionDB> gameSessionDB = gameSessionRepository.findById(gameSessionId);
		if (gameSessionDB.isEmpty() || gameSessionDB.get().state.equals("deleted"))
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);

		return gameSessionDB.get();
	}

	@GetMapping("/users/{userID}")
	public ResponseEntity<UserDB> user(@PathVariable int userID)
	{
		Optional<UserDB> user = userRepository.findById(userID);
		if (user.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(user.get(), HttpStatus.OK);
	}

	@GetMapping("/gameSessions")
	public ResponseEntity<List<GameSessionDB>> gameSessions()
	{
		return new ResponseEntity<>(gameSessionRepository.findActive(), HttpStatus.OK);
	}

	@GetMapping("/gameSessions/{gameSessionID}")
	public ResponseEntity<GameSessionDB> gameSession(@PathVariable int gameSessionID)
	{
		//TODO: return in deleted state or 404?
		return new ResponseEntity<>(findGameSessionOrThrow(gameSessionID), HttpStatus.OK);
	}

	@PostMapping("/gameSessions")
	public ResponseEntity<Void> createGameSession(@Validated @RequestBody CreateGameSessionDTO createGameSessionDTO, UriComponentsBuilder uriComponentsBuilder)
	{
		//TODO: request parameters not null

		GameSessionDB gameSession = new GameSessionDB();
		gameSession.type = createGameSessionDTO.type; //TODO: validate type
		gameSession.state = "lobby";
		gameSession.players = new ArrayList<>();
		gameSession.doubleRoundType = createGameSessionDTO.doubleRoundType; //TODO: validate type
		gameSession.doubleRoundData = 0;
		gameSession.currentGameId = null;
		gameSession.createTime = System.currentTimeMillis();

		if (gameSession.type == null || gameSession.doubleRoundType == null)
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		gameSession = gameSessionRepository.save(gameSession);

		PlayerDB creatorPlayer = new PlayerDB();
		creatorPlayer.gameSessionId = gameSession.id;
		creatorPlayer.ordinal = 0;
		creatorPlayer.userId = 4; //TODO
		creatorPlayer.points = 0;
		gameSession.players.add(creatorPlayer);

		gameSession = gameSessionRepository.save(gameSession);

		URI uri = uriComponentsBuilder.path("/gameSessions/{gameSessionID}").buildAndExpand(gameSession.id).toUri();
		return ResponseEntity.created(uri).build();
	}

	@DeleteMapping("/gameSessions/{gameSessionID}")
	public ResponseEntity<Void> deleteGameSession(@PathVariable int gameSessionID)
	{
		Optional<GameSessionDB> gameSessionOptional = gameSessionRepository.findById(gameSessionID);
		if (gameSessionOptional.isEmpty() || gameSessionOptional.get().state.equals("deleted"))
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		GameSessionDB gameSession = findGameSessionOrThrow(gameSessionID);

		int userId = 4; //TODO
		boolean containsUser = gameSession.players.stream().anyMatch(p -> p.userId == userId);
		if (!containsUser)
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		if (gameSession.state.equals("lobby"))
			gameSession.players.clear();

		gameSession.state = "deleted";

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	@PostMapping("/gameSessions/{gameSessionID}/join")
	public ResponseEntity<Void> joinGameSession(@PathVariable int gameSessionID)
	{
		GameSessionDB gameSession = findGameSessionOrThrow(gameSessionID);

		if (!gameSession.state.equals("lobby"))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		int userId = 4; //TODO
		boolean containsUser = gameSession.players.stream().anyMatch(p -> p.userId == userId);
		if (containsUser)
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		PlayerDB player = new PlayerDB();
		player.gameSessionId = gameSessionID;
		player.ordinal = gameSession.players.size();
		player.points = 0;
		player.userId = userId;
		gameSession.players.add(player);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("/gameSessions/{gameSessionID}/leave")
	public ResponseEntity<Void> leaveGameSession(@PathVariable int gameSessionID)
	{
		GameSessionDB gameSession = findGameSessionOrThrow(gameSessionID);

		if (!gameSession.state.equals("lobby"))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		int userId = 4; //TODO
		gameSession.players.removeIf(p -> p.userId == userId);

		if (gameSession.players.isEmpty())
			gameSession.state = "deleted";

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("/gameSessions/{gameSessionID}/start")
	public ResponseEntity<Void> startGameSession(@PathVariable int gameSessionID)
	{
		GameSessionDB gameSession = findGameSessionOrThrow(gameSessionID);

		if (!gameSession.state.equals("lobby"))
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		int userId = 4; //TODO
		if (gameSession.players.stream().noneMatch(p -> p.userId == userId))
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		int playerCount = gameSession.players.size();
		while (playerCount < 4)
		{
			PlayerDB bot = new PlayerDB();
			bot.gameSessionId = gameSessionID;
			bot.ordinal = playerCount++;
			bot.userId = 4 - playerCount + 1;
			bot.points = 0;
			gameSession.players.add(bot);
		}

		Collections.shuffle(gameSession.players);
		for (int i = 0; i < playerCount; i++)
			gameSession.players.get(i).ordinal = i;

		gameSession.state = "game";
		startNewGame(gameSession, 0);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	private GameDB findGameOrThrow(int gameId)
	{
		Optional<GameDB> gameDB = gameRepository.findById(gameId);
		if (gameDB.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);

		return gameDB.get();
	}

	private void startNewGame(GameSessionDB gameSession, int beginnerPlayer)
	{
		if (!gameSession.state.equals("game"))
			throw new IllegalStateException();

		GameDB gameDB = new GameDB();
		gameDB.gameSessionId = gameSession.id;
		gameDB.actions = new ArrayList<>();
		gameDB.beginnerPlayer = beginnerPlayer;
		gameDB.createTime = System.currentTimeMillis();
		gameDB = gameRepository.save(gameDB);

		gameSession.currentGameId = gameDB.id;

		List<Card> deck = new ArrayList<>(Card.getAll());
		Collections.shuffle(deck);
		List<DeckCardDB> deckDB = new ArrayList<>();
		for (Card card : deck)
		{
			DeckCardDB deckCardDB = new DeckCardDB();
			deckCardDB.card = card.getID();
			deckCardDB.gameId = gameDB.id;
			deckCardDB.ordinal = deckDB.size();
			deckDB.add(deckCardDB);
		}

		gameDB.deckCards = deckDB;
	}

	@GetMapping("/games/{gameID}")
	public ResponseEntity<GameDB> game(@PathVariable int gameID)
	{
		return new ResponseEntity<>(findGameOrThrow(gameID), HttpStatus.OK);
	}

	@GetMapping("/games/{gameID}/actions")
	public Object getActions(@PathVariable int gameID, @RequestParam(defaultValue = "0") int from)
	{
		GameDB game = findGameOrThrow(gameID);

		//TODO: hide fold actions
		List<ActionDB> actions = game.actions;
		List<ActionDB> sublist = actions.subList(from, actions.size());
		if (sublist.isEmpty()) //TODO: longpoll boolean parameter?
			return actionDeferredResultService.getDeferredResult(gameID);

		return new ResponseEntity<>(sublist, HttpStatus.OK);
	}

	private Game loadGame(GameDB gameDB)
	{
		GameSessionDB gameSessionDB = gameSessionRepository.findById(gameDB.id).orElseThrow();
		List<Card> deck = gameDB.deckCards.stream().map(deckCardDB -> Card.fromId(deckCardDB.card)).collect(Collectors.toList());
		Game game = new Game(GameType.fromID(gameSessionDB.type), deck, 1); //TODO: point multiplier
		game.start();
		for (ActionDB a : gameDB.actions)
			game.processAction(PlayerSeat.fromInt(a.seat), new Action(a.action));

		return game;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	@PostMapping("/games/{gameID}/actions")
	public ResponseEntity<Void> postAction(@PathVariable int gameID, @RequestBody ActionPostDTO actionPostDTO)
	{
		if (actionPostDTO.action.length() >= 256)
			return new ResponseEntity<>(HttpStatus.PAYLOAD_TOO_LARGE);

		GameDB gameDB = findGameOrThrow(gameID);
		GameSessionDB gameSessionDB = gameSessionRepository.findById(gameDB.id).orElseThrow();

		int userId = 4; //TODO
		Optional<PlayerDB> player = gameSessionDB.players.stream().filter(p -> p.userId == userId).findFirst();
		if (player.isEmpty())
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		Game game = loadGame(gameDB);
		int playerCount = gameSessionDB.players.size();
		int seat = (player.get().ordinal - gameDB.beginnerPlayer + playerCount) % playerCount;

		boolean success;
		try
		{
			Action action = new Action(actionPostDTO.action);
			success = game.processAction(PlayerSeat.fromInt(seat), action);
		}
		catch (IllegalArgumentException e) //TODO: cleaner way?
		{
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		if (!success)
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);

		ActionDB actionDB = new ActionDB();
		actionDB.gameId = gameID;
		actionDB.ordinal = gameDB.actions.size();
		actionDB.seat = seat;
		actionDB.action = actionPostDTO.action;
		actionDB.time = System.currentTimeMillis();
		gameDB.actions.add(actionDB);

		if (game.isFinished())
		{
			for (PlayerSeat s : PlayerSeat.getAll())
			{
				PlayerDB p = gameSessionDB.players.get((gameDB.beginnerPlayer + s.asInt()) % playerCount);
				p.points += game.getPoints(s);
			}

			DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(DoubleRoundType.fromID(gameSessionDB.doubleRoundType));
			doubleRoundTracker.setData(gameSessionDB.doubleRoundData);
			if (game.isNormalFinish())
				doubleRoundTracker.gameFinished();
			else
				doubleRoundTracker.gameInterrupted();
			gameSessionDB.doubleRoundData = doubleRoundTracker.getData();

			startNewGame(gameSessionDB, (gameDB.beginnerPlayer + 1) % gameSessionDB.players.size());
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@GetMapping("/games/{gameID}/state")
	public ResponseEntity<GameStateDTO> getGameState(@PathVariable int gameID)
	{
		GameDB gameDB = findGameOrThrow(gameID);
		Game game = loadGame(gameDB);
		PlayerSeat me = null; //TODO

		GameStateDTO gameStateDTO = new GameStateDTO();
		gameStateDTO.phase = game.getCurrentPhaseEnum().getID();
		gameStateDTO.canThrowCards = me != null && game.canThrowCards(me);
		if (me != null && game.getTurn(me))
			gameStateDTO.availableActions = game.getAvailableActions().stream().map(Action::getId).collect(Collectors.toList());
		//TODO: caller tarock fold

		gameStateDTO.statistics.callerCardPoints = game.getCallerCardPoints();
		gameStateDTO.statistics.opponentCardPoints = game.getOpponentCardPoints();
		for (AnnouncementResult ar : game.getAnnouncementResults())
		{
			GameStateDTO.AnnouncementResult arDTO = new GameStateDTO.AnnouncementResult();
			arDTO.announcement = ar.getAnnouncementContra().getID();
			arDTO.points = ar.getPoints();
			arDTO.team = ar.getTeam() == Team.CALLER ? "caller" : "opponent";
			gameStateDTO.statistics.announcementResults.add(arDTO);
		}
		gameStateDTO.statistics.sumPoints = game.getSumPoints();
		gameStateDTO.statistics.pointMultiplier = game.getPointMultiplier();

		Collection<Card> callerSkart = game.getSkart(game.getBidWinnerPlayer());
		if (callerSkart == null)
			gameStateDTO.callerTarockFold = Collections.emptyList();
		else
			gameStateDTO.callerTarockFold = callerSkart.stream()
					.filter(c -> c instanceof TarockCard)
					.map(Card::getID)
					.collect(Collectors.toList());

		Trick currentTrick, previousTrick;
		if (game.getTrickCount() == 0)
		{
			previousTrick = null;
			currentTrick = new Trick(PlayerSeat.SEAT0);
		}
		else if (game.getTrickCount() == 1)
		{
			previousTrick = null;
			currentTrick = game.getTrick(0);
		}
		else
		{
			previousTrick = game.getTrick(game.getTrickCount() - 2);
			currentTrick = game.getTrick(game.getTrickCount() - 1);
		}

		gameStateDTO.currentTrick = new ArrayList<>();
		if (previousTrick == null)
		{
			gameStateDTO.previousTrick = null;
			gameStateDTO.previousTrickWinner = null;
		}
		else
		{
			gameStateDTO.previousTrick = new ArrayList<>();
			gameStateDTO.previousTrickWinner = previousTrick.getWinner().asInt();
		}

		for (PlayerSeat p : PlayerSeat.getAll())
		{
			if (p == me)
				gameStateDTO.cards.add(game.getPlayerCards(p).getCards().stream().map(Card::getID).collect(Collectors.toList()));
			else
				gameStateDTO.cards.add(null);

			gameStateDTO.turn.add(game.getTurn(p));

			String teamInfo = null;
			if (game.isTeamInfoGlobalOf(p) || (me != null && game.hasTeamInfo(me, p)))
				teamInfo = game.getPlayerPairs().getTeam(p) == Team.CALLER ? "caller" : "opponent";
			gameStateDTO.teamInfo.add(teamInfo);

			gameStateDTO.currentTrick.add(currentTrick.getCardByPlayer(p) == null ? null : currentTrick.getCardByPlayer(p).getID());

			if (previousTrick != null)
				gameStateDTO.previousTrick.add(previousTrick.getCardByPlayer(p) == null ? null : previousTrick.getCardByPlayer(p).getID());

			Collection<Card> skart = game.getSkart(p);
			if (skart == null)
				gameStateDTO.tarockFoldCount.add(0);
			else
				gameStateDTO.tarockFoldCount.add((int) skart.stream().filter(c -> c instanceof TarockCard).count());
		}

		return new ResponseEntity<>(gameStateDTO, HttpStatus.OK);
	}

	@GetMapping("/gameSessions/{gameSessionID}/chat")
	public Object chatGet(@PathVariable int gameSessionID, @RequestParam(defaultValue = "0") long from)
	{
		findGameSessionOrThrow(gameSessionID);

		List<ChatDB> chats = chatRepository.findTop100ByGameSessionIdAndTimeGreaterThanEqual(gameSessionID, from);
		if (chats.isEmpty())
			return chatDeferredResultService.getDeferredResult(gameSessionID);

		return new ResponseEntity<>(chats, HttpStatus.OK);
	}

	@PostMapping("/gameSessions/{gameSessionID}/chat")
	public ResponseEntity<Void> chatPost(@PathVariable int gameSessionID, @RequestBody ChatPostDTO chatPostDTO)
	{
		if (chatPostDTO.message.length() >= 256)
			return new ResponseEntity<>(HttpStatus.PAYLOAD_TOO_LARGE);

		findGameSessionOrThrow(gameSessionID);

		int userID = 4; //TODO

		ChatDB chatDB = new ChatDB();
		chatDB.gameSessionId = gameSessionID;
		chatDB.message = chatPostDTO.message;
		chatDB.time = System.currentTimeMillis();
		chatDB.userId = userID;

		chatDB = chatRepository.save(chatDB);
		chatDeferredResultService.notifyNewResult(gameSessionID, chatDB);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

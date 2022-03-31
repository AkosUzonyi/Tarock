package com.tisza.tarock.spring;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.spring.dto.*;
import com.tisza.tarock.spring.exception.*;
import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.repository.*;
import com.tisza.tarock.spring.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.*;

import java.net.*;
import java.util.*;
import java.util.stream.*;

@RestController
public class MainController
{
	private final UserRepository userRepository;
	private final IdpUserRepository idpUserRepository;
	private final GameSessionRepository gameSessionRepository;
	private final PlayerRepository playerRepository;
	private final GameRepository gameRepository;
	private final ChatRepository chatRepository;
	private final DeferredResultService<List<ChatDB>> chatDeferredResultService;
	private final DeferredResultService<List<ActionDB>> actionDeferredResultService;
	private final GameService gameService;
	private final GameSessionService gameSessionService;
	private final AuthService authService;

	public MainController(UserRepository userRepository, IdpUserRepository idpUserRepository, GameSessionRepository gameSessionRepository, PlayerRepository playerRepository, GameRepository gameRepository, ChatRepository chatRepository, DeferredResultService<List<ChatDB>> chatDeferredResultService, DeferredResultService<List<ActionDB>> actionDeferredResultService, GameService gameService, GameSessionService gameSessionService, AuthService authService)
	{
		this.userRepository = userRepository;
		this.idpUserRepository = idpUserRepository;
		this.gameSessionRepository = gameSessionRepository;
		this.playerRepository = playerRepository;
		this.gameRepository = gameRepository;
		this.chatRepository = chatRepository;
		this.chatDeferredResultService = chatDeferredResultService;
		this.actionDeferredResultService = actionDeferredResultService;
		this.gameService = gameService;
		this.gameSessionService = gameSessionService;
		this.authService = authService;
	}

	private int getLoggedInUserId()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
			return -1;

		return (Integer) authentication.getPrincipal();
	}

	private int requireLoggedInUserId()
	{
		int userId = getLoggedInUserId();
		if (userId < 0)
			throw new UnauthenticatedException();

		return userId;
	}

	@PostMapping("/auth/login")
	public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO)
	{
		LoginResponseDTO response;

		try
		{
			response = authService.auth(loginRequestDTO.provider, loginRequestDTO.token);
		}
		catch (IllegalArgumentException e)
		{
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		catch (Exception e)
		{
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<UserDB> user(@PathVariable int userId)
	{
		Optional<UserDB> user = userRepository.findById(userId);
		if (user.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(user.get(), HttpStatus.OK);
	}

	@GetMapping("/gameSessions")
	public ResponseEntity<List<GameSessionDB>> gameSessions()
	{
		return new ResponseEntity<>(gameSessionRepository.findActive(), HttpStatus.OK);
	}

	@GetMapping("/gameSessions/{gameSessionId}")
	public ResponseEntity<GameSessionDB> gameSession(@PathVariable int gameSessionId)
	{
		return new ResponseEntity<>(gameSessionService.findGameSession(gameSessionId), HttpStatus.OK);
	}

	@PostMapping("/gameSessions")
	public ResponseEntity<Void> createGameSession(@Validated @RequestBody CreateGameSessionDTO createGameSessionDTO, UriComponentsBuilder uriComponentsBuilder)
	{
		GameType type;
		DoubleRoundType doubleRoundType;
		try
		{
			type = GameType.fromID(createGameSessionDTO.type);
			doubleRoundType = DoubleRoundType.fromID(createGameSessionDTO.doubleRoundType);
		}
		catch (IllegalArgumentException e)
		{
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		int gameSessionId = gameSessionService.createGameSession(type, doubleRoundType, requireLoggedInUserId());

		URI uri = uriComponentsBuilder.path("/gameSessions/{gameSessionId}").buildAndExpand(gameSessionId).toUri();
		return ResponseEntity.created(uri).build();
	}

	@Transactional
	@DeleteMapping("/gameSessions/{gameSessionId}")
	public ResponseEntity<Void> deleteGameSession(@PathVariable int gameSessionId)
	{
		GameSessionDB gameSessionDB = gameSessionRepository.findById(gameSessionId).orElse(null);
		if (gameSessionDB == null || gameSessionDB.state.equals("deleted"))
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		if (gameSessionService.getPlayerFromUser(gameSessionDB, requireLoggedInUserId()) == null)
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		gameSessionService.deleteGameSession(gameSessionId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("/gameSessions/{gameSessionId}/join")
	public ResponseEntity<Void> joinGameSession(@PathVariable int gameSessionId)
	{
		GameSessionDB gameSessionDB = gameSessionService.findGameSession(gameSessionId);

		if (!gameSessionDB.state.equals("lobby"))
			return new ResponseEntity<>(HttpStatus.CONFLICT);

		gameSessionService.joinGameSession(gameSessionId, requireLoggedInUserId());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("/gameSessions/{gameSessionId}/leave")
	public ResponseEntity<Void> leaveGameSession(@PathVariable int gameSessionId)
	{
		GameSessionDB gameSessionDB = gameSessionService.findGameSession(gameSessionId);

		if (!gameSessionDB.state.equals("lobby"))
			return new ResponseEntity<>(HttpStatus.CONFLICT);

		gameSessionService.leaveGameSession(gameSessionId, requireLoggedInUserId());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("/gameSessions/{gameSessionId}/start")
	@Transactional
	public ResponseEntity<Void> startGameSession(@PathVariable int gameSessionId)
	{
		GameSessionDB gameSessionDB = gameSessionService.findGameSession(gameSessionId);

		if (!gameSessionDB.state.equals("lobby"))
			return new ResponseEntity<>(HttpStatus.CONFLICT);

		if (gameSessionService.getPlayerFromUser(gameSessionDB, requireLoggedInUserId()) == null)
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		gameSessionService.startGameSession(gameSessionId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@GetMapping("/games/{gameId}/actions")
	public Object getActions(@PathVariable int gameId, @RequestParam(defaultValue = "-1") int from)
	{
		if (from < 0)
			return new ResponseEntity<>(gameService.getActionsFiltered(gameId), HttpStatus.OK);

		return actionDeferredResultService.getDeferredResult(gameId, () -> {
			List<ActionDB> actions = gameService.getActionsFiltered(gameId);

			if (from >= actions.size())
				return Optional.empty();

			return Optional.of(actions.subList(from, actions.size()));
		});
	}

	@PostMapping("/games/{gameId}/actions")
	public ResponseEntity<Void> postAction(@PathVariable int gameId, @RequestBody ActionPostDTO actionPostDTO)
	{
		if (actionPostDTO.action.length() >= 256)
			return new ResponseEntity<>(HttpStatus.PAYLOAD_TOO_LARGE);

		GameDB gameDB = gameService.findGame(gameId);

		PlayerDB player = gameSessionService.getPlayerFromUser(gameDB.gameSession, requireLoggedInUserId());
		if (player == null)
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		Action action = new Action(actionPostDTO.action);
		PlayerSeat seat = gameService.getSeatFromPlayer(gameDB, player);
		if (seat == null)
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		boolean success;
		try
		{
			success = gameService.executeAction(gameId, seat, action);
		}
		catch (IllegalArgumentException e)
		{
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		if (!success)
			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@GetMapping("/games/{gameId}")
	public ResponseEntity<GameDTO> getGame(@PathVariable int gameId)
	{
		GameDB gameDB = gameService.findGame(gameId);
		Game game = gameService.loadGame(gameId);

		DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(DoubleRoundType.fromID(gameDB.gameSession.doubleRoundType));
		doubleRoundTracker.setData(gameDB.gameSession.doubleRoundData);
		int pointMultiplier = doubleRoundTracker.getCurrentMultiplier();

		GameDTO gameDTO = new GameDTO();

		gameDTO.id = gameDB.id;
		gameDTO.type = gameDB.gameSession.type;
		gameDTO.gameSessionId = gameDB.gameSession.id;
		gameDTO.createTime = gameDB.createTime;

		PlayerSeat me = null;
		int userId = getLoggedInUserId();
		if (userId >= 0)
		{
			PlayerDB playerDB = gameSessionService.getPlayerFromUser(gameDB.gameSession, userId);
			if (playerDB != null)
				me = gameService.getSeatFromPlayer(gameDB, playerDB);
		}

		gameDTO.phase = game.getCurrentPhaseEnum().getID();
		gameDTO.canThrowCards = me != null && game.canThrowCards(me);
		if (me != null && game.getTurn(me))
			gameDTO.availableActions = game.getAvailableActions().stream().map(Action::getId).collect(Collectors.toList());

		gameDTO.statistics.callerCardPoints = game.getCallerCardPoints();
		gameDTO.statistics.opponentCardPoints = game.getOpponentCardPoints();
		for (AnnouncementResult ar : game.getAnnouncementResults())
		{
			GameDTO.AnnouncementResult arDTO = new GameDTO.AnnouncementResult();
			arDTO.announcement = ar.getAnnouncementContra().getID();
			arDTO.points = ar.getPoints() * pointMultiplier;
			(ar.getTeam() == Team.CALLER ? gameDTO.statistics.callerAnnouncementResults : gameDTO.statistics.opponentAnnouncementResults).add(arDTO);
		}
		gameDTO.statistics.sumPoints = game.getSumPoints() * pointMultiplier;
		gameDTO.statistics.pointMultiplier = pointMultiplier;

		Trick currentTrick, previousTrick;
		if (game.getTrickCount() == 0)
		{
			previousTrick = null;
			currentTrick = null;
		}
		else if (game.getTrickCount() == 1)
		{
			previousTrick = null;
			currentTrick = game.getTrick(0);
		}
		else if (game.areAllTricksPassed())
		{
			previousTrick = game.getTrick(game.getTrickCount() - 1);
			currentTrick = null;
		}
		else
		{
			previousTrick = game.getTrick(game.getTrickCount() - 2);
			currentTrick = game.getTrick(game.getTrickCount() - 1);
		}

		if (previousTrick != null)
			gameDTO.previousTrickWinner = previousTrick.getWinner().asInt();

		for (PlayerSeat p : PlayerSeat.getAll())
		{
			GameDTO.PlayerInfo playerInfo = new GameDTO.PlayerInfo();
			gameDTO.players.add(playerInfo);

			playerInfo.user = gameService.getPlayerFromSeat(gameDB, p).user;

			if (p == me)
				playerInfo.cards = game.getPlayerCards(p).getCards().stream().map(Card::getID).collect(Collectors.toList());

			playerInfo.turn = game.getTurn(p);

			if (game.isTeamInfoGlobalOf(p) || (me != null && game.hasTeamInfo(me, p)))
				playerInfo.team = game.getPlayerPairs().getTeam(p) == Team.CALLER ? "caller" : "opponent";

			if (currentTrick != null)
				playerInfo.currentTrickCard = currentTrick.getCardByPlayer(p) == null ? null : currentTrick.getCardByPlayer(p).getID();

			if (previousTrick != null)
				playerInfo.previousTrickCard = previousTrick.getCardByPlayer(p) == null ? null : previousTrick.getCardByPlayer(p).getID();

			playerInfo.points = game.getPoints(p) * pointMultiplier;

			Collection<Card> skart = game.getSkart(p);
			if (game.getCurrentPhaseEnum().isAfter(PhaseEnum.FOLDING) && skart != null)
			{
				playerInfo.tarockFoldCount = (int) skart.stream().filter(c -> c instanceof TarockCard).count();

				if (p == game.getBidWinnerPlayer())
					playerInfo.visibleFoldedCards = skart.stream().filter(c -> c instanceof TarockCard).map(Card::getID).collect(Collectors.toList());
			}
		}

		return new ResponseEntity<>(gameDTO, HttpStatus.OK);
	}

	@GetMapping("/gameSessions/{gameSessionId}/chat")
	public Object chatGet(@PathVariable int gameSessionId, @RequestParam(defaultValue = "0") long from)
	{
		return chatDeferredResultService.getDeferredResult(gameSessionId, () -> {
			gameSessionService.findGameSession(gameSessionId);
			List<ChatDB> chats = chatRepository.findTop100ByGameSessionIdAndTimeGreaterThanEqual(gameSessionId, from);
			return chats.isEmpty() ? Optional.empty() : Optional.of(chats);
		});
	}

	@PostMapping("/gameSessions/{gameSessionId}/chat")
	public ResponseEntity<Void> chatPost(@PathVariable int gameSessionId, @RequestBody ChatPostDTO chatPostDTO)
	{
		if (chatPostDTO.message.length() >= 256)
			return new ResponseEntity<>(HttpStatus.PAYLOAD_TOO_LARGE);

		GameSessionDB gameSession = gameSessionService.findGameSession(gameSessionId);

		ChatDB chatDB = new ChatDB();
		chatDB.gameSession = gameSession;
		chatDB.message = chatPostDTO.message;
		chatDB.time = System.currentTimeMillis();
		chatDB.user = userRepository.findById(requireLoggedInUserId()).orElseThrow();

		chatDB = chatRepository.save(chatDB);
		chatDeferredResultService.notifyNewResult(gameSessionId);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

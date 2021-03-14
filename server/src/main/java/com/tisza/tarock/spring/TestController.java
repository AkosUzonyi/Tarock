package com.tisza.tarock.spring;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.spring.dto.*;
import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.model.UserDB;
import com.tisza.tarock.spring.repository.*;
import com.tisza.tarock.spring.service.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.*;

import java.net.*;
import java.util.*;

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
	private ChatDeferredResultService chatDeferredResultService;

	Logger logger = LoggerFactory.getLogger(TestController.class);

	@GetMapping("/idp")
	public ResponseEntity<IdpUserDB> idp()
	{
		Iterable<IdpUserDB> idpusers = idpUserRepository.findAll();
		return new ResponseEntity<>(idpusers.iterator().next(), HttpStatus.OK);
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
		Optional<GameSessionDB> gameSession = gameSessionRepository.findById(gameSessionID);
		if (gameSession.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(gameSession.get(), HttpStatus.OK);
	}

	@PostMapping("/gameSessions")
	public ResponseEntity<Void> createGameSession(@Validated @RequestBody CreateGameSessionDTO createGameSessionDTO, UriComponentsBuilder uriComponentsBuilder)
	{
		//TODO: request parameters not null

		GameSessionDB gameSession = new GameSessionDB();
		gameSession.type = GameType.fromID(createGameSessionDTO.type);
		gameSession.state = "lobby";
		gameSession.players = new ArrayList<>();
		gameSession.doubleRoundType = DoubleRoundType.fromID(createGameSessionDTO.doubleRoundType);
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
		Optional<GameSessionDB> gameSession = gameSessionRepository.findById(gameSessionID);
		if (gameSession.isEmpty())
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		int userID = 4; //TODO
		boolean containsUser = gameSession.get().players.stream().anyMatch(p -> p.userId == userID);
		if (!containsUser)
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		if (gameSession.get().state.equals("lobby"))
			gameSession.get().players.clear();

		gameSession.get().state = "deleted";

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@GetMapping("/games/{gameID}")
	public ResponseEntity<GameDB> game(@PathVariable int gameID)
	{
		Optional<GameDB> game = gameRepository.findById(gameID);
		if (game.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(game.get(), HttpStatus.OK);
	}

	@GetMapping("/games/{gameID}/actions")
	public ResponseEntity<List<ActionDB>> gameActions(@PathVariable int gameID, @RequestParam(defaultValue = "0") int fromIndex)
	{
		Optional<GameDB> game = gameRepository.findById(gameID);
		if (game.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		List<ActionDB> actions = game.get().actions;
		List<ActionDB> sublist = actions.subList(fromIndex, actions.size());
		if (sublist.isEmpty());
			//TODO

		return new ResponseEntity<>(sublist, HttpStatus.OK);
	}

	@GetMapping("/gameSessions/{gameSessionID}/chat")
	public Object chatGet(@PathVariable int gameSessionID, @RequestParam(defaultValue = "0") long from)
	{
		Optional<GameSessionDB> gameSession = gameSessionRepository.findById(gameSessionID);
		if (gameSession.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		List<ChatDB> chats = chatRepository.findTop100ByTimeGreaterThanEqual(from);
		if (chats.isEmpty())
			return chatDeferredResultService.getDeferredResult(gameSessionID);

		return new ResponseEntity<>(chats, HttpStatus.OK);
	}

	@PostMapping("/gameSessions/{gameSessionID}/chat")
	public ResponseEntity<Void> chatPost(@PathVariable int gameSessionID, @RequestBody ChatRequestDTO chatRequestDTO)
	{
		if (chatRequestDTO.message.length() >= 256)
			return new ResponseEntity<>(HttpStatus.PAYLOAD_TOO_LARGE);

		Optional<GameSessionDB> gameSession = gameSessionRepository.findById(gameSessionID);
		if (gameSession.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		int userID = 4; //TODO

		ChatDB chatDB = new ChatDB();
		chatDB.gameSessionId = gameSessionID;
		chatDB.message = chatRequestDTO.message;
		chatDB.time = System.currentTimeMillis();
		chatDB.userId = userID;

		chatDB = chatRepository.save(chatDB);
		chatDeferredResultService.newChat(gameSessionID, chatDB);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

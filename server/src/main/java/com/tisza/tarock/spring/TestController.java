package com.tisza.tarock.spring;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.server.*;
import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.model.User;
import com.tisza.tarock.spring.repository.*;
import javassist.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.*;

import java.util.*;
import java.util.concurrent.atomic.*;

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

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();
	Logger logger = LoggerFactory.getLogger(TestController.class);
	private DeferredResult<String> d;

	@GetMapping("/greeting")
	@ResponseBody
	public List<Card> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return Server.instance.getGameSessionManager().getGameSessions().iterator().next().getCurrentGame().getTalon();
		/*String str = "";
		for (GameSession gs : )
			str += gs.getID() + " ";
		return "Hello " + str;*/

	}

	@GetMapping("/idp")
	public ResponseEntity<IdpUser> idp()
	{
		Iterable<IdpUser> idpusers = idpUserRepository.findAll();
		return new ResponseEntity<>(idpusers.iterator().next(), HttpStatus.OK);
	}

	@GetMapping("/users/{userID}")
	public ResponseEntity<User> user(@PathVariable int userID)
	{
		Optional<User> user = userRepository.findById(userID);
		if (user.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(user.get(), HttpStatus.OK);
	}

	@GetMapping("/gameSessions")
	public ResponseEntity<List<GameSessionDB>> gameSessions()
	{
		List<GameSessionDB> gameSessions = new ArrayList<>();
		gameSessionRepository.findAll().forEach(gameSessions::add);
		return new ResponseEntity<>(gameSessions, HttpStatus.OK);
	}

	@GetMapping("/longpoll")
	public DeferredResult<String> longpoll() {
		if (d == null)
			d = new DeferredResult<String>(5000L, "alma");

		return d;
	}

	@GetMapping("/longpush")
	public void longpush(@RequestParam(value = "name", defaultValue = "World") String name) {
		logger.info(name);
		if (d != null) {
			d.setResult(name);
			d = null;
		}
	}
}

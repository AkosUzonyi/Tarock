package com.tisza.tarock.gui.viewmodel;

import android.app.*;
import android.text.*;
import android.util.*;
import androidx.lifecycle.*;
import com.tisza.tarock.R;
import com.tisza.tarock.api.*;
import com.tisza.tarock.api.model.*;
import com.tisza.tarock.game.*;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.*;
import io.reactivex.disposables.*;
import io.reactivex.plugins.*;
import retrofit2.*;

import java.util.*;
import java.util.concurrent.*;

public class GameViewModel extends AndroidViewModel
{
	public static final String TAG = GameViewModel.class.getSimpleName();

	private final int gameSessionId;

	private final APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);;

	private User loggedInUser;

	private final MutableLiveData<GameSession> gameSession = new MutableLiveData<>();
	private GameStateDTO gameStateDTO;
	private final MutableLiveData<Integer> mySeat = new MutableLiveData<>();

	private final MutableLiveData<List<String>> shortUserNames = new MutableLiveData<>();
	private final MutableLiveData<GameState> gameState = new MutableLiveData<>();
	private final MutableLiveData<PhaseEnum> phase = new MutableLiveData<>();
	private final MutableLiveData<Spanned> messagesText = new MutableLiveData<>();
	private final List<MutableLiveData<String>> actionBubbleContents = new ArrayList<>();
	private final List<MutableLiveData<String>> chatBubbleContents = new ArrayList<>();

	private List<ActionDTO> actions = new ArrayList<>();
	private int nextActionOrdinal = 0;
	private List<Chat> chats = new ArrayList<>();
	private long lastChatTime = 0;

	private int prevBid;

	private Disposable chatDisposable = null;
	private Disposable actionDisposable = null;
	private final Disposable gameSessionDisposable;


	public GameViewModel(Application application, int gameSessionId)
	{
		super(application);
		this.gameSessionId = gameSessionId;

		for (int i = 0; i < 4; i++)
		{
			actionBubbleContents.add(new MutableLiveData<>());
			chatBubbleContents.add(new MutableLiveData<>());
		}

		gameSessionDisposable = Observable.interval(0, 2, TimeUnit.SECONDS)
				.flatMap(i -> apiInterface.getGameSession(gameSessionId))
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::onGameSessionUpdate);
	}

	//TODO hack
	public void setLoggedInUser(User loggedInUser)
	{
		this.loggedInUser = loggedInUser;
		updateSeat();
		updateGameStateValues();
	}

	public LiveData<GameSession> getGameSession()
	{
		return gameSession;
	}

	public MutableLiveData<GameState> getGameState()
	{
		return gameState;
	}

	public LiveData<Integer> getMySeat()
	{
		return mySeat;
	}

	public LiveData<List<String>> getShortUserNames()
	{
		return shortUserNames;
	}

	public LiveData<PhaseEnum> getPhase()
	{
		return phase;
	}

	public MutableLiveData<Spanned> getMessagesText()
	{
		return messagesText;
	}

	public LiveData<String> getActionBubbleContent(int seat)
	{
		return actionBubbleContents.get(seat);
	}

	public LiveData<String> getChatBubbleContent(int seat)
	{
		return chatBubbleContents.get(seat);
	}

	public void startGameSession()
	{
		apiInterface.startGameSession(gameSessionId).subscribe();
	}

	public void doAction(Action action)
	{
		if (gameStateDTO == null)
			Log.w(TAG, "doAction: no game is in progress");

		apiInterface.postAction(gameStateDTO.id, new ActionPostDTO(action.getId())).subscribe();
	}

	public void sendChat(String text)
	{
		apiInterface.postChat(gameSessionId, new ChatPostDTO(text)).subscribe();
	}

	private int getSeatOfUser(int userID)
	{
		if (gameStateDTO == null)
			return -1;

		for (int i = 0; i < gameStateDTO.players.size(); i++)
			if (gameStateDTO.players.get(i).user.id == userID)
				return i;

		return -1;
	}

	private String getPlayerName(int seat)
	{
		User user = gameStateDTO.players.get(seat).user;
		for (int i = 0; i < gameSession.getValue().players.size(); i++)
			if (gameSession.getValue().players.get(i).user.id == user.id)
				return shortUserNames.getValue().get(i);

		return getApplication().getString(R.string.empty_seat);
	}

	public static String getFirstName(String name)
	{
		return name.substring(name.lastIndexOf(' ') + 1);
	}

	private void updateShortNames()
	{
		Set<String> firstNames = new HashSet<>();
		Set<String> duplicateFirstNames = new HashSet<>();
		for (Player player : gameSession.getValue().players)
		{
			String firstName = getFirstName(player.user.getName());
			if (!firstNames.add(firstName))
				duplicateFirstNames.add(firstName);
		}
		List<String> shortUserNamesLocal = new ArrayList<>();
		for (Player player : gameSession.getValue().players)
		{
			String firstName = getFirstName(player.user.getName());
			if (!duplicateFirstNames.contains(firstName))
				shortUserNamesLocal.add(firstName);
			else
				shortUserNamesLocal.add(player.user.getName());
		}
		shortUserNames.setValue(shortUserNamesLocal);
	}

	private void updateSeat()
	{
		if (gameStateDTO == null)
			return;

		mySeat.setValue(loggedInUser == null ? null : getSeatOfUser(loggedInUser.id));
		updateGameStateValues();
	}

	//TODO do not update so frequent
	private void onGameSessionUpdate(GameSession gameSession)
	{
		this.gameSession.setValue(gameSession);

		if (gameSession == null)
			return;

		if (GameSessionState.fromId(gameSession.state) == GameSessionState.LOBBY)
			apiInterface.joinGameSession(gameSessionId).subscribe();

		updateShortNames();
		if (lastChatTime == 0)
			lastChatTime = gameSession.createTime;

		updateGameState();
	}

	private int getPhaseStringRes(PhaseEnum phase)
	{
		int phaseStringRes = 0;
		switch (phase)
		{
			case BIDDING: phaseStringRes = R.string.message_bidding; break;
			case FOLDING: phaseStringRes = R.string.message_folding; break;
			case CALLING: phaseStringRes = R.string.message_calling; break;
			case ANNOUNCING: phaseStringRes = R.string.message_announcing; break;
			case INTERRUPTED: phaseStringRes = R.string.message_press_ok; break;
		}
		return phaseStringRes;
	}

	private void updateMessagesText()
	{
		StringBuilder messagesHtml = new StringBuilder();
		messagesHtml.append(getApplication().getString(R.string.message_phase, getApplication().getString(R.string.message_bidding)));

		int actionIndex = 0;
		int chatIndex = 0;
		while (true)
		{
			ActionDTO actionDTO = actionIndex >= actions.size() ? null : actions.get(actionIndex);
			Chat chat = chatIndex >= chats.size() ? null : chats.get(chatIndex);
			if (actionDTO == null && chat == null)
				break;

			boolean selectAction = chat == null || actionDTO != null && actionDTO.time < chat.time;

			int phaseStringRes = 0;
			int stringTemplateRes;
			String userName;
			String message;
			if (selectAction)
			{
				stringTemplateRes = R.string.message_action;
				userName = getPlayerName(actionDTO.seat);
				Action action = new Action(actionDTO.action);
				message = action.translate(getApplication().getResources());
				actionIndex++;

				if (action.getId().equals("announce:passz"))
					continue;

				PhaseEnum nextActionPhase;
				if (actionIndex < actions.size())
					nextActionPhase = new Action(actions.get(actionIndex).action).getPhase();
				else
					nextActionPhase = PhaseEnum.fromID(gameStateDTO.phase);

				if (action.getPhase() != nextActionPhase)
				{
					phaseStringRes = getPhaseStringRes(nextActionPhase);
					if (action.getPhase() == PhaseEnum.FOLDING)
					{
						for (int seat = 0; seat < 4; seat++)
						{
							int count = gameStateDTO.players.get(seat).tarockFoldCount;
							if (count > 0)
							{
								String tarockFoldMessage = getApplication().getResources().getQuantityString(R.plurals.message_fold_tarock, count, count);
								messagesHtml.append(getApplication().getString(stringTemplateRes, getPlayerName(seat), tarockFoldMessage));
								messagesHtml.append("<br>");
							}
						}
					}
				}
			}
			else
			{
				stringTemplateRes = R.string.message_chat;

				int seat = getSeatOfUser(chat.user.id);
				if (seat < 0)
					userName = chat.user.name;
				else
					userName = getPlayerName(seat);

				message = chat.message;
				chatIndex++;
			}

			if (message != null)
			{
				messagesHtml.append(getApplication().getString(stringTemplateRes, userName, message));
				messagesHtml.append("<br>");
			}

			if (phaseStringRes > 0)
			{
				messagesHtml.append("<br>");
				messagesHtml.append(getApplication().getString(R.string.message_phase, getApplication().getString(phaseStringRes)));
			}
		}

		messagesText.setValue(Html.fromHtml(messagesHtml.toString()));
	}

	private void pollChats(boolean init)
	{
		if (chatDisposable != null)
			chatDisposable.dispose();

		chatDisposable = apiInterface.getChat(gameSessionId, lastChatTime).observeOn(AndroidSchedulers.mainThread()).subscribe(newChats ->
		{
			chats.addAll(newChats);
			if (!init)
			{
				for (Chat chat : newChats)
				{
					int seat = getSeatOfUser(chat.user.id);
					if (seat >= 0)
						chatBubbleContents.get(getDirectionOfSeat(seat)).setValue(chat.message);
				}
			}

			if (!chats.isEmpty())
				lastChatTime = chats.get(chats.size() - 1).time + 1;
			updateMessagesText();
			pollChats(false);
		});
	}

	private void pollActions(boolean init)
	{
		if (actionDisposable != null)
			actionDisposable.dispose();

		if (gameSession.getValue() == null || gameSession.getValue().currentGameId == null)
			return;

		actionDisposable = apiInterface.getActions(gameSession.getValue().currentGameId, nextActionOrdinal).observeOn(AndroidSchedulers.mainThread()).subscribe(newActions ->
		{
			actions.addAll(newActions);
			if (!init)
			{
				for (ActionDTO actionDTO : newActions)
				{
					String message = new Action(actionDTO.action).translate(getApplication().getResources());
					if (message != null)
						actionBubbleContents.get(getDirectionOfSeat(actionDTO.seat)).setValue(message);
				}
			}

			if (!actions.isEmpty())
				nextActionOrdinal = actions.get(actions.size() - 1).ordinal + 1;
			updateMessagesText();
			updateGameState();
			pollActions(false);
		});
	}

	private void updateGameStateValues()
	{
		if (gameStateDTO == null)
		{
			gameState.setValue(null);
			return;
		}

		GameState gameStateLocal = new GameState();

		GameStateDTO.PlayerInfo[] playersRotated = new GameStateDTO.PlayerInfo[4];
		for (int seat = 0; seat < 4; seat++)
			playersRotated[getDirectionOfSeat(seat)] = gameStateDTO.players.get(seat);

		gameStateLocal.type = GameType.fromID(gameStateDTO.type);
		gameStateLocal.phase = PhaseEnum.fromID(gameStateDTO.phase);
		gameStateLocal.canThrowCards = gameStateDTO.canThrowCards;
		gameStateLocal.availableActions = gameStateDTO.availableActions;
		gameStateLocal.previousTrickWinnerDirection = gameStateDTO.previousTrickWinner == null ? null : getDirectionOfSeat(gameStateDTO.previousTrickWinner);
		gameStateLocal.playersRotated = Arrays.asList(playersRotated);
		gameStateLocal.beginnerDirection = getDirectionOfSeat(0);
		gameStateLocal.statistics = gameStateDTO.statistics;

		gameState.setValue(gameStateLocal);
	}

	private void updateGameState()
	{
		if (gameSession.getValue() == null || gameSession.getValue().currentGameId == null)
			return;

		apiInterface.getGameState(gameSession.getValue().currentGameId).observeOn(AndroidSchedulers.mainThread()).subscribe(newGameState ->
		{
			boolean phaseChange = gameStateDTO == null || !newGameState.phase.equals(gameStateDTO.phase);
			boolean isNewGame = gameStateDTO == null || gameStateDTO.id != newGameState.id;

			gameStateDTO = newGameState;
			updateSeat();
			updateGameStateValues();

			if (isNewGame)
			{
				chats.clear();
				lastChatTime = newGameState.createTime;
				pollChats(true);

				actions.clear();
				nextActionOrdinal = 0;
				pollActions(true);
			}

			if (phaseChange)
			{
				PhaseEnum newPhase = PhaseEnum.fromID(newGameState.phase);
				phase.setValue(newPhase);

				if (newPhase == PhaseEnum.FOLDING)
				{
					for (int seat = 0; seat < 4; seat++)
					{
						int count = newGameState.players.get(seat).tarockFoldCount;
						if (count > 0)
						{
							String tarockFoldMessage = getApplication().getResources().getQuantityString(R.plurals.message_fold_tarock, count, count);
							actionBubbleContents.get(getDirectionOfSeat(seat)).setValue(tarockFoldMessage);
						}
					}
				}
			}

			updateMessagesText();
		},
		throwable ->
		{
			if (throwable instanceof HttpException)
			{
				HttpException httpException = (HttpException) throwable;
				if (httpException.code() == 410)
					return; //TODO update game session?
			}

			RxJavaPlugins.onError(throwable);
		});
	}

	private int getDirectionOfSeat(int otherSeat)
	{
		int viewSeat = mySeat.getValue() == null ? 0 : mySeat.getValue();
		return (otherSeat - viewSeat + 4) % 4;
	}

	@Override
	public void onCleared()
	{
		super.onCleared();

		if (gameSession.getValue() != null && GameSessionState.fromId(gameSession.getValue().state) == GameSessionState.LOBBY)
			apiInterface.leaveGameSession(gameSessionId).subscribe();

		if (actionDisposable != null)
			actionDisposable.dispose();
		if (chatDisposable != null)
			chatDisposable.dispose();
		if (gameSessionDisposable != null)
			gameSessionDisposable.dispose();
	}

	public static class Factory implements ViewModelProvider.Factory
	{
		private final Application application;
		private final int gameSessionId;

		public Factory(Application application, int gameSessionId)
		{
			this.application = application;
			this.gameSessionId = gameSessionId;
		}

		@Override
		public <T extends ViewModel> T create(Class<T> modelClass)
		{
			return (T) new GameViewModel(application, gameSessionId);
		}
	}
}

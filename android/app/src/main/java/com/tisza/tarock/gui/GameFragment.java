package com.tisza.tarock.gui;

import android.animation.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.view.animation.Animation.*;
import android.view.inputmethod.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.lifecycle.*;
import androidx.preference.*;
import androidx.recyclerview.widget.*;
import androidx.viewpager.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.R;
import com.tisza.tarock.api.model.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.message.Action;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.zebisound.*;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.*;
import io.reactivex.disposables.*;
import io.reactivex.plugins.*;
import retrofit2.*;

import java.util.*;
import java.util.concurrent.*;

public class GameFragment extends MainActivityFragment implements TextView.OnEditorActionListener
{
	public static final String TAG = "game";
	public static final String KEY_GAME_SESSION_ID = "game_id";
	public static final String LOG_TAG = "Tarokk";

	public static final float PLAYED_CARD_DISTANCE = 1F;
	public static final int PLAY_DURATION = 50;
	public static final int TAKE_DURATION = 400;
	public static final int DELAY = 2500;
	public static final int CARDS_PER_ROW = 6;

	private static final int MESSAGES_VIEW_INDEX = 0;
	private static final int GAMEPLAY_VIEW_INDEX = 1;
	private static final int STATISTICS_VIEW_INDEX = 2;

	public int cardWidth;

	private ZebiSounds zebiSounds;

	private LayoutInflater layoutInflater;
	private Vibrator vibrator;

	private TextView[] playerNameViews;
	private TextView[] playerMessageViews;
	private View[] skartViews;
	private View myCardsView;
	private LinearLayout myCardsView0;
	private LinearLayout myCardsView1;
	private View cardsBackgroundColorView;
	private View cardsHighlightView;
	private ViewPager centerSpace;
	private Button okButton;
	private Button throwButton;
	private Button lobbyStartButton;
	private RecyclerView userListRecyclerView;
	private UsersAdapter usersAdapter;

	private View messagesFrame;

	private View messagesView;
	private ScrollView messagesScrollView;
	private TextView messagesTextView;
	private ListView availableActionsListView;
	private ArrayAdapter<ActionButtonItem> availableActionsAdapter;
	private EditText messagesChatEditText;

	private View ultimoView;
	private Button ultimoBackButton;
	private UltimoViewManager ultimoViewManager;
	private Button announceButton;

	private RelativeLayout gameplayView;
	private PlayedCardView[] playedCardViews;

	private View statisticsView;
	private TextView statisticsGamepointsCaller;
	private TextView statisticsGamepointsOpponent;
	private TextView statisticsPointMultiplierView;
	private LinearLayout statisticsCallerEntriesView;
	private LinearLayout statisticsOpponentEntriesView;
	private TextView statisticsSumPointsView;
	private RecyclerView statisticsPointsView;
	private StatisticsPointsAdapter statisticsPointsAdapter;
	private EditText statisticsChatEditText;

	private ConnectionViewModel connectionViewModel;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		zebiSounds = new ZebiSounds(getActivity());
		connectionViewModel = ViewModelProviders.of(getActivity()).get(ConnectionViewModel.class);
		vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View contentView = inflater.inflate(R.layout.game, container, false);

		cardWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth() / CARDS_PER_ROW;

		centerSpace = contentView.findViewById(R.id.center_space);

		playerNameViews = new TextView[]
		{
				(TextView)contentView.findViewById(R.id.player_name_0),
				(TextView)contentView.findViewById(R.id.player_name_1),
				(TextView)contentView.findViewById(R.id.player_name_2),
				(TextView)contentView.findViewById(R.id.player_name_3),
		};

		playerMessageViews = new TextView[]
		{
				(TextView)contentView.findViewById(R.id.player_message_0),
				(TextView)contentView.findViewById(R.id.player_message_1),
				(TextView)contentView.findViewById(R.id.player_message_2),
				(TextView)contentView.findViewById(R.id.player_message_3),
		};

		skartViews = new View[]
		{
				contentView.findViewById(R.id.skart_0),
				contentView.findViewById(R.id.skart_1),
				contentView.findViewById(R.id.skart_2),
				contentView.findViewById(R.id.skart_3),
		};

		myCardsView = contentView.findViewById(R.id.cards);
		myCardsView0 = (LinearLayout)contentView.findViewById(R.id.my_cards_0);
		myCardsView1 = (LinearLayout)contentView.findViewById(R.id.my_cards_1);
		cardsBackgroundColorView = contentView.findViewById(R.id.cards_background_color);
		cardsHighlightView = contentView.findViewById(R.id.cards_highlight);

		okButton = (Button)contentView.findViewById(R.id.ok_button);
		okButton.setOnClickListener(v ->
		{
			switch (PhaseEnum.fromID(gameStateDTO.phase))
			{
				case FOLDING:
					doAction(Action.fold(cardsToFold));
					break;
				case ANNOUNCING:
					if (ultimoView.getVisibility() == View.GONE)
						doAction(Action.announcePassz());
					break;
				case END:
				case INTERRUPTED:
					doAction(Action.readyForNewGame());
					break;
			}
		});
		throwButton = (Button)contentView.findViewById(R.id.throw_button);
		throwButton.setOnClickListener(v -> doAction(Action.throwCards()));

		availableActionsAdapter = new ArrayAdapter<ActionButtonItem>(getActivity(), R.layout.button)
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				View view = super.getView(position, convertView, parent);
				view.setOnClickListener(new DoubleClickListener(getContext(), v ->
				{
					ActionButtonItem actionButton = getItem(position);
					actionButton.onClicked();
				}));
				return view;
			}
		};

		messagesFrame = layoutInflater.inflate(R.layout.messages, centerSpace, false);

		messagesView = messagesFrame.findViewById(R.id.messages_view);
		messagesScrollView = (ScrollView)messagesFrame.findViewById(R.id.messages_scroll);
		messagesTextView = (TextView)messagesFrame.findViewById(R.id.messages_text_view);
		messagesTextView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> messagesScrollView.smoothScrollTo(0, bottom));
		availableActionsListView = messagesFrame.findViewById(R.id.available_actions_list);
		availableActionsListView.setAdapter(availableActionsAdapter);
		messagesChatEditText = messagesFrame.findViewById(R.id.messages_chat_edit_text);
		messagesChatEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		messagesChatEditText.setOnEditorActionListener(this);
		userListRecyclerView = messagesFrame.findViewById(R.id.game_user_list_recycler_view);
		userListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		userListRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
		usersAdapter = new UsersAdapter(getContext());
		userListRecyclerView.setAdapter(usersAdapter);
		lobbyStartButton = messagesFrame.findViewById(R.id.lobby_start_button);
		lobbyStartButton.setOnClickListener(v ->
		{
			MainProto.Message startMessage = MainProto.Message.newBuilder().setStartGameSessionLobby(MainProto.StartGameSessionLobby.getDefaultInstance()).build();

			if (gameSession.getPlayers().size() >= 4)
				connectionViewModel.getApiInterface().startGameSession(gameSessionId).subscribe();
			else
				new AlertDialog.Builder(getContext())
						.setTitle(Html.fromHtml(getString(R.string.lobby_start_with_bots_confirm_title)))
						.setMessage(Html.fromHtml(getString(R.string.lobby_start_with_bots_confirm_body)))
						.setPositiveButton(R.string.lobby_start_with_bots_confirm_yes, (dialog, which) -> connectionViewModel.getApiInterface().startGameSession(gameSessionId).subscribe())
						.setNegativeButton(R.string.cancel, null)
						.show();

		});

		ultimoView = messagesFrame.findViewById(R.id.ultimo_view);
		ultimoBackButton = (Button)messagesFrame.findViewById(R.id.ultimo_back_buton);
		announceButton = (Button)messagesFrame.findViewById(R.id.ultimo_announce_button);
		ultimoViewManager = new UltimoViewManager(getActivity(), layoutInflater, (LinearLayout)messagesFrame.findViewById(R.id.ultimo_spinner_list));
		ultimoBackButton.setOnClickListener(v -> setUltimoViewVisible(false));

		announceButton.setOnClickListener(new DoubleClickListener(getContext(), v ->
		{
			Announcement announcement = ultimoViewManager.getCurrentSelectedAnnouncement();

			if (announcement == null)
				throw new RuntimeException();

			doAction(Action.announce(announcement));
			setUltimoViewVisible(false);
		}));

		gameplayView = (RelativeLayout)layoutInflater.inflate(R.layout.gameplay, centerSpace, false);
		playedCardViews = new PlayedCardView[4];
		for (int i = 0; i < 4; i++)
		{
			playedCardViews[i] = new PlayedCardView(getActivity(), cardWidth, i);
			playedCardViews[i].setOnClickListener(v ->
			{
				if (((PlayedCardView)v).isTaken())
					for (PlayedCardView playedCardView : playedCardViews)
						playedCardView.showTaken();
			});
			gameplayView.addView(playedCardViews[i]);
		}


		statisticsView = layoutInflater.inflate(R.layout.statistics, centerSpace, false);
		statisticsGamepointsCaller = (TextView)statisticsView.findViewById(R.id.statistics_gamepoints_caller);
		statisticsGamepointsOpponent = (TextView)statisticsView.findViewById(R.id.statistics_gamepoints_opponent);
		statisticsPointMultiplierView = (TextView)statisticsView.findViewById(R.id.statistics_point_multiplier);
		statisticsCallerEntriesView = (LinearLayout)statisticsView.findViewById(R.id.statistics_caller_entries_list);
		statisticsOpponentEntriesView = (LinearLayout)statisticsView.findViewById(R.id.statistics_opponent_entries_list);
		statisticsSumPointsView = (TextView)statisticsView.findViewById(R.id.statistics_sum_points);
		statisticsPointsView = statisticsView.findViewById(R.id.statistics_points);
		statisticsPointsView.setLayoutManager(new GridLayoutManager(getContext(), 4));
		statisticsPointsAdapter = new StatisticsPointsAdapter();
		statisticsPointsView.setAdapter(statisticsPointsAdapter);
		statisticsChatEditText = statisticsView.findViewById(R.id.statistics_chat_edit_text);
		statisticsChatEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		statisticsChatEditText.setOnEditorActionListener(this);

		View[] centerViews = {messagesFrame, gameplayView, statisticsView};
		int[] centerViewTitles = {R.string.pager_announcing, R.string.pager_gameplay, R.string.pager_statistics};
		centerSpace.setAdapter(new CenterViewPagerAdapter(getActivity(), centerViews, centerViewTitles));

		if (!getArguments().containsKey(KEY_GAME_SESSION_ID))
			throw new IllegalArgumentException("no game id given");

		gameSessionId = getArguments().getInt(KEY_GAME_SESSION_ID);

		gameSessionDisposable = Observable.interval(0, 2, TimeUnit.SECONDS)
				.flatMap(i -> connectionViewModel.getApiInterface().getGameSession(gameSessionId))
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::onGameSessionUpdate);

		connectionViewModel.getLoggedInUser().observe(this, user ->
		{
			loggedInUser = user;
			updateSeat();
			updateGameState();
		});

		return contentView;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if (gameSession != null && GameSessionState.fromId(gameSession.state) == GameSessionState.LOBBY)
			connectionViewModel.getApiInterface().leaveGameSession(gameSessionId).subscribe();

		if (actionDisposable != null)
			actionDisposable.dispose();
		if (chatDisposable != null)
			chatDisposable.dispose();
		if (gameSessionDisposable != null)
			gameSessionDisposable.dispose();
	}

	private void resetGameViews()
	{
		showCenterView(MESSAGES_VIEW_INDEX);
		okButton.setVisibility(View.GONE);
		throwButton.setVisibility(View.GONE);
		messagesTextView.setText("");
		availableActionsAdapter.clear();
		setUltimoViewVisible(false);

		cardsHighlightView.setVisibility(View.GONE);
		cardsBackgroundColorView.setBackgroundDrawable(null);

		statisticsGamepointsCaller.setText(null);
		statisticsGamepointsOpponent.setText(null);
		statisticsPointMultiplierView.setVisibility(View.GONE);
		statisticsCallerEntriesView.removeAllViews();
		statisticsOpponentEntriesView.removeAllViews();
		statisticsSumPointsView.setText(null);

		cardClickListener = null;

		for (PlayedCardView playedCardView : playedCardViews)
			playedCardView.reset();

		for (TextView nameView : playerNameViews)
			nameView.setTextColor(getResources().getColor(R.color.unknown_team));

		for (View skartView : skartViews)
			skartView.setVisibility(View.GONE);
	}

	private int getSeatOfUser(int userID)
	{
		if (gameDTO == null)
			return -1;

		for (int i = 0; i < gameDTO.players.size(); i++)
			if (gameDTO.players.get(i).user.id == userID)
				return i;

		return -1;
	}

	public static String getFirstName(String name)
	{
		return name.substring(name.lastIndexOf(' ') + 1);
	}

	private String getPlayerName(int seat)
	{
		if (seat >= shortUserNames.size())
			return getString(R.string.empty_seat);

		return shortUserNames.get(seat);
	}

	private void updateSeat()
	{
		if (gameDTO == null)
			return;

		mySeat = loggedInUser == null ? -1 : getSeatOfUser(loggedInUser.id);
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

	private void updatePlayerNameViews()
	{
		for (int i = 0; i < 4; i++)
		{
			TextView playerNameView = playerNameViews[getPositionFromSeat(i)];
			playerNameView.setText(R.string.empty_seat);
			User user = gameDTO.players.get(i).user;
			if (user != null)
			{
				playerNameView.setText(user.getName());
				playerNameView.setAlpha(user.isOnline() ? 1F : 0.5F);
			}
		}
	}

	private int gameSessionId;
	private GameSession gameSession;

	private GameDTO gameDTO;
	private GameStateDTO gameStateDTO;
	private List<String> shortUserNames;

	private User loggedInUser;
	private int mySeat = -1;

	private List<ActionDTO> actions = new ArrayList<>();
	private int nextActionOrdinal = 0;
	private List<Chat> chats = new ArrayList<>();
	private long lastChatTime = 0;

	private Map<Card, View> cardToViewMapping = new HashMap<>();
	private OnClickListener cardClickListener;

	private List<Card> cardsToFold = new ArrayList<>();
	private int prevBid;

	private Disposable chatDisposable = null;
	private Disposable actionDisposable = null;
	private Disposable gameSessionDisposable = null;

	private void doAction(Action action)
	{
		if (gameDTO == null)
			Log.w(TAG,"doAction: no game is in progress");

		connectionViewModel.getApiInterface().postAction(gameDTO.id, new ActionPostDTO(action.getId())).subscribe(responseBody -> {}, throwable ->
		{
			if (throwable instanceof HttpException)
			{
				HttpException httpException = (HttpException) throwable;
				switch (httpException.code())
				{
					case 422:
						vibrator.vibrate(100);
						return;
				}
			}
			RxJavaPlugins.onError(throwable);
		});
	}

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
	{
		if ((view == messagesChatEditText || view == statisticsChatEditText) && actionId == EditorInfo.IME_ACTION_SEND)
		{
			Editable text = ((EditText)view).getText();
			if (text.length() == 0)
				return false;
			connectionViewModel.getApiInterface().postChat(gameSessionId, new ChatPostDTO(text.toString())).subscribe();
			text.clear();
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			return true;
		}

		return false;
	}

	//TODO do not update so frequent
	private void onGameSessionUpdate(GameSession gameSession)
	{
		if (gameSession == null)
		{
			getActivity().getSupportFragmentManager().popBackStack();
			return;
		}

		boolean updateGame = this.gameSession == null || this.gameSession.currentGameId != gameSession.currentGameId;

		this.gameSession = gameSession;

		if (GameSessionState.fromId(gameSession.state) == GameSessionState.LOBBY)
			connectionViewModel.getApiInterface().joinGameSession(gameSessionId).subscribe();

		updateSeat();
		if (lastChatTime == 0)
			lastChatTime = gameSession.createTime;
		//pollChats(true); TODO
		if (updateGame)
			updateGame();

		List<User> users = new ArrayList<>();
		for (Player player : gameSession.getPlayers())
			users.add(player.user);
		usersAdapter.setUsers(users);

		int userCount = gameSession.getPlayers().size();
		if (userCount >= 4)
			lobbyStartButton.setText(R.string.lobby_start);
		else
			lobbyStartButton.setText(getResources().getQuantityString(R.plurals.lobby_start_with_bots, 4 - userCount, 4 - userCount));

		switch (gameSession.getState())
		{
			case LOBBY:
				lobbyStartButton.setVisibility(View.VISIBLE);
				userListRecyclerView.setVisibility(View.VISIBLE);
				availableActionsListView.setVisibility(View.GONE);
				break;
			case GAME:
				lobbyStartButton.setVisibility(View.GONE);
				userListRecyclerView.setVisibility(View.GONE);
				availableActionsListView.setVisibility(View.VISIBLE);
				break;
			case DELETED:
				getActivity().getSupportFragmentManager().popBackStack();
				break;
		}

		/*boolean soundsEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("sounds", true);
		zebiSounds.setEnabled(soundsEnabled && gameSession.getType() == GameType.ZEBI && gameSession.containsUser(121));*/
	}

	private void updateGame()
	{
		if (gameSession == null || gameSession.currentGameId == null)
			return;

		connectionViewModel.getApiInterface().getGame(gameSession.currentGameId).observeOn(AndroidSchedulers.mainThread()).subscribe(gameDTO ->
		{
			this.gameDTO = gameDTO;

			updateSeat();
			updateShortNames();
			updateGameState();
			statisticsPointsAdapter.setPoints(Utils.map(gameDTO.players, p -> p.points));

			cardsToFold.clear();

			chats.clear();
			lastChatTime = gameDTO.createTime;
			pollChats(true); //TODO is it ok here?

			actions.clear();
			nextActionOrdinal = 0;
			pollActions(true);

			//TODO: incrementpoints, update points at and of game
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

	private void updateShortNames()
	{
		Set<String> firstNames = new HashSet<>();
		Set<String> duplicateFirstNames = new HashSet<>();
		for (Player player : gameDTO.getPlayers())
		{
			String firstName = getFirstName(player.user.getName());
			if (!firstNames.add(firstName))
				duplicateFirstNames.add(firstName);
		}
		shortUserNames = new ArrayList<>();
		for (Player player : gameDTO.getPlayers())
		{
			String firstName = getFirstName(player.user.getName());
			if (!duplicateFirstNames.contains(firstName))
				shortUserNames.add(firstName);
			else
				shortUserNames.add(player.user.getName());
		}
		statisticsPointsAdapter.setNames(shortUserNames);
	}

	private void pollChats(boolean init)
	{
		if (chatDisposable != null)
			chatDisposable.dispose();

		chatDisposable = connectionViewModel.getApiInterface().getChat(gameSessionId, lastChatTime).observeOn(AndroidSchedulers.mainThread()).subscribe(newChats ->
		{
			chats.addAll(newChats);
			if (!init)
			{
				for (Chat chat : newChats)
				{
					int seat = getSeatOfUser(chat.user.id);
					if (seat >= 0)
						showPlayerMessageView(seat, chat.message, R.drawable.player_message_background_chat);
				}
			}

			if (!chats.isEmpty())
				lastChatTime = chats.get(chats.size() - 1).time + 1;
			updateMessagesView();
			pollChats(false);
		});
	}

	private void pollActions(boolean init)
	{
		if (actionDisposable != null)
			actionDisposable.dispose();

		if (gameDTO == null)
			return;

		actionDisposable = connectionViewModel.getApiInterface().getActions(gameDTO.id, nextActionOrdinal).observeOn(AndroidSchedulers.mainThread()).subscribe(newActions ->
		{
			actions.addAll(newActions);
			if (!init)
			{
				for (ActionDTO actionDTO : newActions)
				{
					String message = new Action(actionDTO.action).translate(getResources());
					if (message != null)
						showPlayerMessageView(actionDTO.seat, message, R.drawable.player_message_background);
				}
			}

			if (!actions.isEmpty())
				nextActionOrdinal = actions.get(actions.size() - 1).ordinal + 1;
			updateGameState();
			pollActions(false);
		});
	}

	private void updateGameState()
	{
		if (gameSession == null || gameSession.currentGameId == null)
			return;

		connectionViewModel.getApiInterface().getGameState(gameSession.currentGameId).observeOn(AndroidSchedulers.mainThread()).subscribe(newGameState ->
		{
			boolean phaseChange = gameStateDTO == null || !newGameState.phase.equals(gameStateDTO.phase);

			gameStateDTO = newGameState;

			if (phaseChange)
				phaseChanged();

			updateView();
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

	private void updateView()
	{
		if (!isKibic())
		{
			updateMyCardsView();
			updateOkButtonVisibility();
			updateAvailableActions();
		}
		updateTurnHighlight();
		updatePlayerNameViews();
		updateTeamColors();
		updateMessagesView();
		updatePlayedCards();
		updateStatistics();

		throwButton.setVisibility(gameStateDTO.canThrowCards ? View.VISIBLE : View.GONE);

		myCardsView.setVisibility(isKibic() ? View.GONE : View.VISIBLE);
		playerNameViews[0].setVisibility(isKibic() ? View.VISIBLE : View.GONE);
	}

	private void updateAvailableActions()
	{
		availableActionsAdapter.clear();
		for (String actionId : gameStateDTO.availableActions)
		{
			Action action = new Action(actionId);
			availableActionsAdapter.add(new ActionButtonItem()
			{
				@Override
				public Action getAction()
				{
					return action;
				}

				@Override
				public void onClicked()
				{
					doAction(new Action(actionId));
				}

				@NonNull
				@Override
				public String toString()
				{
					return action.translate(getResources());
				}
			});
		}
	}

	private void updateMessagesView()
	{
		StringBuilder messagesHtml = new StringBuilder();
		messagesHtml.append(getString(R.string.message_phase, getString(R.string.message_bidding)));

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
				message = action.translate(getResources());
				actionIndex++;

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
							int count = gameStateDTO.playerInfos.get(seat).tarockFoldCount;
							if (count > 0)
							{
								String tarockFoldMessage = getResources().getQuantityString(R.plurals.message_fold_tarock, count, count);
								messagesHtml.append(getString(stringTemplateRes, userName, tarockFoldMessage));
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
				messagesHtml.append(getString(stringTemplateRes, userName, message));
				messagesHtml.append("<br>");
			}

			if (phaseStringRes > 0)
			{
				messagesHtml.append("<br>");
				messagesHtml.append(getString(R.string.message_phase, getString(phaseStringRes)));
			}
		}

		messagesTextView.setText(Html.fromHtml(messagesHtml.toString()));
	}

	private void updateOkButtonVisibility()
	{
		PhaseEnum phaseEnum = PhaseEnum.fromID(gameStateDTO.phase);
		boolean okVisible = false;
		switch (phaseEnum)
		{
			case FOLDING:
			case ANNOUNCING:
			case END:
			case INTERRUPTED:
				if (gameStateDTO.playerInfos.get(mySeat).turn)
					okVisible = true;
				break;
		}
		okButton.setVisibility(okVisible ? View.VISIBLE : View.GONE);
	}

	private void updateMyCardsView()
	{
		Set<Card> removedCards = new HashSet<>(cardToViewMapping.keySet());
		for (String cardId : gameStateDTO.playerInfos.get(mySeat).cards)
		{
			Card card = Card.fromId(cardId);
			removedCards.remove(card);
			if (!cardToViewMapping.containsKey(card))
			{
				arrangeCards();
				return;
			}
		}
		for (Card card : removedCards)
			animateCardShrink(card);
	}

	private void animateCardShrink(Card card)
	{
		View myCardView = cardToViewMapping.remove(card);
		if (myCardView == null)
			return;

		ValueAnimator shrinkAnimator = ValueAnimator.ofInt(myCardView.getWidth(), 0);
		shrinkAnimator.addUpdateListener(animation ->
		{
			myCardView.getLayoutParams().width = (Integer)animation.getAnimatedValue();
			myCardView.requestLayout();
		});
		shrinkAnimator.setDuration(PLAY_DURATION);
		boolean shouldArrange = gameStateDTO.playerInfos.get(mySeat).cards.size() == CARDS_PER_ROW;
		shrinkAnimator.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				myCardsView0.removeView(myCardView);
				myCardsView1.removeView(myCardView);

				if (shouldArrange)
					arrangeCards();
			}
		});
		shrinkAnimator.start();
	}

	private void updatePlayedCards()
	{
		for (int seat = 0; seat < 4; seat++)
		{
			PlayedCardView playedCardView = playedCardViews[getPositionFromSeat(seat)];

			GameStateDTO.PlayerInfo playerInfo = gameStateDTO.playerInfos.get(seat);
			Card previousCard = Card.fromId(playerInfo.previousTrickCard);
			Card currentCard = Card.fromId(playerInfo.currentTrickCard);
			int dir = getPositionFromSeat(gameStateDTO.previousTrickWinner == null ? 0 : gameStateDTO.previousTrickWinner);

			if (previousCard != playedCardView.getTakenCard())
			{
				playedCardView.play(previousCard);
				playedCardView.take(dir);
				playedCardView.play(currentCard);
			}
			else if (currentCard != playedCardView.getCurrentCard())
			{
				playedCardView.play(currentCard);
			}
		}
	}

	public void phaseChanged()
	{
		PhaseEnum phase = PhaseEnum.fromID(gameStateDTO.phase);

		cardClickListener = null;

		switch (phase)
		{
			case BIDDING:
				showCenterView(MESSAGES_VIEW_INDEX);
				break;
			case FOLDING:
				cardsToFold.clear();
				setSkartCardClickListener();

				for (int seat = 0; seat < 4; seat++)
				{
					int count = gameStateDTO.playerInfos.get(seat).tarockFoldCount;
					if (count > 0)
					{
						String tarockFoldMessage = getResources().getQuantityString(R.plurals.message_fold_tarock, count, count);
						showPlayerMessageView(seat, tarockFoldMessage, R.drawable.player_message_background);
					}
				}
				break;
			case GAMEPLAY:
				showCenterViewDelayed(GAMEPLAY_VIEW_INDEX);
				setPlayCardClickListener();
				break;
			case END:
				showCenterViewDelayed(STATISTICS_VIEW_INDEX);
				break;
			case INTERRUPTED:
				showCenterView(MESSAGES_VIEW_INDEX);
				break;
		}

		skartViews[getPositionFromSeat(0)].setVisibility(phase.isAfter(PhaseEnum.FOLDING) ? View.VISIBLE : View.GONE);
	}

	/*@Override
	public void availableBids(List<Integer> bids)
	{
		availableActionsAdapter.clear();
		for (int bid : bids)
			availableActionsAdapter.add(new Bid(bid, bid == prevBid));
	}
	
	@Override
	public void bid(int player, int bid)
	{
		if (player == mySeat)
			availableActionsAdapter.clear();

		String msg = ResourceMappings.bidToName.get(bid == prevBid ? -2 : bid);
		displayPlayerActionMessage(player, msg);

		if (bid >= 0)
			prevBid = bid;
	}

	@Override
	public void availableCalls(List<Card> calls)
	{
		availableActionsAdapter.clear();
		availableActionsAdapter.addAll(calls);
	}

	@Override
	public void availableAnnouncements(List<Announcement> announcements)
	{
		availableActionsAdapter.clear();

		Collections.sort(announcements);

		if (gameDTO.getType() != GameType.PASKIEVICS)
			ultimoViewManager.takeAnnouncements(announcements);

		if (ultimoViewManager.hasAnyUltimo())
		{
			availableActionsAdapter.add(new ActionButtonItem()
			{
				@Override
				public void onClicked()
				{
					setUltimoViewVisible(true);
				}

				@Override
				public Action getAction()
				{
					return null;
				}

				@Override
				public String toString()
				{
					return getString(R.string.ultimo_button);
				}
			});
		}

		availableActionsAdapter.addAll(announcements);

		okButton.setVisibility(View.VISIBLE);
	}*/

	public void updateTeamColors()
	{
		for (int seat = 0; seat < 4; seat++)
		{
			int pos = getPositionFromSeat(seat);
			String team = gameStateDTO.playerInfos.get(seat).team;
			int color = getResources().getColor(team == null ? R.color.unknown_team : team.equals("caller") ? R.color.caller_team : R.color.opponent_team);

			if (seat == mySeat)
				cardsBackgroundColorView.setBackgroundColor(color);

			playerNameViews[pos].setTextColor(color);
		}
	}

	private void updateStatistics()
	{
		GameStateDTO.Statistics statistics = gameStateDTO.statistics;
		if (statistics == null)
		{
			statisticsGamepointsCaller.setText("");
			statisticsGamepointsOpponent.setText("");
			statisticsPointMultiplierView.setVisibility(View.GONE);
			statisticsCallerEntriesView.removeAllViews();
			statisticsOpponentEntriesView.removeAllViews();
			statisticsSumPointsView.setText("0");
			return;
		}

		Team selfTeam = isKibic() || "caller".equals(gameStateDTO.playerInfos.get(mySeat).team) ? Team.CALLER : Team.OPPONENT;

		statisticsGamepointsCaller.setText(String.valueOf(statistics.callerCardPoints));
		statisticsGamepointsOpponent.setText(String.valueOf(statistics.opponentCardPoints));

		statisticsPointMultiplierView.setVisibility(statistics.pointMultiplier == 1 ? View.GONE : View.VISIBLE);
		statisticsPointMultiplierView.setText(getString(R.string.statictics_point_multiplier, statistics.pointMultiplier));

		statisticsCallerEntriesView.removeAllViews();
		statisticsOpponentEntriesView.removeAllViews();
		for (Team team : Team.values())
		{
			List<GameStateDTO.AnnouncementResult> announcementResults = team == Team.CALLER ? statistics.callerAnnouncementResults : statistics.opponentAnnouncementResults;
			ViewGroup viewToAppend = team == Team.CALLER ? statisticsCallerEntriesView : statisticsOpponentEntriesView;

			for (GameStateDTO.AnnouncementResult announcementResult : announcementResults)
			{
				View entryView = layoutInflater.inflate(R.layout.statistics_entry, viewToAppend, false);
				TextView nameView = (TextView)entryView.findViewById(R.id.statistics_announcement_name);
				TextView pointsView = (TextView)entryView.findViewById(R.id.statistics_sum_points);

				nameView.setText(Announcement.fromID(announcementResult.announcement).translate(getResources()));
				int announcerPoints = announcementResult.points;
				if (announcerPoints < 0)
					nameView.setTextColor(getResources().getColor(R.color.announcement_failed));
				int myPoints = announcerPoints * (team == selfTeam ? 1 : -1);
				pointsView.setText(String.valueOf(myPoints));
				pointsView.setVisibility(myPoints == 0 ? View.GONE : View.VISIBLE);

				viewToAppend.addView(entryView);
			}
		}

		int selfSumPoints = statistics.sumPoints * (selfTeam == Team.CALLER ? 1 : -1);
		statisticsSumPointsView.setText(String.valueOf(selfSumPoints));
	}

	private void arrangeCards()
	{
		List<Card> myCards = new ArrayList<>();
		for (String cardId : gameStateDTO.playerInfos.get(mySeat).cards)
			myCards.add(Card.fromId(cardId));

		removeAllMyCardsView();
		
		Collections.sort(myCards);
		
		int cardCount = myCards.size();
		int cardsUp = cardCount <= CARDS_PER_ROW ? 0 : cardCount / 2;
		for (int i = 0; i < cardCount; i++)
		{
			final Card card = myCards.get(i);
			
			ImageView cardView = new ImageView(getActivity());
			cardView.setAdjustViewBounds(true);
			int margin = (int)(cardWidth * 0.1F / 2);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cardWidth - margin * 2, LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(margin, margin, margin, margin);
			cardView.setLayoutParams(lp);
			if (card != null)
				cardView.setImageResource(ResourceMappings.getBitmapResForCard(card));
			
			final LinearLayout parentView = i < cardsUp ? myCardsView1 : myCardsView0;
			parentView.addView(cardView);
			cardToViewMapping.put(card, cardView);

			cardView.setTag(card);
			cardView.setOnClickListener(view ->
			{
				if (cardClickListener != null && gameStateDTO.playerInfos.get(mySeat).turn)
					cardClickListener.onClick(view);
			});
		}
	}

	private void setSkartCardClickListener()
	{
		cardClickListener = v ->
		{
			Card card = (Card)v.getTag();
			if (!cardsToFold.contains(card))
			{
				cardsToFold.add(card);
				Animation a = new TranslateAnimation(0, 0, 0, -v.getHeight() / 5);
				a.setDuration(300);
				a.setFillAfter(true);
				v.startAnimation(a);
			}
			else
			{
				cardsToFold.remove(card);
				Animation a = new TranslateAnimation(0, 0, -v.getHeight() / 5, 0);
				a.setDuration(300);
				a.setFillAfter(true);
				v.startAnimation(a);
			}
		};
	}

	private void setPlayCardClickListener()
	{
		cardClickListener = new DoubleClickListener(getContext(), v ->
		{
			doAction(Action.play((Card)v.getTag()));
		});
	}

	private void removeAllMyCardsView()
	{
		cardToViewMapping.clear();
		
		for (ViewGroup cardsView : new ViewGroup[]{myCardsView0, myCardsView1})
		{
			int c = cardsView.getChildCount();
			for (int i = 0; i < c; i++)
			{
				ImageView iv = (ImageView)cardsView.getChildAt(i);
				//((BitmapDrawable)iv.getDrawable()).getBitmap().recycle();
				iv.setImageBitmap(null);
			}
			cardsView.removeAllViews();
		}
	}

	private void setUltimoViewVisible(boolean visible)
	{
		ultimoView.setVisibility(visible ? View.VISIBLE : View.GONE);
		messagesView.setVisibility(visible ? View.GONE : View.VISIBLE);
	}
	
	private void showCenterView(int item)
	{
		pendingCenterView = item;
		centerSpace.setCurrentItem(item);
	}
	
	private int pendingCenterView;
	private void showCenterViewDelayed(int item)
	{
		pendingCenterView = item;
		new Handler().postDelayed(() ->
		{
			if (pendingCenterView != item)
				return;

			showCenterView(pendingCenterView);
		}, DELAY);
	}

	private void showPlayerMessageView(int player, String msg, int backgroundRes)
	{
		final TextView view = playerMessageViews[getPositionFromSeat(player)];
		view.setText(Html.fromHtml(msg));
		view.setBackgroundResource(backgroundRes);
		view.setVisibility(View.VISIBLE);
		Animation fadeAnimation = new AlphaAnimation(1, 0);
		fadeAnimation.setDuration(500);
		fadeAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis() + DELAY);
		fadeAnimation.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
			}
			
			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
			
			@Override
			public void onAnimationEnd(Animation animation)
			{
				view.setVisibility(View.GONE);
			}
		});
		view.setAnimation(fadeAnimation);
	}

	private void updateTurnHighlight()
	{
		for (int seat = 0; seat < 4; seat++)
		{
			int pos = getPositionFromSeat(seat);
			boolean val = gameStateDTO.playerInfos.get(seat).turn;

			if (pos == 0)
			{
				cardsHighlightView.setVisibility(val ? View.VISIBLE : View.GONE);
			}

			if (val)
				playerNameViews[pos].setBackgroundResource(R.drawable.name_highlight);
			else
				playerNameViews[pos].setBackgroundColor(Color.TRANSPARENT);
		}
	}

	private boolean isKibic()
	{
		return mySeat < 0;
	}
	
	private int getPositionFromSeat(int id)
	{
		int viewID = isKibic() ? 0 : mySeat;
		return (id - viewID + 4) % 4;
	}
}

package com.tisza.tarock.gui;

import android.animation.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.view.animation.Animation.*;
import android.view.inputmethod.*;
import android.widget.*;
import androidx.appcompat.app.*;
import androidx.lifecycle.*;
import androidx.preference.*;
import androidx.recyclerview.widget.*;
import androidx.viewpager.widget.*;
import com.tisza.tarock.R;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.zebisound.*;

import java.util.*;

public class GameFragment extends MainActivityFragment implements EventHandler, TextView.OnEditorActionListener
{
	public static final String TAG = "game";
	public static final String KEY_GAME_ID = "game_id";
	public static final String KEY_HISTORY_GAME_ID = "history_game_id";
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
					if (actionButton.getAction() != null)
						doAction(actionButton.getAction());
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

			if (gameInfo.getUsers().size() >= 4)
				connectionViewModel.sendMessage(startMessage);
			else
				new AlertDialog.Builder(getContext())
						.setTitle(Html.fromHtml(getString(R.string.lobby_start_with_bots_confirm_title)))
						.setMessage(Html.fromHtml(getString(R.string.lobby_start_with_bots_confirm_body)))
						.setPositiveButton(R.string.lobby_start_with_bots_confirm_yes, (dialog, which) -> connectionViewModel.sendMessage(startMessage))
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

		myUserID = connectionViewModel.getUserID().getValue();

		for (ZebiSound zebiSound : zebiSounds.getZebiSounds())
		{
			connectionViewModel.addEventHandler(zebiSound);
		}
		connectionViewModel.addEventHandler(this);

		if (!getArguments().containsKey(KEY_GAME_ID))
			throw new IllegalArgumentException("no game id given");

		int gameID = getArguments().getInt(KEY_GAME_ID);
		connectionViewModel.sendMessage(MainProto.Message.newBuilder().setJoinGameSession(MainProto.JoinGameSession.newBuilder()
				.setGameSessionId(gameID)
				.build())
				.build());

		connectionViewModel.getGameByID(gameID).observe(this, this::onGameInfoUpdate);
		connectionViewModel.getUsers().observe(this, u -> users = u);

		return contentView;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		for (ZebiSound zebiSound : zebiSounds.getZebiSounds())
		{
			connectionViewModel.removeEventHandler(zebiSound);
		}
		connectionViewModel.removeEventHandler(this);
		connectionViewModel.sendMessage(MainProto.Message.newBuilder().setJoinGameSession(MainProto.JoinGameSession.newBuilder()
				.build())
				.build());
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

	private void onGameInfoUpdate(GameInfo gameInfo)
	{
		this.gameInfo = gameInfo;

		if (gameInfo == null)
		{
			getActivity().getSupportFragmentManager().popBackStack();
			return;
		}

		updateSeat();

		usersAdapter.setUsers(gameInfo.getUsers());

		Set<String> firstNames = new HashSet<>();
		Set<String> duplicateFirstNames = new HashSet<>();
		for (User user : gameInfo.getUsers())
		{
			String firstName = getFirstName(user.getName());
			if (!firstNames.add(firstName))
				duplicateFirstNames.add(firstName);
		}
		shortUserNames = new ArrayList<>();
		for (User user : gameInfo.getUsers())
		{
			String firstName = getFirstName(user.getName());
			if (!duplicateFirstNames.contains(firstName))
				shortUserNames.add(firstName);
			else
				shortUserNames.add(user.getName());
		}
		statisticsPointsAdapter.setNames(shortUserNames);

		int userCount = gameInfo.getUsers().size();
		if (userCount >= 4)
			lobbyStartButton.setText(R.string.lobby_start);
		else
			lobbyStartButton.setText(getResources().getQuantityString(R.plurals.lobby_start_with_bots, 4 - userCount, 4 - userCount));

		switch (gameInfo.getState())
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

		boolean soundsEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("sounds", true);
		zebiSounds.setEnabled(soundsEnabled && gameInfo.getType() == GameType.ZEBI && gameInfo.containsUser(121));
	}

	private User getUserOfPlayer(int player)
	{
		if (gameInfo == null || gameInfo.getState() != GameSessionState.GAME)
			return null;

		return gameInfo.getUsers().get((beginnerPlayerIndex + player) % gameInfo.getUsers().size());
	}

	private int getPlayerOfUser(int userID)
	{
		for (int i = 0; i < 4; i++)
			if (getUserOfPlayer(i) != null && getUserOfPlayer(i).getId() == userID)
				return i;

		return -1;
	}

	public static String getFirstName(String name)
	{
		return name.substring(name.lastIndexOf(' ') + 1);
	}

	private String getPlayerName(int player)
	{
		User user = getUserOfPlayer(player);
		if (user == null)
			return getString(R.string.empty_seat);

		return shortUserNames.get(gameInfo.getUsers().indexOf(user));
	}

	private void updateSeat()
	{
		seat = getPlayerOfUser(myUserID);

		for (int i = 0; i < 4; i++)
		{
			TextView playerNameView = playerNameViews[getPositionFromPlayerID(i)];
			playerNameView.setText(R.string.empty_seat);
			User user = getUserOfPlayer(i);
			if (user != null)
			{
				playerNameView.setText(user.getName());
				playerNameView.setAlpha(user.isOnline() ? 1F : 0.5F);
			}
		}

		myCardsView.setVisibility(isKibic() ? View.GONE : View.VISIBLE);
		playerNameViews[0].setVisibility(isKibic() ? View.VISIBLE : View.GONE);
	}

	private List<User> users;
	private List<String> shortUserNames;
	private GameInfo gameInfo;
	private int myUserID;
	private List<Card> myCards;
	private int seat = -1;
	private Team myTeam;
	private GameType gameType;
	private int beginnerPlayerIndex;

	private Map<Card, View> cardToViewMapping = new HashMap<>();
	
	private PhaseEnum gamePhase;
	
	private String messagesHtml = "";

	private OnClickListener cardClickListener;
	private List<Card> cardsToSkart = new ArrayList<>();
	private int prevBid;

	@Override
	public void startGame(GameType gameType, int beginnerPlayer)
	{
		resetGameViews();

		this.gameType = gameType;
		this.beginnerPlayerIndex = beginnerPlayer;
		myTeam = null;

		updateSeat();

		messagesHtml = "";
		prevBid = 4;
	}

	private void doAction(Action action)
	{
		connectionViewModel.sendMessage(MainProto.Message.newBuilder().setAction(action.getId()).build());
	}

	@Override
	public void chat(int userID, String message)
	{
		int player = getPlayerOfUser(userID);
		if (player >= 0)
		{
			showPlayerMessageView(player, message, R.drawable.player_message_background_chat);
			displayMessage(getString(R.string.message_chat, getPlayerName(player), message));
		}
		else
		{
			for (User kibicUser : users)
				if (kibicUser.getId() == userID)
					displayMessage(getString(R.string.message_chat, kibicUser.getName(), message));
		}
	}

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
	{
		if ((view == messagesChatEditText || view == statisticsChatEditText) && actionId == EditorInfo.IME_ACTION_SEND)
		{
			Editable text = ((EditText)view).getText();
			if (text.length() == 0)
				return false;
			connectionViewModel.sendMessage(MainProto.Message.newBuilder().setChat(MainProto.Chat.newBuilder().setMessage(text.toString())).build());
			text.clear();
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			return true;
		}

		return false;
	}

	@Override
	public void cardsChanged(List<Card> cards, boolean canBeThrown)
	{
		myCards = cards;
		arrangeCards();
		throwButton.setVisibility(canBeThrown ? View.VISIBLE : View.GONE);
	}

	@Override
	public void phaseChanged(PhaseEnum phase)
	{
		gamePhase = phase;

		if (phase == PhaseEnum.BIDDING)
		{
			showCenterView(MESSAGES_VIEW_INDEX);
			displayMessage(R.string.message_bidding);
		}
		else if (phase == PhaseEnum.CHANGING)
		{
			showCenterView(MESSAGES_VIEW_INDEX);
			higlightAllName();
			displayMessage(R.string.message_folding);
			setSkartCardClickListener();
		}
		else if (phase == PhaseEnum.CALLING)
		{
			showCenterView(MESSAGES_VIEW_INDEX);
			displayMessage(R.string.message_calling);
		}
		else if (phase == PhaseEnum.ANNOUNCING)
		{
			showCenterView(MESSAGES_VIEW_INDEX);
			displayMessage(R.string.message_announcing);
		}
		else if (phase == PhaseEnum.GAMEPLAY)
		{
			showCenterViewDelayed(GAMEPLAY_VIEW_INDEX);
			setPlayCardClickListener();
		}
		else if (phase == PhaseEnum.END)
		{
			showCenterViewDelayed(STATISTICS_VIEW_INDEX);
		}
		else if (phase == PhaseEnum.INTERRUPTED)
		{
			availableActionsAdapter.clear();
			displayMessage(R.string.message_press_ok);
		}

		skartViews[getPositionFromPlayerID(0)].setVisibility(phase.isAfter(PhaseEnum.CHANGING) ? View.VISIBLE : View.GONE);
	}

	@Override
	public void throwCards(int player)
	{
		displayPlayerActionMessage(player, getString(R.string.message_cards_thrown));
	}

	@Override
	public void availableBids(List<Integer> bids)
	{
		availableActionsAdapter.clear();
		for (int bid : bids)
			availableActionsAdapter.add(new Bid(bid, bid == prevBid));
	}
	
	@Override
	public void bid(int player, int bid)
	{
		if (player == seat)
			availableActionsAdapter.clear();

		String msg = ResourceMappings.bidToName.get(bid == prevBid ? -2 : bid);
		displayPlayerActionMessage(player, msg);

		if (bid >= 0)
			prevBid = bid;
	}

	@Override
	public void foldDone(int player)
	{
		setHiglighted(player, false);
		if (player == seat)
		{
			okButton.setVisibility(View.GONE);
			throwButton.setVisibility(View.GONE);
			cardsToSkart.clear();
			cardClickListener = null;
		}
	}

	@Override
	public void fold(int player, List<Card> cards)
	{
		StringBuilder msg = null;
		for (Card card : cards)
		{
			String cardName = ResourceMappings.uppercaseCardName(card);
			if (msg == null)
				msg = new StringBuilder(cardName);
			else
				msg.append(", ").append(cardName);
		}

		if (msg == null)
			return;

		displayPlayerActionMessage(player, getString(R.string.message_fold, msg.toString()));
	}

	@Override
	public void foldTarock(int[] counts)
	{
		for (int p = 0; p < 4; p++)
		{
			int count = counts[p];
			if (count > 0)
			{
				String msg = getResources().getQuantityString(R.plurals.message_fold_tarock, count, count);
				displayPlayerActionMessage(p, msg);
			}
		}
	}

	@Override
	public void availableCalls(List<Card> calls)
	{
		availableActionsAdapter.clear();
		availableActionsAdapter.addAll(calls);
	}
	
	@Override
	public void call(int player, Card card)
	{
		if (player == seat)
			availableActionsAdapter.clear();

		displayPlayerActionMessage(player, ResourceMappings.uppercaseCardName(card));
	}
	
	@Override
	public void availableAnnouncements(List<Announcement> announcements)
	{
		availableActionsAdapter.clear();

		Collections.sort(announcements);

		if (gameType != GameType.PASKIEVICS)
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
		okButton.setOnClickListener(v ->
		{
			if (ultimoView.getVisibility() == View.GONE)
				doAction(Action.announcePassz());
		});
	}

	@Override
	public void announce(int player, Announcement announcement)
	{
		if (announcement.getName().equals("jatek") && announcement.getContraLevel() == 0)
			return;

		if (player == seat)
		{
			okButton.setVisibility(View.GONE);
			availableActionsAdapter.clear();
		}

		setUltimoViewVisible(false);

		String msg = announcement.toString();
		displayPlayerActionMessage(player, msg);
	}

	@Override
	public void announcePassz(int player)
	{
		if (player == seat)
		{
			okButton.setVisibility(View.GONE);
			availableActionsAdapter.clear();
		}

		showPlayerMessageView(player, getString(R.string.passz), R.drawable.player_message_background);
	}

	@Override
	public void playCard(int player, Card card)
	{
		PlayedCardView playedCardView = playedCardViews[getPositionFromPlayerID(player)];
		playedCardView.play(card);

		if (myCards == null || player != seat)
			return;

		myCards.remove(card);
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
		boolean shouldArrange = myCards.size() == CARDS_PER_ROW;
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
	
	@Override
	public void cardsTaken(final int winnerPlayer)
	{
		for (PlayedCardView playedCardView : playedCardViews)
		{
			playedCardView.take(getPositionFromPlayerID(winnerPlayer));
		}
	}
	
	@Override
	public void turn(int player)
	{
		if (gamePhase == PhaseEnum.CHANGING)
		{
			okButton.setVisibility(View.VISIBLE);
			okButton.setOnClickListener(v -> doAction(Action.fold(cardsToSkart)));
		}
		else
		{
			highlightName(player);
		}
	}

	@Override
	public void playerTeamInfo(int player, Team team)
	{
		int pos = getPositionFromPlayerID(player);
		int color = getResources().getColor(team == Team.CALLER ? R.color.caller_team : R.color.opponent_team);

		if (player == seat)
		{
			myTeam = team;
			cardsBackgroundColorView.setBackgroundColor(color);
		}

		playerNameViews[pos].setTextColor(color);
	}

	@Override
	public void statistics(int callerGamePoints, int opponentGamePoints, List<AnnouncementResult> announcementResults, int sumPoints, int pointMultiplier)
	{
		Team selfTeam = myTeam == null ? Team.CALLER : myTeam;

		statisticsGamepointsCaller.setText(String.valueOf(callerGamePoints));
		statisticsGamepointsOpponent.setText(String.valueOf(opponentGamePoints));

		statisticsPointMultiplierView.setVisibility(pointMultiplier == 1 ? View.GONE : View.VISIBLE);
		statisticsPointMultiplierView.setText(getString(R.string.statictics_point_multiplier, pointMultiplier));

		statisticsCallerEntriesView.removeAllViews();
		statisticsOpponentEntriesView.removeAllViews();
		for (AnnouncementResult announcementResult : announcementResults)
		{
			ViewGroup viewToAppend = announcementResult.getTeam() == Team.CALLER ? statisticsCallerEntriesView : statisticsOpponentEntriesView;

			View entryView = layoutInflater.inflate(R.layout.statistics_entry, viewToAppend, false);
			TextView nameView = (TextView)entryView.findViewById(R.id.statistics_announcement_name);
			TextView pointsView = (TextView)entryView.findViewById(R.id.statistics_sum_points);

			nameView.setText(announcementResult.getAnnouncement().toString());
			int announcerPoints = announcementResult.getPoints();
			if (announcerPoints < 0)
				nameView.setTextColor(getResources().getColor(R.color.announcement_failed));
			int myPoints = announcerPoints * (announcementResult.getTeam() == selfTeam ? 1 : -1);
			pointsView.setText(String.valueOf(myPoints));
			pointsView.setVisibility(myPoints == 0 ? View.GONE : View.VISIBLE);

			viewToAppend.addView(entryView);
		}

		int selfSumPoints = sumPoints * (selfTeam == Team.CALLER ? 1 : -1);
		statisticsSumPointsView.setText(String.valueOf(selfSumPoints));
	}

	@Override
	public void playerPoints(List<Integer> points, List<Integer> incrementPoints)
	{
		statisticsPointsAdapter.setPoints(points);
		statisticsPointsAdapter.setIncrementPoints(incrementPoints);
	}

	@Override
	public void pendingNewGame()
	{
		if (!isKibic())
		{
			okButton.setVisibility(View.VISIBLE);
			okButton.setOnClickListener(v -> doAction(Action.readyForNewGame()));
		}

		higlightAllName();
	}

	@Override
	public void readyForNewGame(int player)
	{
		if (player == seat)
			okButton.setVisibility(View.GONE);

		setHiglighted(player, false);
	}

	@Override
	public void wrongAction()
	{
		displayMessage("error");
	}

	private void arrangeCards()
	{
		removeAllMyCardsView();

		if (myCards == null)
			return;
		
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
				if (cardClickListener != null)
					cardClickListener.onClick(view);
			});
		}
	}

	private void setSkartCardClickListener()
	{
		cardClickListener = v ->
		{
			Card card = (Card)v.getTag();
			if (!cardsToSkart.contains(card))
			{
				cardsToSkart.add(card);
				Animation a = new TranslateAnimation(0, 0, 0, -v.getHeight() / 5);
				a.setDuration(300);
				a.setFillAfter(true);
				v.startAnimation(a);
			}
			else
			{
				cardsToSkart.remove(card);
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

	private void displayMessage(int msgRes, Object ... formatArgs)
	{
		displayMessage(getString(msgRes, formatArgs));
	}

	private void displayMessage(String msg)
	{
		messagesHtml += msg;
		messagesTextView.setText(Html.fromHtml(messagesHtml));
		messagesHtml += "<br>";
	}

	private void displayPlayerActionMessage(int player, String msg)
	{
		displayMessage(getPlayerName(player) + ": " + msg);
		showPlayerMessageView(player, msg, R.drawable.player_message_background);
	}
	
	private void showPlayerMessageView(int player, String msg, int backgroundRes)
	{
		final TextView view = playerMessageViews[getPositionFromPlayerID(player)];
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
	
	private void highlightName(int player)
	{
		for (int i = 0; i < 4; i++)
		{
			setHiglighted(i, player == i);
		}
	}
	
	private void higlightAllName()
	{
		for (int i = 0; i < 4; i++)
		{
			setHiglighted(i, true);
		}
	}
	
	private void setHiglighted(int player, boolean val)
	{
		int pos = getPositionFromPlayerID(player);
		
		if (pos == 0)
		{
			cardsHighlightView.setVisibility(val ? View.VISIBLE : View.GONE);
		}

		if (val)
		{
			playerNameViews[pos].setBackgroundResource(R.drawable.name_highlight);
		}
		else
		{
			playerNameViews[pos].setBackgroundColor(Color.TRANSPARENT);
		}
	}

	private boolean isKibic()
	{
		return seat < 0;
	}
	
	private int getPositionFromPlayerID(int id)
	{
		int viewID = isKibic() ? 0 : seat;
		return (id - viewID + 4) % 4;
	}
}

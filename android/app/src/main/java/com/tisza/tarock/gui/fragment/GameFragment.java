package com.tisza.tarock.gui.fragment;

import android.animation.*;
import android.content.*;
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
import androidx.recyclerview.widget.*;
import androidx.viewpager.widget.*;
import com.tisza.tarock.R;
import com.tisza.tarock.api.model.*;
import com.tisza.tarock.databinding.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.Action;
import com.tisza.tarock.gui.adapter.*;
import com.tisza.tarock.gui.misc.*;
import com.tisza.tarock.gui.view.*;
import com.tisza.tarock.gui.viewmodel.*;
import com.tisza.tarock.zebisound.*;

import java.util.*;

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

	private GameBinding gameBinding;
	private MessagesBinding messagesBinding;
	private StatisticsBinding statisticsBinding;

	private TextView[] playerMessageViews;
	private Button availableActionsListUltimoButton;
	private RelativeLayout gameplayView;
	private PlayedCardView[] playedCardViews;

	private UsersAdapter usersAdapter;
	private ArrayAdapter<Action> availableActionsAdapter;
	private UltimoViewManager ultimoViewManager;
	private StatisticsPointsAdapter statisticsPointsAdapter;

	private AuthenticationViewModel authenticationViewModel;
	private GameViewModel gameViewModel;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		int gameSessionId = getArguments().getInt(KEY_GAME_SESSION_ID);

		layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		zebiSounds = new ZebiSounds(getActivity());
		authenticationViewModel = ViewModelProviders.of(getActivity()).get(AuthenticationViewModel.class);
		gameViewModel = ViewModelProviders.of(this, new GameViewModel.Factory(getActivity().getApplication(), gameSessionId)).get(GameViewModel.class);
	}

	private void setupBinding()
	{
		gameViewModel.getGameState().observe(this, this::updateView);
		for (int i = 0; i < 4; i++)
		{
			int dir = i;
			gameViewModel.getActionBubbleContent(dir).observe(this, message -> showPlayerMessageView(dir, message, R.drawable.player_message_background));
			gameViewModel.getChatBubbleContent(dir).observe(this, message -> showPlayerMessageView(dir, message, R.drawable.player_message_background_chat));
		}

		gameViewModel.getShortUserNames().observe(this, names -> statisticsPointsAdapter.setNames(names));

		gameViewModel.getPhase().observe(this, this::phaseChanged);

		gameViewModel.getGameSession().observe(this, gameSession ->
		{
			if (gameSession == null || gameSession.getState() == GameSessionState.DELETED)
			{
				getActivity().getSupportFragmentManager().popBackStack();
				return;
			}

			List<User> users = new ArrayList<>();
			for (Player player : gameSession.getPlayers())
				users.add(player.user);
			usersAdapter.setUsers(users);
		});
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		gameBinding = GameBinding.inflate(inflater, container, false);
		gameBinding.setGameViewModel(gameViewModel);
		gameBinding.setLifecycleOwner(this);

		cardWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth() / CARDS_PER_ROW;

		messagesBinding = MessagesBinding.inflate(layoutInflater, gameBinding.centerSpace, false);
		messagesBinding.setGameViewModel(gameViewModel);
		messagesBinding.setLifecycleOwner(this);

		statisticsBinding = StatisticsBinding.inflate(layoutInflater, gameBinding.centerSpace, false);
		statisticsBinding.setGameViewModel(gameViewModel);
		statisticsBinding.setLifecycleOwner(this);

		playerMessageViews = new TextView[] {gameBinding.playerMessage0, gameBinding.playerMessage1, gameBinding.playerMessage2, gameBinding.playerMessage3};

		gameBinding.okButton.setOnClickListener(v ->
		{
			switch (gameViewModel.getGameState().getValue().phase)
			{
				case FOLDING:
					gameViewModel.doAction(Action.fold(cardsToFold));
					break;
				case ANNOUNCING:
					if (messagesBinding.ultimoView.getVisibility() == View.GONE)
						gameViewModel.doAction(Action.announcePassz());
					break;
				case END:
				case INTERRUPTED:
					gameViewModel.doAction(Action.readyForNewGame());
					break;
			}
		});

		availableActionsAdapter = new ArrayAdapter<Action>(getActivity(), R.layout.button)
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				TextView view = (TextView) super.getView(position, convertView, parent);
				Action action = getItem(position);
				view.setOnClickListener(new DoubleClickListener(getContext(), v -> gameViewModel.doAction(action)));
				view.setText(action.translate(getResources()));
				return view;
			}
		};

		messagesBinding.availableActionsList.setAdapter(availableActionsAdapter);
		availableActionsListUltimoButton = (Button) inflater.inflate(R.layout.button, messagesBinding.availableActionsList, false);
		availableActionsListUltimoButton.setText(R.string.ultimo_button);
		availableActionsListUltimoButton.setOnClickListener(view -> setUltimoViewVisible(true));
		((EditText) messagesBinding.messagesChatEditText).setRawInputType(InputType.TYPE_CLASS_TEXT);
		((EditText) messagesBinding.messagesChatEditText).setOnEditorActionListener(this);
		messagesBinding.gameUserListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		messagesBinding.gameUserListRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
		usersAdapter = new UsersAdapter(getContext());
		messagesBinding.gameUserListRecyclerView.setAdapter(usersAdapter);
		messagesBinding.lobbyStartButton.setOnClickListener(v ->
		{
			if (gameViewModel.getGameSession().getValue().getPlayers().size() >= 4)
				gameViewModel.startGameSession();
			else
				new AlertDialog.Builder(getContext())
						.setTitle(Html.fromHtml(getString(R.string.lobby_start_with_bots_confirm_title)))
						.setMessage(Html.fromHtml(getString(R.string.lobby_start_with_bots_confirm_body)))
						.setPositiveButton(R.string.lobby_start_with_bots_confirm_yes, (dialog, which) -> gameViewModel.startGameSession())
						.setNegativeButton(R.string.cancel, null)
						.show();

		});

		ultimoViewManager = new UltimoViewManager(getActivity(), layoutInflater, (LinearLayout)messagesBinding.getRoot().findViewById(R.id.ultimo_spinner_list));
		messagesBinding.ultimoBackButon.setOnClickListener(v -> setUltimoViewVisible(false));

		messagesBinding.ultimoAnnounceButton.setOnClickListener(new DoubleClickListener(getContext(), v ->
		{
			Announcement announcement = ultimoViewManager.getCurrentSelectedAnnouncement();

			if (announcement == null)
				throw new RuntimeException();

			gameViewModel.doAction(Action.announce(announcement));
			setUltimoViewVisible(false);
		}));

		gameplayView = (RelativeLayout)layoutInflater.inflate(R.layout.gameplay, gameBinding.centerSpace, false);
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


		statisticsBinding.statisticsPoints.setLayoutManager(new GridLayoutManager(getContext(), 4));
		statisticsPointsAdapter = new StatisticsPointsAdapter();
		statisticsBinding.statisticsPoints.setAdapter(statisticsPointsAdapter);
		((EditText) statisticsBinding.statisticsChatEditText).setRawInputType(InputType.TYPE_CLASS_TEXT);
		((EditText) statisticsBinding.statisticsChatEditText).setOnEditorActionListener(this);

		View[] centerViews = {messagesBinding.getRoot(), gameplayView, statisticsBinding.getRoot()};
		int[] centerViewTitles = {R.string.pager_announcing, R.string.pager_gameplay, R.string.pager_statistics};
		gameBinding.centerSpace.setAdapter(new CenterViewPagerAdapter(getActivity(), centerViews, centerViewTitles));

		if (!getArguments().containsKey(KEY_GAME_SESSION_ID))
			throw new IllegalArgumentException("no game id given");

		authenticationViewModel.getLoggedInUser().observe(this, gameViewModel::setLoggedInUser);

		/*boolean soundsEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("sounds", true);
		zebiSounds.setEnabled(soundsEnabled && gameSession.getType() == GameType.ZEBI && gameSession.containsUser(121));*/

		setupBinding();

		return gameBinding.getRoot();
	}

	private Map<Card, View> cardToViewMapping = new HashMap<>();
	private OnClickListener cardClickListener;

	private List<Card> cardsToFold = new ArrayList<>();

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
	{
		if ((view == messagesBinding.messagesChatEditText || view == statisticsBinding.statisticsChatEditText) && actionId == EditorInfo.IME_ACTION_SEND)
		{
			Editable text = ((EditText)view).getText();
			if (text.length() == 0)
				return false;
			gameViewModel.sendChat(text.toString());
			text.clear();
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			return true;
		}

		return false;
	}

	private void updateView(GameState gameState)
	{
		if (gameState == null)
			return;

		if (gameViewModel.getMySeat().getValue() != null)
		{
			updateMyCardsView(gameState);
			updateOkButtonVisibility(gameState);
			updateAvailableActions(gameState);
		}
		updatePlayedCards(gameState);
		updateStatistics(gameState);
	}

	private void updateAvailableActions(GameState gameState)
	{
		if (messagesBinding.ultimoView.getVisibility() == View.VISIBLE)
			return;

		availableActionsAdapter.clear();
		messagesBinding.availableActionsList.removeHeaderView(availableActionsListUltimoButton);

		if (gameState.phase == PhaseEnum.ANNOUNCING)
		{
			List<Announcement> announcements = new ArrayList<>();
			for (String actionId : gameState.availableActions)
				announcements.add(Announcement.fromID(new Action(actionId).getParams()));

			if (gameState.type != GameType.PASKIEVICS)
				ultimoViewManager.takeAnnouncements(announcements);

			if (ultimoViewManager.hasAnyUltimo())
				messagesBinding.availableActionsList.addHeaderView(availableActionsListUltimoButton);

			for (Announcement announcement : announcements)
				availableActionsAdapter.add(Action.announce(announcement));
		}
		else
		{
			for (String actionId : gameState.availableActions)
				availableActionsAdapter.add(new Action(actionId));
		}
	}

	private void updateOkButtonVisibility(GameState gameState)
	{
		boolean okVisible = false;
		switch (gameState.phase)
		{
			case FOLDING:
			case ANNOUNCING:
			case END:
			case INTERRUPTED:
				if (gameState.playersRotated.get(0).turn)
					okVisible = true;
				break;
		}
		gameBinding.okButton.setVisibility(okVisible ? View.VISIBLE : View.GONE);
	}

	private void updateMyCardsView(GameState gameState)
	{
		Set<Card> removedCards = new HashSet<>(cardToViewMapping.keySet());
		for (String cardId : gameState.playersRotated.get(0).cards)
		{
			Card card = Card.fromId(cardId);
			removedCards.remove(card);
			if (!cardToViewMapping.containsKey(card))
			{
				arrangeCards(gameState);
				return;
			}
		}
		for (Card card : removedCards)
			animateCardShrink(gameState, card);
	}

	private void animateCardShrink(GameState gameState, Card card)
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
		boolean shouldArrange = gameState.playersRotated.get(0).cards.size() == CARDS_PER_ROW;
		shrinkAnimator.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				gameBinding.myCards0.removeView(myCardView);
				gameBinding.myCards0.removeView(myCardView);

				if (shouldArrange)
					arrangeCards(gameState);
			}
		});
		shrinkAnimator.start();
	}

	private void updatePlayedCards(GameState gameState)
	{
		for (int dir = 0; dir < 4; dir++)
		{
			PlayedCardView playedCardView = playedCardViews[dir];

			GameStateDTO.PlayerInfo playerInfo = gameState.playersRotated.get(dir);
			Card previousCard = Card.fromId(playerInfo.previousTrickCard);
			Card currentCard = Card.fromId(playerInfo.currentTrickCard);
			int takeDir = gameState.previousTrickWinnerDirection == null ? 0 : gameState.previousTrickWinnerDirection;

			if (previousCard != playedCardView.getTakenCard())
			{
				playedCardView.play(previousCard);
				playedCardView.take(takeDir);
				playedCardView.play(currentCard);
			}
			else if (currentCard != playedCardView.getCurrentCard())
			{
				playedCardView.play(currentCard);
			}
		}
	}

	public void phaseChanged(PhaseEnum phase)
	{
		cardClickListener = null;

		switch (phase)
		{
			case BIDDING:
				showCenterView(MESSAGES_VIEW_INDEX);
				break;
			case FOLDING:
				cardsToFold.clear();
				setSkartCardClickListener();
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
	}

	private void updateStatistics(GameState gameState)
	{
		GameStateDTO.Statistics statistics = gameState.statistics;
		if (statistics == null)
		{
			statisticsBinding.statisticsCallerEntriesList.removeAllViews();
			statisticsBinding.statisticsOpponentEntriesList.removeAllViews();
			return;
		}

		Team selfTeam = gameViewModel.getMySeat().getValue() == null || "caller".equals(gameState.playersRotated.get(0).team) ? Team.CALLER : Team.OPPONENT;

		statisticsBinding.statisticsCallerEntriesList.removeAllViews();
		statisticsBinding.statisticsOpponentEntriesList.removeAllViews();
		for (Team team : Team.values())
		{
			List<GameStateDTO.AnnouncementResult> announcementResults = team == Team.CALLER ? statistics.callerAnnouncementResults : statistics.opponentAnnouncementResults;
			ViewGroup viewToAppend = team == Team.CALLER ? statisticsBinding.statisticsCallerEntriesList : statisticsBinding.statisticsOpponentEntriesList;

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
		statisticsBinding.statisticsSumPoints.setText(String.valueOf(selfSumPoints));

		List<Integer> pointsList = new ArrayList<>();
		List<Integer> incrementPointsList = new ArrayList<>();
		//TODO csunya
		for (Player player : gameViewModel.getGameSession().getValue().players)
		{
			int points = player.points;
			int gamePoints = 0;
			for (GameStateDTO.PlayerInfo inGamePlayer : gameState.playersRotated)
				if (inGamePlayer.user.id == player.user.id)
					gamePoints = inGamePlayer.points;

			pointsList.add(points + gamePoints);
			incrementPointsList.add(gamePoints);
		}
		statisticsPointsAdapter.setPoints(pointsList);
		statisticsPointsAdapter.setIncrementPoints(incrementPointsList);
	}

	private void arrangeCards(GameState gameState)
	{
		List<Card> myCards = new ArrayList<>();
		for (String cardId : gameState.playersRotated.get(0).cards)
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

			final LinearLayout parentView = i < cardsUp ? gameBinding.myCards1 : gameBinding.myCards0;
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
			GameState gameState = gameViewModel.getGameState().getValue();
			if (gameState == null || !gameState.playersRotated.get(0).turn)
				return;

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
			gameViewModel.doAction(Action.play((Card)v.getTag()));
		});
	}

	private void removeAllMyCardsView()
	{
		cardToViewMapping.clear();

		for (ViewGroup cardsView : new ViewGroup[]{gameBinding.myCards0, gameBinding.myCards1})
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
		messagesBinding.ultimoView.setVisibility(visible ? View.VISIBLE : View.GONE);
		messagesBinding.messagesView.setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	private void showCenterView(int item)
	{
		pendingCenterView = item;
		gameBinding.centerSpace.setCurrentItem(item);
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

	private void showPlayerMessageView(int dir, String msg, int backgroundRes)
	{
		final TextView view = playerMessageViews[dir];
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
}

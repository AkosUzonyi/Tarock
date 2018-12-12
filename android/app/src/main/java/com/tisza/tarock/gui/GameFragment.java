package com.tisza.tarock.gui;

import android.animation.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v4.view.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.view.animation.Animation.*;
import android.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.zebisound.*;

import java.util.*;

public class GameFragment extends MainActivityFragment implements EventHandler
{
	public static final String LOG_TAG = "Tarokk";

	public static final float PLAYED_CARD_DISTANCE = 0.65F;
	public static final int PLAY_DURATION = 50;
	public static final int TAKE_DURATION = 400;
	private static final int DELAY = BuildConfig.DEBUG ? 500 : 1500;
	private static final int CARDS_PER_ROW = 6;

	private static final int MESSAGES_VIEW_INDEX = 0;
	private static final int GAMEPLAY_VIEW_INDEX = 1;
	private static final int STATISTICS_VIEW_INDEX = 2;

	private static final float cardImageRatio = 1.66F;
	private int cardWidth, cardHeight;

	private ZebiSounds zebiSounds;

	private LayoutInflater layoutInflater;

	private TextView[] playerNameViews;
	private TextView[] playerMessageViews;
	private View[] skartViews;
	private LinearLayout myCardsView0;
	private LinearLayout myCardsView1;
	private View cardsBackgroundColorView;
	private View cardsHighlightView;
	private ViewPager centerSpace;
	private Button okButton;
	private Button throwButton;

	private View messagesFrame;

	private View messagesView;
	private ScrollView messagesScrollView;
	private TextView messagesTextView;
	private LinearLayout availabeActionsView;

	private View ultimoView;
	private Button ultimoBackButton;
	private UltimoViewManager ultimoViewManager;
	private Button announceButton;

	private RelativeLayout gameplayView;
	private PlayedCardView[] playedCardViews;

	private View statisticsView;
	private TextView statisticsGamepointsSelf;
	private TextView statisticsGamepointsOpponent;
	private TextView statisticsPointMultiplierView;
	private LinearLayout statisticsSelfEntriesView;
	private LinearLayout statisticsOpponentEntriesView;
	private TextView statisticsSumPointsView;
	private TextView[] statisticsPointsNameViews = new TextView[4];
	private TextView[] statisticsPointsValueViews = new TextView[4];

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ResourceMappings.init(getActivity());

		layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		zebiSounds = new ZebiSounds(getActivity());
	}

	private ActionSender getActionSender()
	{
		return getMainActivity().getActionSender();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View contentView = inflater.inflate(R.layout.game, container, false);

		cardWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth() / CARDS_PER_ROW;
		cardHeight = (int)(cardWidth * cardImageRatio);

		centerSpace = contentView.findViewById(R.id.center_space);

		playerNameViews = new TextView[]
		{
				null,
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

		myCardsView0 = (LinearLayout)contentView.findViewById(R.id.my_cards_0);
		myCardsView1 = (LinearLayout)contentView.findViewById(R.id.my_cards_1);
		cardsBackgroundColorView = contentView.findViewById(R.id.cards_background_color);
		cardsHighlightView = contentView.findViewById(R.id.cards_highlight);

		okButton = (Button)contentView.findViewById(R.id.ok_button);
		throwButton = (Button)contentView.findViewById(R.id.throw_button);
		throwButton.setOnClickListener(v -> getActionSender().throwCards());

		messagesFrame = layoutInflater.inflate(R.layout.messages, centerSpace, false);

		messagesView = messagesFrame.findViewById(R.id.messages_view);
		messagesScrollView = (ScrollView)messagesFrame.findViewById(R.id.messages_scroll);
		messagesTextView = (TextView)messagesFrame.findViewById(R.id.messages_text_view);
		availabeActionsView = (LinearLayout)messagesFrame.findViewById(R.id.available_actions);

		ultimoView = messagesFrame.findViewById(R.id.ultimo_view);
		ultimoBackButton = (Button)messagesFrame.findViewById(R.id.ultimo_back_buton);
		announceButton = (Button)messagesFrame.findViewById(R.id.ultimo_announce_button);
		ultimoViewManager = new UltimoViewManager(getActivity(), layoutInflater, (LinearLayout)messagesFrame.findViewById(R.id.ultimo_spinner_list));
		ultimoBackButton.setOnClickListener(v -> setUltimoViewVisible(false));

		announceButton.setOnClickListener(v ->
		{
			Announcement announcement = ultimoViewManager.getCurrentSelectedAnnouncement();

			if (announcement == null)
				throw new RuntimeException();

			getActionSender().announce(announcement);
		});

		gameplayView = (RelativeLayout)layoutInflater.inflate(R.layout.gameplay, centerSpace, false);
		playedCardViews = new PlayedCardView[4];
		for (int i = 0; i < 4; i++)
		{
			playedCardViews[i] = new PlayedCardView(getActivity(), cardWidth, cardHeight, i);
			gameplayView.addView(playedCardViews[i]);
		}

		statisticsView = layoutInflater.inflate(R.layout.statistics, centerSpace, false);
		statisticsGamepointsSelf = (TextView)statisticsView.findViewById(R.id.statistics_gamepoints_self);
		statisticsGamepointsOpponent = (TextView)statisticsView.findViewById(R.id.statistics_gamepoints_opponent);
		statisticsPointMultiplierView = (TextView)statisticsView.findViewById(R.id.statistics_point_multiplier);
		statisticsSelfEntriesView = (LinearLayout)statisticsView.findViewById(R.id.statistics_self_entries_list);
		statisticsOpponentEntriesView = (LinearLayout)statisticsView.findViewById(R.id.statistics_opponent_entries_list);
		statisticsSumPointsView = (TextView)statisticsView.findViewById(R.id.statistics_sum_points);
		statisticsPointsNameViews[0] = (TextView)statisticsView.findViewById(R.id.statistics_points_name_0);
		statisticsPointsNameViews[1] = (TextView)statisticsView.findViewById(R.id.statistics_points_name_1);
		statisticsPointsNameViews[2] = (TextView)statisticsView.findViewById(R.id.statistics_points_name_2);
		statisticsPointsNameViews[3] = (TextView)statisticsView.findViewById(R.id.statistics_points_name_3);
		statisticsPointsValueViews[0] = (TextView)statisticsView.findViewById(R.id.statistics_points_value_0);
		statisticsPointsValueViews[1] = (TextView)statisticsView.findViewById(R.id.statistics_points_value_1);
		statisticsPointsValueViews[2] = (TextView)statisticsView.findViewById(R.id.statistics_points_value_2);
		statisticsPointsValueViews[3] = (TextView)statisticsView.findViewById(R.id.statistics_points_value_3);

		View[] centerViews = {messagesFrame, gameplayView, statisticsView};
		int[] centerViewTitles = {R.string.pager_announcing, R.string.pager_gameplay, R.string.pager_statistics};
		centerSpace.setAdapter(new CenterViewPagerAdapter(getActivity(), centerViews, centerViewTitles));

		for (ZebiSound zebiSound : zebiSounds.getZebiSounds())
		{
			getMainActivity().addEventHandler(zebiSound);
		}
		getMainActivity().addEventHandler(this);
		getMainActivity().getConnection().sendMessage(MainProto.Message.newBuilder().setJoinGame(MainProto.JoinGame.newBuilder()
				.setGameId(getArguments().getInt("gameID"))
				.build())
				.build());

		return contentView;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		for (ZebiSound zebiSound : zebiSounds.getZebiSounds())
		{
			getMainActivity().removeEventHandler(zebiSound);
		}
		getMainActivity().removeEventHandler(this);
		getMainActivity().getConnection().sendMessage(MainProto.Message.newBuilder().setJoinGame(MainProto.JoinGame.newBuilder()
				.build())
				.build());
	}

	private void resetGameViews()
	{
		showCenterView(MESSAGES_VIEW_INDEX);
		okButton.setVisibility(View.GONE);
		throwButton.setVisibility(View.GONE);
		messagesTextView.setText("");
		availabeActionsView.removeAllViews();
		setUltimoViewVisible(false);

		cardsHighlightView.setVisibility(View.GONE);
		cardsBackgroundColorView.setBackgroundDrawable(null);

		statisticsGamepointsSelf.setText(null);
		statisticsGamepointsOpponent.setText(null);
		statisticsPointMultiplierView.setVisibility(View.GONE);
		statisticsSelfEntriesView.removeAllViews();
		statisticsOpponentEntriesView.removeAllViews();
		statisticsSumPointsView.setText(null);

		for (PlayedCardView playedCardView : playedCardViews)
		{
			playedCardView.clear();
		}

		for (TextView nameView : playerNameViews)
		{
			if (nameView != null)
				nameView.setTextColor(getResources().getColor(R.color.unknown_team));
		}

		for (View skartView : skartViews)
		{
			skartView.setVisibility(View.GONE);
		}
	}

	private List<String> playerNames;
	protected PlayerCards myCards;
	private int myID = -1;
	private GameType gameType;
	private int beginnerPlayer;

	private Map<Card, View> cardToViewMapping = new HashMap<>();
	
	private PhaseEnum gamePhase;
	
	private String messages;
	
	private List<Card> cardsToSkart = new ArrayList<>();
	private boolean skarting = false;

	private boolean waitingForTakeAnimation;
	
	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		resetGameViews();

		this.myID = myID;
		this.playerNames = playerNames;
		this.gameType = gameType;
		this.beginnerPlayer = beginnerPlayer;

		zebiSounds.setEnabled(BuildConfig.DEBUG && gameType == GameType.ZEBI);

		for (int i = 0; i < 4; i++)
		{
			int pos = getPositionFromPlayerID(i);
			if (pos != 0)
			{
				playerNameViews[pos].setText(playerNames.get(i));
			}
		}
		messages = "";
	}
	
	@Override
	public void cardsChanged(List<Card> cards)
	{
		myCards = new PlayerCards(cards);
		arrangeCards();
	}

	@Override
	public void phaseChanged(PhaseEnum phase)
	{
		gamePhase = phase;

		if (phase == PhaseEnum.BIDDING)
		{
			showCenterView(MESSAGES_VIEW_INDEX);

			if (myCards.canBeThrown())
				throwButton.setVisibility(View.VISIBLE);
		}
		else if (phase == PhaseEnum.CHANGING)
		{
			showCenterView(MESSAGES_VIEW_INDEX);

			higlightAllName();
		}
		else if (phase == PhaseEnum.CALLING)
		{
			showCenterView(MESSAGES_VIEW_INDEX);
		}
		else if (phase == PhaseEnum.ANNOUNCING)
		{
			showCenterView(MESSAGES_VIEW_INDEX);
		}
		else if (phase == PhaseEnum.GAMEPLAY)
		{
			showCenterViewDelayed(GAMEPLAY_VIEW_INDEX);
		}
		else if (phase == PhaseEnum.END)
		{
			showCenterViewDelayed(STATISTICS_VIEW_INDEX);
		}
		else if (phase == PhaseEnum.INTERRUPTED)
		{
			availabeActionsView.removeAllViews();
			displayMessage(R.string.press_ok);
		}

		skartViews[getPositionFromPlayerID(beginnerPlayer)].setVisibility(phase.isAfter(PhaseEnum.CHANGING) ? View.VISIBLE : View.GONE);
	}

	@Override
	public void cardsThrown(int player, PlayerCards thrownCards)
	{
		displayPlayerActionMessage(R.string.message_generic, player, getString(R.string.cards_thrown));
	}
	
	@Override
	public void availableBids(List<Integer> bids)
	{
		availabeActionsView.removeAllViews();
		for (final int bid : bids)
		{
			Button bidButton = (Button)layoutInflater.inflate(R.layout.button, availabeActionsView, false);
			bidButton.setText(ResourceMappings.bidToName.get(bid));
			bidButton.setOnClickListener(v -> getActionSender().bid(bid));
			availabeActionsView.addView(bidButton);
		}
	}
	
	@Override
	public void bid(int player, int bid)
	{
		if (player == myID)
			availabeActionsView.removeAllViews();

		String msg = ResourceMappings.bidToName.get(bid);
		displayPlayerActionMessage(R.string.message_bid, player, msg);
	}

	@Override
	public void changeDone(int player)
	{
		setHiglighted(player, false);
		if (player == myID)
		{
			okButton.setVisibility(View.GONE);
			throwButton.setVisibility(View.GONE);
			cardsToSkart.clear();
			skarting = false;
		}
	}
	
	@Override
	public void skartTarock(int[] counts)
	{
		for (int p = 0; p < 4; p++)
		{
			int count = counts[p];
			if (count > 0)
			{
				String msg = getResources().getQuantityString(R.plurals.message_skart_tarock, count, count);
				displayPlayerActionMessage(R.string.message_generic, p, msg);
			}
		}
	}

	@Override
	public void availableCalls(List<Card> calls)
	{
		availabeActionsView.removeAllViews();
		for (final Card card : calls)
		{
			Button callButton = (Button)layoutInflater.inflate(R.layout.button, availabeActionsView, false);
			callButton.setText(ResourceMappings.uppercaseCardName(card));
			callButton.setOnClickListener(v -> getActionSender().call(card));
			availabeActionsView.addView(callButton);
		}
	}
	
	@Override
	public void call(int player, Card card)
	{
		if (player == myID)
			availabeActionsView.removeAllViews();

		displayPlayerActionMessage(R.string.message_call, player, ResourceMappings.uppercaseCardName(card));
	}
	
	@Override
	public void availableAnnouncements(List<Announcement> announcements)
	{
		availabeActionsView.removeAllViews();

		Collections.sort(announcements);

		if (gameType != GameType.PASKIEVICS)
			ultimoViewManager.takeAnnouncements(announcements);

		if (ultimoViewManager.hasAnyUltimo())
		{
			Button ultimoButton = (Button)layoutInflater.inflate(R.layout.button, availabeActionsView, false);
			ultimoButton.setText(ResourceMappings.roundNames[8]);
			ultimoButton.setOnClickListener(v -> setUltimoViewVisible(true));
			availabeActionsView.addView(ultimoButton);
		}

		for (Announcement announcement : announcements)
		{
			Button announceButton = (Button)layoutInflater.inflate(R.layout.button, availabeActionsView, false);
			announceButton.setText(announcement.getDisplayText());
			announceButton.setOnClickListener(v -> getActionSender().announce(announcement));
			availabeActionsView.addView(announceButton);
		}
		
		okButton.setVisibility(View.VISIBLE);
		okButton.setOnClickListener(v -> getActionSender().announcePassz());
	}

	@Override
	public void announce(int player, Announcement announcement)
	{
		if (announcement.getName().equals("jatek") && announcement.getContraLevel() == 0)
			return;

		if (player == myID)
		{
			okButton.setVisibility(View.GONE);
			availabeActionsView.removeAllViews();
		}

		setUltimoViewVisible(false);

		String msg = announcement.getDisplayText();
		displayPlayerActionMessage(R.string.message_announce, player, msg);
	}

	@Override
	public void announcePassz(int player)
	{
		if (player == myID)
		{
			okButton.setVisibility(View.GONE);
			availabeActionsView.removeAllViews();
		}
	}

	@Override
	public void cardPlayed(int player, Card card)
	{
		int pos = getPositionFromPlayerID(player);
		final PlayedCardView playedCardView = playedCardViews[pos];
		
		playedCardView.setCard(card);
		playedCardView.bringToFront();
		playedCardView.animatePlay();
		
		if (player == myID)
		{
			myCards.removeCard(card);

			View myCardView = cardToViewMapping.remove(card);
			if (myCardView != null)
			{
				int originalWidth = myCardView.getWidth();
				int originalHeight = myCardView.getHeight();

				ValueAnimator shrinkAnimator = ValueAnimator.ofInt(originalWidth, 0);
				shrinkAnimator.addUpdateListener(animation ->
				{
					myCardView.getLayoutParams().width = (Integer)animation.getAnimatedValue();
					//myCardView.getLayoutParams().height = originalHeight;
					myCardView.requestLayout();
				});
				shrinkAnimator.setDuration(PLAY_DURATION);
				shrinkAnimator.start();
				shrinkAnimator.addListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationEnd(Animator animation)
					{
						myCardsView0.removeView(myCardView);
						myCardsView1.removeView(myCardView);

						if (myCards.getCards().size() == CARDS_PER_ROW)
						{
							arrangeCards();
						}
					}
				});
			}
		}
	}
	
	@Override
	public void cardsTaken(final int winnerPlayer)
	{
		waitingForTakeAnimation = true;
		new Handler().postDelayed(() ->
		{
			for (PlayedCardView playedCardView : playedCardViews)
			{
				playedCardView.animateTake(getPositionFromPlayerID(winnerPlayer));
				waitingForTakeAnimation = false;
			}
		}, DELAY);
	}
	
	@Override
	public void turn(int player)
	{
		if (gamePhase == PhaseEnum.CHANGING)
		{
			skarting = true;

			okButton.setVisibility(View.VISIBLE);
			okButton.setOnClickListener(v -> getActionSender().change(cardsToSkart));

			if (myCards.canBeThrown())
				throwButton.setVisibility(View.VISIBLE);
		}
		else
		{
			highlightName(player);
		}
	}

	@Override
	public void playerTeamInfo(int player, boolean callerTeam)
	{
		int pos = getPositionFromPlayerID(player);
		int color = getResources().getColor(callerTeam ? R.color.caller_team : R.color.opponent_team);

		if (pos == 0)
		{
			cardsBackgroundColorView.setBackgroundColor(color);
		}
		else
		{
			playerNameViews[pos].setTextColor(color);
		}
	}

	@Override
	public void statistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, List<Integer> points, int pointMultiplier)
	{
		statisticsGamepointsSelf.setText(String.valueOf(selfGamePoints));
		statisticsGamepointsOpponent.setText(String.valueOf(opponentGamePoints));

		statisticsPointMultiplierView.setVisibility(pointMultiplier == 1 ? View.GONE : View.VISIBLE);
		statisticsPointMultiplierView.setText(getString(R.string.statictics_point_multiplier, pointMultiplier));

		addStatisticEntries(statisticsSelfEntriesView, selfEntries);
		addStatisticEntries(statisticsOpponentEntriesView, opponentEntries);
		
		statisticsSumPointsView.setText(String.valueOf(sumPoints));
		statisticsSumPointsView.setTextColor(sumPoints >= 0 ? Color.BLACK : Color.RED);

		if (!points.isEmpty())
		{
			for (int i = 0; i < 4; i++)
			{
				TextView nameView = statisticsPointsNameViews[i];
				nameView.setText(playerNames.get(i));
				nameView.setGravity(Gravity.CENTER);

				TextView pointsView = statisticsPointsValueViews[i];
				pointsView.setText(String.valueOf(points.get(i)));
				pointsView.setGravity(Gravity.CENTER);
			}
		}
	}

	private void addStatisticEntries(ViewGroup viewToAppend, List<AnnouncementStaticticsEntry> entries)
	{
		viewToAppend.removeAllViews();

		for (AnnouncementStaticticsEntry entry : entries)
		{
			View entryView = layoutInflater.inflate(R.layout.statistics_entry, viewToAppend, false);
			TextView nameView = (TextView)entryView.findViewById(R.id.statistics_announcement_name);
			TextView pointsView = (TextView)entryView.findViewById(R.id.statistics_sum_points);
			
			nameView.setText(entry.getAnnouncement().getDisplayText());
			pointsView.setText(String.valueOf(entry.getPoints()));
			pointsView.setVisibility(entry.getPoints() == 0 ? View.GONE : View.VISIBLE);
			pointsView.setTextColor(entry.getPoints() >= 0 ? Color.BLACK : Color.RED);
			
			viewToAppend.addView(entryView);
		}
	}

	@Override
	public void pendingNewGame()
	{
		okButton.setVisibility(View.VISIBLE);
		okButton.setOnClickListener(v -> getActionSender().readyForNewGame());

		higlightAllName();
	}

	@Override
	public void readyForNewGame(int player)
	{
		if (player == myID)
			okButton.setVisibility(View.GONE);

		setHiglighted(player, false);
	}

	@Override
	public void deleteGame()
	{
		getActivity().getFragmentManager().popBackStack();
	}

	@Override
	public void wrongAction()
	{
		displayMessage("error");
	}

	private void arrangeCards()
	{
		removeAllMyCardsView();
		
		myCards.sort();
		
		int cardCount = myCards.getCards().size();
		int cardsUp = cardCount <= CARDS_PER_ROW ? 0 : cardCount / 2;
		for (int i = 0; i < cardCount; i++)
		{
			final Card card = myCards.getCards().get(i);
			
			ImageView cardView = new ImageView(getActivity());
			cardView.setAdjustViewBounds(true);
			int padding = (int)(cardWidth * 0.1F / 2);
			cardView.setPadding(padding, padding, padding, padding);
			cardView.setLayoutParams(new LinearLayout.LayoutParams(cardWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
			if (card != null)
				cardView.setImageResource(PlayedCardView.getBitmapResForCard(card));
			
			final LinearLayout parentView = i < cardsUp ? myCardsView1 : myCardsView0;
			parentView.addView(cardView);
			cardToViewMapping.put(card, cardView);
			
			cardView.setOnClickListener(new OnClickListener()
			{
				private boolean selectedForSkart = false;
				@Override
				public void onClick(View v)
				{
					if (skarting)
					{
						if (!selectedForSkart)
						{
							cardsToSkart.add(card);
							selectedForSkart = true;
							Animation a = new TranslateAnimation(0, 0, 0, -v.getHeight() / 5);
							a.setDuration(300);
							a.setFillAfter(true);
							v.startAnimation(a);
						}
						else
						{
							cardsToSkart.remove(card);
							selectedForSkart = false;
							Animation a = new TranslateAnimation(0, 0, -v.getHeight() / 5, 0);
							a.setDuration(300);
							a.setFillAfter(true);
							v.startAnimation(a);
						}
					}
					else if (gamePhase == PhaseEnum.GAMEPLAY && !waitingForTakeAnimation && !playedCardViews[0].isAnimating())
					{
						getActionSender().playCard(card);
					}
				}
			});
		}
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
		messages += msg;
		messagesTextView.setText(messages);
		messagesScrollView.fullScroll(View.FOCUS_DOWN);
	}

	private void displayPlayerActionMessage(int actionRes, int player, String msg)
	{
		displayMessage(actionRes, playerNames.get(player), msg);
		showPlayerMessageView(player, msg);
	}
	
	private void showPlayerMessageView(int player, String msg)
	{
		final TextView view = playerMessageViews[getPositionFromPlayerID(player)];
		view.setText(msg);
		view.setVisibility(View.VISIBLE);
		Animation fadeAnimation = new AlphaAnimation(1, 0);
		fadeAnimation.setDuration(500);
		fadeAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis() + 2000);
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
		else
		{
			if (val)
			{
				playerNameViews[pos].setBackgroundResource(R.drawable.name_highlight);
			}
			else
			{
				playerNameViews[pos].setBackgroundColor(Color.TRANSPARENT);
			}
		}
	}
	
	private int getPositionFromPlayerID(int id)
	{
		return (id - myID + 4) % 4;
	}
}

package com.tisza.tarock.gui;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.view.animation.Animation.*;
import android.widget.*;
import com.tisza.tarock.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;

import java.util.*;

public class GameFragment extends MainActivityFragment implements EventHandler
{
	public static final String LOG_TAG = "Tarokk";

	public static final float PLAYED_CARD_DISTANCE = 0.65F;
	public static final int PLAY_DURATION = 50;
	public static final int TAKE_DURATION = 400;
	private static final int DELAY = 600;
	private static final int CARDS_PER_ROW = 6;

	private static final float cardImageRatio = 1.66F;
	private int cardWidth, cardHeight;
	
	private LayoutInflater layoutInflater;

	private TextView[] playerNameViews;
	private TextView[] playerMessageViews;
	private LinearLayout myCardsView;
	private LinearLayout myCardsView0;
	private LinearLayout myCardsView1;
	private FrameLayout centerSpace;
	private Button okButton;
	private Button throwButton;
	
	private View messagesView;
	private View ultimoView;
	private ScrollView messagesScrollView;
	private TextView messagesTextView;
	private LinearLayout availabeActionsView;
	
	private Button ultimoBackButton;
	private UltimoViewManager ultimoViewManager;
	private Button announceButton;
	
	private RelativeLayout playedCardsView;
	private PlayedCardView[] playedCardViews;
	
	private View statisticsView;
	private TextView statisticsGamepointsSelf;
	private TextView statisticsGamepointsOpponent;
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
	}

	private ActionSender getActionSender()
	{
		return mainActivity.getActionSender();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View contentView = inflater.inflate(R.layout.game, container, false);

		cardWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth() / CARDS_PER_ROW;
		cardHeight = (int)(cardWidth * cardImageRatio);

		centerSpace = (FrameLayout)contentView.findViewById(R.id.center_space);
		
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
		
		myCardsView = (LinearLayout)contentView.findViewById(R.id.my_cards);
		myCardsView0 = (LinearLayout)contentView.findViewById(R.id.my_cards_0);
		myCardsView1 = (LinearLayout)contentView.findViewById(R.id.my_cards_1);
		
		okButton = (Button)contentView.findViewById(R.id.ok_button);
		throwButton = (Button)contentView.findViewById(R.id.throw_button);
		throwButton.setOnClickListener(v ->
		{
			getActionSender().throwCards();
		});
		
		layoutInflater.inflate(R.layout.messages, centerSpace);
		messagesView = contentView.findViewById(R.id.messages_view);
		messagesScrollView = (ScrollView)contentView.findViewById(R.id.messages_scroll);
		messagesTextView = (TextView)contentView.findViewById(R.id.messages_text_view);
		availabeActionsView = (LinearLayout)contentView.findViewById(R.id.available_actions);

		layoutInflater.inflate(R.layout.ultimo, centerSpace);
		ultimoView = contentView.findViewById(R.id.ultimo_view);
		ultimoBackButton = (Button)contentView.findViewById(R.id.ultimo_back_buton);
		announceButton = (Button)contentView.findViewById(R.id.ultimo_announce_button);
		ultimoViewManager = new UltimoViewManager(getActivity(), layoutInflater, (LinearLayout)contentView.findViewById(R.id.ultimo_spinner_list));
		ultimoBackButton.setOnClickListener(v -> showCenterView(messagesView));

		announceButton.setOnClickListener(v ->
		{
			Announcement announcement = ultimoViewManager.getCurrentSelectedAnnouncement();

			if (announcement == null)
				throw new RuntimeException();

			getActionSender().announce(announcement);
		});
		
		playedCardsView = new RelativeLayout(getActivity());
		playedCardsView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		playedCardViews = new PlayedCardView[4];
		for (int i = 0; i < 4; i++)
		{
			playedCardViews[i] = new PlayedCardView(getActivity(), cardWidth, cardHeight, i);
			playedCardsView.addView(playedCardViews[i]);
		}
		centerSpace.addView(playedCardsView);
		
		layoutInflater.inflate(R.layout.statistics, centerSpace);
		statisticsView = contentView.findViewById(R.id.statistics_view);
		statisticsGamepointsSelf = (TextView)contentView.findViewById(R.id.statistics_gamepoints_self);
		statisticsGamepointsOpponent = (TextView)contentView.findViewById(R.id.statistics_gamepoints_opponent);
		statisticsSelfEntriesView = (LinearLayout)contentView.findViewById(R.id.statistics_self_entries_list);
		statisticsOpponentEntriesView = (LinearLayout)contentView.findViewById(R.id.statistics_opponent_entries_list);
		statisticsSumPointsView = (TextView)contentView.findViewById(R.id.statistics_sum_points);
		statisticsPointsNameViews[0] = (TextView)contentView.findViewById(R.id.statistics_points_name_0);
		statisticsPointsNameViews[1] = (TextView)contentView.findViewById(R.id.statistics_points_name_1);
		statisticsPointsNameViews[2] = (TextView)contentView.findViewById(R.id.statistics_points_name_2);
		statisticsPointsNameViews[3] = (TextView)contentView.findViewById(R.id.statistics_points_name_3);
		statisticsPointsValueViews[0] = (TextView)contentView.findViewById(R.id.statistics_points_value_0);
		statisticsPointsValueViews[1] = (TextView)contentView.findViewById(R.id.statistics_points_value_1);
		statisticsPointsValueViews[2] = (TextView)contentView.findViewById(R.id.statistics_points_value_2);
		statisticsPointsValueViews[3] = (TextView)contentView.findViewById(R.id.statistics_points_value_3);

		mainActivity.setEventHandler(this);
		mainActivity.getConnection().sendMessage(MainProto.Message.newBuilder().setJoinGame(MainProto.JoinGame.newBuilder()
				.setGameId(getArguments().getInt("gameID"))
				.build())
				.build());

		return contentView;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mainActivity.setEventHandler(null);
		mainActivity.getConnection().sendMessage(MainProto.Message.newBuilder().setJoinGame(MainProto.JoinGame.newBuilder()
				.build())
				.build());
	}

	private void resetGameViews()
	{
		showCenterView(messagesView);
		okButton.setVisibility(View.GONE);
		throwButton.setVisibility(View.GONE);
		messagesTextView.setText("");

	}

	private List<String> playerNames;
	private PlayerCards myCards;
	private int myID = -1;
	private Map<Card, View> cardToViewMapping = new HashMap<>();
	
	private PhaseEnum gamePhase;
	
	private String messages;
	
	private List<Card> cardsToSkart = new ArrayList<>();
	private boolean skarting = false;

	private boolean waitingForTakeAnimation;
	
	@Override
	public void startGame(int myID, List<String> playerNames)
	{
		resetGameViews();

		this.myID = myID;
		this.playerNames = playerNames;
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

		throwButton.setVisibility(View.GONE);

		if (gamePhase == PhaseEnum.BIDDING)
		{
			if (myCards.canBeThrown(false))
			{
				throwButton.setVisibility(View.VISIBLE);
			}
		}
		else if (gamePhase == PhaseEnum.CHANGING)
		{
			if (myCards.canBeThrown(true))
			{
				throwButton.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void phaseChanged(PhaseEnum phase)
	{
		gamePhase = phase;

		if (phase == PhaseEnum.BIDDING)
		{
			showCenterView(messagesView);
		}
		else if (phase == PhaseEnum.CHANGING)
		{
			showCenterView(messagesView);

			higlightAllName();
		}
		else if (phase == PhaseEnum.CALLING)
		{
			showCenterView(messagesView);
		}
		else if (phase == PhaseEnum.ANNOUNCING)
		{
			showCenterView(messagesView);
		}
		else if (phase == PhaseEnum.GAMEPLAY)
		{
			showCenterViewDelayed(playedCardsView);
		}
		else if (phase == PhaseEnum.END)
		{
			showCenterViewDelayed(statisticsView);
		}
	}

	@Override
	public void cardsThrown(int player, PlayerCards thrownCards)
	{
		String msg = getResources().getString(R.string.message_throw_cards);
		displayPlayerActionMessage(player, msg);
		displayMessage(getResources().getString(R.string.press_ok) + "\n");
	}
	
	@Override
	public void availableBids(List<Integer> bids)
	{
		availabeActionsView.removeAllViews();
		for (final int bid : bids)
		{
			Button bidButton = (Button)layoutInflater.inflate(R.layout.button, availabeActionsView, false);
			bidButton.setText(ResourceMappings.bidToName.get(bid));
			
			bidButton.setOnClickListener(v ->
			{
				availabeActionsView.removeAllViews();
				getActionSender().bid(bid);
			});
			availabeActionsView.addView(bidButton);
		}
	}
	
	@Override
	public void bid(int player, int bid)
	{
		String msg = ResourceMappings.bidToName.get(bid);
		displayPlayerActionMessage(player, R.string.message_bid, msg);
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
				String msg = "";
				msg += count;
				msg += " ";
				msg += getResources().getString(R.string.message_skart_tarock);
				displayPlayerActionMessage(p, msg);
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
			callButton.setOnClickListener(v ->
			{
				availabeActionsView.removeAllViews();
				getActionSender().call(card);
			});
			availabeActionsView.addView(callButton);
		}
	}
	
	@Override
	public void call(int player, Card card)
	{
		displayPlayerActionMessage(player, R.string.message_call, ResourceMappings.uppercaseCardName(card));
	}
	
	@Override
	public void availableAnnouncements(List<Announcement> announcements)
	{
		availabeActionsView.removeAllViews();

		Collections.sort(announcements);

		ultimoViewManager.takeAnnouncements(announcements);
		if (ultimoViewManager.hasAnyUltimo())
		{
			Button ultimoButton = (Button)layoutInflater.inflate(R.layout.button, availabeActionsView, false);
			ultimoButton.setText(ResourceMappings.roundNames[8]);
			ultimoButton.setOnClickListener(v -> showCenterView(ultimoView));
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
		okButton.setOnClickListener(v ->
		{
			okButton.setVisibility(View.GONE);
			showCenterView(messagesView);
			availabeActionsView.removeAllViews();
			getActionSender().announcePassz();
		});
	}

	@Override
	public void announce(int player, Announcement announcement)
	{
		if (announcement.getName().equals("jatek") && announcement.getContraLevel() == 0)
			return;

		showCenterView(messagesView);
		
		String msg = announcement.getDisplayText();
		displayPlayerActionMessage(player, R.string.message_announce, msg);
	}

	@Override
	public void passz(int player)
	{
		showCenterView(messagesView);
		
		String msg = getResources().getString(R.string.passz);
		displayPlayerActionMessage(player, R.string.message_announce, msg);
	}

	@Override
	public void cardPlayed(int player, Card card)
	{
		int pos = getPositionFromPlayerID(player);
		final PlayedCardView playedCardView = playedCardViews[pos];
		
		playedCardView.addCard(card);
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
		}
		else
		{
			highlightName(player);
		}
	}
	
	@Override
	public void statistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, List<Integer> points)
	{
		statisticsGamepointsSelf.setText(selfGamePoints + "");
		statisticsGamepointsOpponent.setText(opponentGamePoints + "");
		
		statisticsSelfEntriesView.removeAllViews();
		statisticsOpponentEntriesView.removeAllViews();
		
		appendHeaderToStatistics(statisticsSelfEntriesView, R.string.statictics_self, R.string.statictics_points);
		appendEntriesToStatistics(statisticsSelfEntriesView, selfEntries);
		
		appendHeaderToStatistics(statisticsOpponentEntriesView, R.string.statictics_opponent, R.string.statictics_points);
		appendEntriesToStatistics(statisticsOpponentEntriesView, opponentEntries);
		
		statisticsSumPointsView.setText(sumPoints + "");
		statisticsSumPointsView.setTextColor(sumPoints >= 0 ? Color.BLACK : Color.RED);
		
		for (int i = 0; i < 4; i++)
		{
			TextView nameView = statisticsPointsNameViews[i];
			nameView.setText(playerNames.get(i));
			nameView.setGravity(Gravity.CENTER);
			
			TextView pointsView = statisticsPointsValueViews[i];
			pointsView.setText(points.get(i) + "");
			pointsView.setGravity(Gravity.CENTER);
		}
	}
	
	private void appendHeaderToStatistics(ViewGroup viewToAppend, int res0, int res1)
	{
		View entryView = layoutInflater.inflate(R.layout.statistics_header, viewToAppend, false);
		TextView nameView = (TextView)entryView.findViewById(R.id.statistics_announcement_name);
		TextView pointsView = (TextView)entryView.findViewById(R.id.statistics_sum_points);
		
		nameView.setText(res0);
		pointsView.setText(res1);
		
		viewToAppend.addView(entryView);
	}
	
	private void appendEntriesToStatistics(ViewGroup viewToAppend, List<AnnouncementStaticticsEntry> entries)
	{
		for (AnnouncementStaticticsEntry entry : entries)
		{
			View entryView = layoutInflater.inflate(R.layout.statistics_entry, viewToAppend, false);
			TextView nameView = (TextView)entryView.findViewById(R.id.statistics_announcement_name);
			TextView pointsView = (TextView)entryView.findViewById(R.id.statistics_sum_points);
			
			nameView.setText(entry.getAnnouncement().getDisplayText());
			pointsView.setText(entry.getPoints() + "");
			pointsView.setTextColor(entry.getPoints() >= 0 ? Color.BLACK : Color.RED);
			
			viewToAppend.addView(entryView);
		}
	}

	@Override
	public void pendingNewGame()
	{
		okButton.setVisibility(View.VISIBLE);
		okButton.setOnClickListener(v ->
		{
			okButton.setVisibility(View.GONE);
			getActionSender().readyForNewGame();
		});
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
	
	private void showCenterView(final View v)
	{
		pendingCenterView = v;
		int count = centerSpace.getChildCount();
		for (int i = 0; i < count; i++)
		{
			View child = centerSpace.getChildAt(i);
			child.setVisibility(child == v ? View.VISIBLE : View.GONE);
		}
	}
	
	private View pendingCenterView;
	private void showCenterViewDelayed(final View v)
	{
		pendingCenterView = v;
		new Handler().postDelayed(() ->
		{
			if (pendingCenterView != v)
				return;

			showCenterView(pendingCenterView);
		}, DELAY);
	}
	
	private void displayMessage(String msg)
	{
		messages += msg;
		messagesTextView.setText(messages);
		messagesScrollView.fullScroll(View.FOCUS_DOWN);
	}
	
	private void displayPlayerActionMessage(int player, String msg)
	{
		displayMessage(playerNames.get(player) + " " + msg + "\n");
		showPlayerMessageView(player, msg);
	}
	
	private void displayPlayerActionMessage(int player, int actionRes, String msg)
	{
		String action = getResources().getString(actionRes);
		displayMessage(playerNames.get(player) + " " + action + ": " + msg + "\n");
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
			myCardsView.setBackgroundResource(val ? R.drawable.cards_highlight : R.drawable.cards);
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

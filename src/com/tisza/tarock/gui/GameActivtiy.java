package com.tisza.tarock.gui;

import java.io.*;
import java.net.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.*;
import android.widget.*;

import com.tisza.tarock.*;
import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.net.packet.PacketAnnouncementStatistics.Entry;
import com.tisza.tarock.net.packet.PacketPhase.Phase;

public class GameActivtiy extends Activity implements PacketHandler
{
	public static final String LOG_TAG = "Tarokk";
	
	private static final int DELAY = 1600;
	private static final int CARDS_PER_ROW = 6;
	private int cardWidth;
	
	private LayoutInflater layoutInflater;
	
	private TextView[] playerNameViews;
	private LinearLayout myCardsView;
	private LinearLayout myCardsView0;
	private LinearLayout myCardsView1;
	private FrameLayout centerSpace;
	private Button okButton;
	private Button throwButton;
	
	private View messagesView;
	private View messagesDefaultView;
	private View messagesUltimoView;
	private ScrollView messagesScrollView;
	private TextView messagesTextView;
	private LinearLayout availabeActionsView;
	
	private Button ultimoBackButton;
	private Spinner ultimoTypeSpinner;
	private Spinner ultimoSuitvalueSpinner;
	private Spinner ultimoRoundSpinner;
	private Button announceButton;
	
	private RelativeLayout playedCardsView;
	private PlacedCardView[] playedCardViews;
	
	private View statisticsView;
	private LinearLayout statisticsSelfEntriesView;
	private LinearLayout statisticsOpponentEntriesView;
	private TextView statisticsGamepointsSelf;
	private TextView statisticsGamepointsOpponent;
	private LinearLayout statisticsPointsNames;
	private LinearLayout statisticsPointsValues;
	
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		ResourceMappings.init(this);
			
		cardWidth = getWindowManager().getDefaultDisplay().getWidth() / CARDS_PER_ROW;
		
		layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final String host = getIntent().getStringExtra("host");
		final int port = getIntent().getIntExtra("port", 8128);
		final String name = getIntent().getStringExtra("name");
		
		Thread connThread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					Socket socket = new Socket();
					socket.connect(new InetSocketAddress(host, port), 1000);
					conncection = new Connection(socket);
					conncection.sendPacket(new PacketLogin(name));
					conncection.addPacketHandler(new PacketHandler()
					{
						public void handlePacket(final Packet p)
						{
							runOnUiThread(new Runnable()
							{
								public void run()
								{
									GameActivtiy.this.handlePacket(p);
								}
							});
						}
						

						public void connectionClosed()
						{
							GameActivtiy.this.connectionClosed();
						}
					});
				}
				catch (IOException e)
				{
					e.printStackTrace();
					finish();
				}
			}
		});
		connThread.start();
		try
		{
			connThread.join(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private void inflateGameViews()
	{
		View game = layoutInflater.inflate(R.layout.game, null);
		setContentView(game);
		
		centerSpace = (FrameLayout)findViewById(R.id.center_space);
		centerSpace.bringToFront();
		
		playerNameViews = new TextView[]
		{
				null,
				(TextView)findViewById(R.id.playername_1),
				(TextView)findViewById(R.id.playername_2),
				(TextView)findViewById(R.id.playername_3),
		};
		
		myCardsView = (LinearLayout)findViewById(R.id.my_cards);
		myCardsView0 = (LinearLayout)findViewById(R.id.my_cards_0);
		myCardsView1 = (LinearLayout)findViewById(R.id.my_cards_1);
		
		okButton = (Button)findViewById(R.id.ok_button);
		throwButton = (Button)findViewById(R.id.throw_button);
		throwButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				conncection.sendPacket(new PacketThrowCards(myID));
			}
		});
		
		layoutInflater.inflate(R.layout.messages, centerSpace);
		messagesView = findViewById(R.id.messages_view);
		messagesDefaultView = findViewById(R.id.messages_default_view);
		messagesScrollView = (ScrollView)findViewById(R.id.messages_scroll);
		messagesTextView = (TextView)findViewById(R.id.messages_text_view);
		availabeActionsView = (LinearLayout)findViewById(R.id.available_actions);
		
		messagesUltimoView = findViewById(R.id.messages_ultimo_view);
		ultimoBackButton = (Button)findViewById(R.id.ultimo_back_buton);
		ultimoTypeSpinner = (Spinner)findViewById(R.id.ultimo_type_spinner);
		ultimoSuitvalueSpinner = (Spinner)findViewById(R.id.ultimo_suitvalue_spinner);
		ultimoRoundSpinner = (Spinner)findViewById(R.id.ultimo_round_spinner);
		announceButton = (Button)findViewById(R.id.ultimo_announce_button);
		
		ultimoBackButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				setUltimoViewVisible(false);
			}
		});
		
		ultimoTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				ultimoSuitvalueSpinner.setVisibility(position < 3 ? View.GONE : View.VISIBLE);
				ultimoRoundSpinner.setVisibility(position < 8 ? View.VISIBLE : View.GONE);
			}

			public void onNothingSelected(AdapterView<?> parent)
			{
			}
		});
		
		announceButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Announcement announcement;
				
				int roundIndex = 8 - ultimoRoundSpinner.getSelectedItemPosition();
				
				int typeSelectedPos = ultimoTypeSpinner.getSelectedItemPosition();
				if (typeSelectedPos < 2)
				{
					announcement = Announcements.ultimok.get(new TarockCard(typeSelectedPos + 1)).get(roundIndex);
				}
				else if (typeSelectedPos < 3)
				{
					announcement = Announcements.ultimok.get(new TarockCard(21)).get(roundIndex);
				}
				else if (typeSelectedPos < 8)
				{
					int suit = ultimoSuitvalueSpinner.getSelectedItemPosition();
					int value = 5 - (typeSelectedPos - 3);
					announcement = Announcements.ultimok.get(new SuitCard(suit, value)).get(roundIndex);
				}
				else if (typeSelectedPos < 10)
				{
					boolean small = typeSelectedPos == 8;
					int suit = ultimoSuitvalueSpinner.getSelectedItemPosition();
					announcement = (small ? Announcements.kisszincsaladok : Announcements.nagyszincsaladok)[suit];
				}
				else
				{
					throw new RuntimeException();
				}
				
				conncection.sendPacket(new PacketAnnounce(new AnnouncementContra(announcement, 0), myID));
			}
		});
		
		playedCardsView = new RelativeLayout(this);
		playedCardsView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		playedCardViews = new PlacedCardView[4];
		for (int i = 0; i < 4; i++)
		{
			playedCardViews[i] = new PlacedCardView(this, cardWidth, i);
			playedCardsView.addView(playedCardViews[i]);
		}
		centerSpace.addView(playedCardsView);
		
		layoutInflater.inflate(R.layout.statistics, centerSpace);
		statisticsView = findViewById(R.id.statistics_view);
		statisticsSelfEntriesView = (LinearLayout)findViewById(R.id.statistics_self_entries_list);
		statisticsOpponentEntriesView = (LinearLayout)findViewById(R.id.statistics_opponent_entries_list);
		statisticsGamepointsSelf = (TextView)findViewById(R.id.statistics_gamepoints_self);
		statisticsGamepointsOpponent = (TextView)findViewById(R.id.statistics_gamepoints_opponent);
		statisticsPointsNames = (LinearLayout)findViewById(R.id.statistics_points_names);
		statisticsPointsValues = (LinearLayout)findViewById(R.id.statistics_points_values);
		
		
		phaseToCenterView.clear();
		phaseToCenterView.put(Phase.BIDDING, messagesView);
		phaseToCenterView.put(Phase.CHANGING, messagesView);
		phaseToCenterView.put(Phase.CALLING, messagesView);
		phaseToCenterView.put(Phase.ANNOUNCING, messagesView);
		phaseToCenterView.put(Phase.GAMEPLAY, playedCardsView);
		phaseToCenterView.put(Phase.END, statisticsView);
		showCenterView(null);
	}
	
	private void setUltimoViewVisible(boolean visible)
	{
		if (visible)
		{
			messagesDefaultView.setVisibility(View.GONE);
			messagesUltimoView.setVisibility(View.VISIBLE);
		}
		else
		{
			messagesDefaultView.setVisibility(View.VISIBLE);
			messagesUltimoView.setVisibility(View.GONE);
		}
	}
	
	private Connection conncection;
	private List<String> playerNames;
	private PlayerCards myCards;
	private int myID = -1;
	private Map<Card, View> cardToViewMapping = new HashMap<Card, View>();
	
	private Phase gamePhase;
	private Map<Phase, View> phaseToCenterView = new HashMap<PacketPhase.Phase, View>();
	
	private String messages;
	
	private List<Card> cardsToSkart = new ArrayList<Card>();
	private boolean skarting = false;
	
	private void onStartGame(List<String> playerNames, int myID)
	{
		inflateGameViews();
		
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
	
	private void setCards(PlayerCards cards)
	{
		myCards = cards;
		arrangeCards();
	}

	private void changePhase(Phase phase)
	{
		gamePhase = phase;
		
		View centerView = phaseToCenterView.get(phase);
		if (phase == Phase.BIDDING)
		{
			showCenterView(centerView);
		}
		else
		{
			showCenterViewDelayed(centerView);
		}
		
		int throwButtonVisibility = View.GONE;
		if (gamePhase == Phase.BIDDING)
		{
			if (myCards.canBeThrown(false))
			{
				throwButtonVisibility = View.VISIBLE;
			}
		}
		else if (gamePhase == Phase.CHANGING)
		{
			if (myCards.canBeThrown(true))
			{
				throwButtonVisibility = View.VISIBLE;
			}
		}
		throwButton.setVisibility(throwButtonVisibility);
	}

	private void onCardsThrown(int player, PlayerCards thrownCards)
	{
		String msg = "";
		msg += playerNames.get(player);
		msg += " ";
		msg += getResources().getString(R.string.message_throw_cards);
		msg += "\n";
		msg += getResources().getString(R.string.press_ok);
		msg += "\n";
		displayMessage(msg);
	}
	
	private void showAvailableBids(List<Integer> bids)
	{
		availabeActionsView.removeAllViews();
		for (final int bid : bids)
		{
			Button bidButton = (Button)layoutInflater.inflate(R.layout.button, availabeActionsView, false);
			bidButton.setText(ResourceMappings.bidToName.get(bid));
			
			bidButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					availabeActionsView.removeAllViews();
					conncection.sendPacket(new PacketBid(bid, myID));
				}
			});
			availabeActionsView.addView(bidButton);
		}
	}
	
	private void onBid(int player, int bid)
	{
		String msg = "";
		msg += playerNames.get(player);
		msg += " ";
		msg += getResources().getString(R.string.message_bid);
		msg += ": ";
		msg += ResourceMappings.bidToName.get(bid);
		msg += "\n";
		displayMessage(msg);
	}
	
	private void onGotCardsFromTalon(List<Card> cards)
	{
		higlightAllName();
		
		myCards.getCards().addAll(cards);
		throwButton.setVisibility(myCards.canBeThrown(true) ? View.VISIBLE : View.GONE);
		skarting = true;
		arrangeCards();
		
		okButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				conncection.sendPacket(new PacketChange(cardsToSkart, myID));
			}
		});
	}
	
	private void onSkartDone(int player)
	{
		setHiglighted(player, false);
		if (player == myID)
		{
			okButton.setOnClickListener(null);
			cardsToSkart.clear();
			skarting = false;
		}
	}
	
	private void onSkartTarock(int[] counts)
	{
		String msg = "";
		for (int p = 0; p < 4; p++)
		{
			int count = counts[p];
			if (count > 0)
			{
				msg += playerNames.get(p);
				msg += " ";
				msg += count;
				msg += " ";
				msg += getResources().getString(R.string.message_skart_tarock);
				msg += "\n";
			}
		}
		displayMessage(msg);
	}

	private void showAvailableCalls(List<Card> calls)
	{
		availabeActionsView.removeAllViews();
		for (final Card card : calls)
		{
			Button callButton = (Button)layoutInflater.inflate(R.layout.button, availabeActionsView, false);
			callButton.setText(ResourceMappings.cardToName.get(card));
			callButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					availabeActionsView.removeAllViews();
					conncection.sendPacket(new PacketCall(card, myID));
				}
			});
			availabeActionsView.addView(callButton);
		}
	}
	
	private void onCall(int player, Card card)
	{
		messages += playerNames.get(player);
		messages += " ";
		messages += getResources().getString(R.string.message_call);
		messages += ": ";
		messages += ResourceMappings.cardToName.get(card);
		messages += "\n";
		messagesTextView.setText(messages);
		messagesScrollView.scrollTo(0, messagesScrollView.getHeight());
	}
	
	private void showAvailableAnnouncements(List<AnnouncementContra> announcements)
	{
		Collections.sort(announcements);
		availabeActionsView.removeAllViews();
		
		Button ultimoButton = (Button)layoutInflater.inflate(R.layout.button, availabeActionsView, false);
		ultimoButton.setText(ResourceMappings.roundNames[8]);
		ultimoButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				setUltimoViewVisible(true);
			}
		});
		availabeActionsView.addView(ultimoButton);
		
		for (final AnnouncementContra ac : announcements)
		{
			if (ac.getContraLevel() == 0 && !ac.getAnnouncement().isShownToUser()) continue;
			
			Button announceButton = (Button)layoutInflater.inflate(R.layout.button, availabeActionsView, false);
			announceButton.setText(ResourceMappings.getAnnouncementContraName(ac));
			announceButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					conncection.sendPacket(new PacketAnnounce(ac, myID));
				}
			});
			availabeActionsView.addView(announceButton);
		}
		
		okButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				availabeActionsView.removeAllViews();
				conncection.sendPacket(new PacketAnnounce(null, myID));
			}
		});
	}

	private void onAnnounce(int player, AnnouncementContra announcement)
	{
		setUltimoViewVisible(false);
		
		String msg = "";
		msg += playerNames.get(player);
		msg += " ";
		msg += getResources().getString(R.string.message_announce);
		msg += ": ";
		msg += ResourceMappings.getAnnouncementContraName(announcement);
		msg += "\n";
		displayMessage(msg);
	}

	private void playCard(int player, Card card)
	{
		int pos = getPositionFromPlayerID(player);
		final PlacedCardView playedCardView = playedCardViews[pos];
		
		playedCardView.addCard(card);
		playedCardView.bringToFront();
		//playedCardView.animatePlay();
		
		if (player == myID)
		{
			myCards.removeCard(card);
			
			View myCardView = cardToViewMapping.remove(card);
			myCardsView0.removeView(myCardView);
			myCardsView1.removeView(myCardView);
			
			if (myCards.getCards().size() == CARDS_PER_ROW)
			{
				arrangeCards();
			}
			
			/*int[] myCardViewLocation = new int[2];
			myCardView.getLocationOnScreen(myCardViewLocation);
			int[] playedCardViewLocation = new int[2];
			playedCardView.getLocationOnScreen(playedCardViewLocation);
			
			final Animation currentAnim = playedCardView.createPositionAnimation();
			
			float tx = myCardViewLocation[0] - playedCardViewLocation[0];
			float ty = myCardViewLocation[1] - playedCardViewLocation[1];
			Animation placeAnim = new TranslateAnimation(-tx, 0, -ty, 0);
			
			AnimationSet animSet = new AnimationSet(false);
			//animSet.addAnimation(currentAnim);
			animSet.addAnimation(placeAnim);
			animSet.setFillAfter(true);
			animSet.setDuration(800);
			playedCardView.startAnimation(animSet);
			
			animSet.setAnimationListener(new AnimationListener()
			{
				public void onAnimationStart(Animation animation)
				{
				}
				
				public void onAnimationRepeat(Animation animation)
				{
				}
				
				public void onAnimationEnd(Animation animation)
				{
					playedCardView.startAnimation(playedCardView.createPositionAnimation());
				}
			});*/
		}
	}
	
	private void onCardsTaken(final int winnerPlayer)
	{
		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				for (PlacedCardView playedCardView : playedCardViews)
				{
					playedCardView.animateTake(getPositionFromPlayerID(winnerPlayer));
				}
			}
		}, DELAY);
	}
	
	private void onTurn(int player)
	{
		if (gamePhase != PacketPhase.Phase.CHANGING)
		{
			highlightName(player);
		}
	}
	
	private void showStatistics(int selfGamePoints, int opponentGamePoints, List<PacketAnnouncementStatistics.Entry> selfEntries, List<Entry> opponentEntries, int[] points)
	{
		statisticsGamepointsSelf.setText(selfGamePoints + "");
		statisticsGamepointsOpponent.setText(opponentGamePoints + "");
		
		statisticsSelfEntriesView.removeAllViews();
		statisticsOpponentEntriesView.removeAllViews();
		
		appendHeaderToStatistics(statisticsSelfEntriesView, R.string.statictics_self, R.string.statictics_points);
		appendEntriesToStatistics(statisticsSelfEntriesView, selfEntries);
		
		appendHeaderToStatistics(statisticsOpponentEntriesView, R.string.statictics_opponent, R.string.statictics_points);
		appendEntriesToStatistics(statisticsOpponentEntriesView, opponentEntries);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
		lp.weight = 0.25F;
		for (int i = 0; i < 4; i++)
		{
			TextView nameView = new TextView(this);
			nameView.setText(playerNames.get(i));
			nameView.setGravity(Gravity.CENTER);
			
			TextView pointsView = new TextView(this);
			pointsView.setText(points[i] + "");
			pointsView.setGravity(Gravity.CENTER);
			
			statisticsPointsNames.addView(nameView, lp);
			statisticsPointsValues.addView(pointsView, lp);
		}
	}
	
	private void appendHeaderToStatistics(ViewGroup viewToAppend, int res0, int res1)
	{
		View entryView = layoutInflater.inflate(R.layout.statistics_header, viewToAppend, false);
		TextView nameView = (TextView)entryView.findViewById(R.id.statistics_announcement_name);
		TextView pointsView = (TextView)entryView.findViewById(R.id.statistics_announcement_points);
		
		nameView.setText(res0);
		pointsView.setText(res1);
		
		viewToAppend.addView(entryView);
	}
	
	private void appendEntriesToStatistics(ViewGroup viewToAppend, List<PacketAnnouncementStatistics.Entry> entries)
	{
		for (PacketAnnouncementStatistics.Entry entry : entries)
		{
			View entryView = layoutInflater.inflate(R.layout.statistics_entry, viewToAppend, false);
			TextView nameView = (TextView)entryView.findViewById(R.id.statistics_announcement_name);
			TextView pointsView = (TextView)entryView.findViewById(R.id.statistics_announcement_points);
			
			nameView.setText(ResourceMappings.getAnnouncementContraName(entry.getAnnouncementContra()));
			pointsView.setText(entry.getPoints() + "");
			pointsView.setTextColor(entry.getPoints() > 0 ? Color.BLACK : Color.RED);
			
			viewToAppend.addView(entryView);
		}
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
			
			ImageView cardView = new ImageView(this);
			cardView.setAdjustViewBounds(true);
			int padding = (int)(cardWidth * 0.1F / 2);
			cardView.setPadding(padding, padding, padding, padding);
			cardView.setLayoutParams(new LinearLayout.LayoutParams(cardWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
			if (card != null)
				cardView.setImageResource(PlacedCardView.getBitmapResForCard(card));
			
			final LinearLayout parentView = i < cardsUp ? myCardsView1 : myCardsView0;
			parentView.addView(cardView);
			cardToViewMapping.put(card, cardView);
			
			cardView.setOnClickListener(new OnClickListener()
			{
				private boolean selectedForSkart = false;
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
					else if (gamePhase == Phase.GAMEPLAY && !playedCardViews[0].isAnimating())
					{
						conncection.sendPacket(new PacketPlayCard(card, myID));
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
		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				if (pendingCenterView != v)
					return;
				
				showCenterView(pendingCenterView);
			}
		}, DELAY);
	}
	
	private void displayMessage(String msg)
	{
		messages += msg;
		messagesTextView.setText(messages);
		messagesScrollView.fullScroll(View.FOCUS_DOWN);
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

	public void handlePacket(Packet p)
	{
		if (p instanceof PacketStartGame)
		{
			PacketStartGame packet = ((PacketStartGame)p);
			onStartGame(packet.getNames(), packet.getPlayerID());
		}
		
		if (p instanceof PacketPlayerCards)
		{
			PacketPlayerCards packet = ((PacketPlayerCards)p);
			setCards(packet.getPlayerCards());
		}
		
		if (p instanceof PacketPhase)
		{
			PacketPhase packet = ((PacketPhase)p);
			changePhase(packet.getPhase());
		}
		
		if (p instanceof PacketTurn)
		{
			PacketTurn packet = ((PacketTurn)p);
			onTurn(packet.getPlayer());
		}
		
		if (p instanceof PacketCardsThrown)
		{
			PacketCardsThrown packet = ((PacketCardsThrown)p);
			onCardsThrown(packet.getPlayer(), packet.getThrownCards());
		}
		
		if (p instanceof PacketAvailableBids)
		{
			PacketAvailableBids packet = ((PacketAvailableBids)p);
			showAvailableBids(packet.getAvailableBids());
		}
		
		if (p instanceof PacketBid)
		{
			PacketBid packet = ((PacketBid)p);
			onBid(packet.getPlayer(), packet.getBid());
		}
		
		if (p instanceof PacketChange)
		{
			PacketChange packet = ((PacketChange)p);
			onGotCardsFromTalon(packet.getCards());
		}
		
		if (p instanceof PacketChangeDone)
		{
			PacketChangeDone packet = ((PacketChangeDone)p);
			onSkartDone(packet.getPlayer());
		}
		
		if (p instanceof PacketSkartTarock)
		{
			PacketSkartTarock packet = ((PacketSkartTarock)p);
			onSkartTarock(packet.getCounts());
		}
		
		if (p instanceof PacketAvailableCalls)
		{
			PacketAvailableCalls packet = ((PacketAvailableCalls)p);
			showAvailableCalls(packet.getAvailableCalls());
		}
		
		if (p instanceof PacketCall)
		{
			PacketCall packet = ((PacketCall)p);
			onCall(packet.getPlayer(), packet.getCalledCard());
		}
		
		if (p instanceof PacketAvailableAnnouncements)
		{
			PacketAvailableAnnouncements packet = ((PacketAvailableAnnouncements)p);
			showAvailableAnnouncements(packet.getAvailableAnnouncements());
		}
		
		if (p instanceof PacketAnnounce)
		{
			PacketAnnounce packet = ((PacketAnnounce)p);
			onAnnounce(packet.getPlayer(), packet.getAnnouncement());
		}
		
		if (p instanceof PacketPlayCard)
		{
			PacketPlayCard packet = ((PacketPlayCard)p);
			playCard(packet.getPlayer(), packet.getCard());
		}
		
		if (p instanceof PacketCardsTaken)
		{
			PacketCardsTaken packet = ((PacketCardsTaken)p);
			onCardsTaken(packet.getWinnerPlayer());
		}
		
		if (p instanceof PacketAnnouncementStatistics)
		{
			PacketAnnouncementStatistics packet = ((PacketAnnouncementStatistics)p);
			showStatistics(packet.getSelfGamePoints(), packet.getOpponentGamePoints(), packet.getSelfEntries(), packet.getOpponentEntries(), packet.getPoints());
		}
		
		if (p instanceof PacketReadyForNewGame)
		{
			okButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					conncection.sendPacket(new PacketReadyForNewGame());
				}
			});
		}
	}

	public void connectionClosed()
	{
		finish();
	}
	
	protected void onDestroy()
	{
		super.onDestroy();
		if (conncection != null)
		{
			conncection.closeRequest();
		}
	}

	private boolean doubleBackToExitPressedOnce = false;
	public void onBackPressed()
	{
		if (doubleBackToExitPressedOnce)
		{
			super.onBackPressed();
			return;
		}

		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				doubleBackToExitPressedOnce = false;
			}
		}, 1000);
	}
}

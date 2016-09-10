package com.tisza.tarock.gui;

import java.io.*;
import java.net.*;
import java.util.*;

import android.app.*;
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

public class GameActivtiy extends Activity implements PacketHandler
{
	private static final int CARDS_PER_ROW = 6;
	private int cardWidth;
	
	private TextView[] playerNameViews;
	private LinearLayout myCardsView0;
	private LinearLayout myCardsView1;
	private FrameLayout centerSpace;
	private Button okButton;
	private Button throwButton;
	
	private View messagesView;
	private Button switchViewButton;
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
	
	private LinearLayout statisticsView;
	private LinearLayout statisticsLinearLayout;	
	
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		ResourceMappings.init(this);
			
		cardWidth = getWindowManager().getDefaultDisplay().getWidth() / CARDS_PER_ROW;
		
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
		View game = View.inflate(this, R.layout.game, null);
		
		centerSpace = (FrameLayout)game.findViewById(R.id.center_space);
		
		playerNameViews = new TextView[]
		{
				null,
				(TextView)game.findViewById(R.id.playername_1),
				(TextView)game.findViewById(R.id.playername_2),
				(TextView)game.findViewById(R.id.playername_3),
		};
		
		myCardsView0 = (LinearLayout)game.findViewById(R.id.my_cards_0);
		myCardsView1 = (LinearLayout)game.findViewById(R.id.my_cards_1);
		
		okButton = (Button)game.findViewById(R.id.ok_button);
		throwButton = (Button)game.findViewById(R.id.throw_button);
		throwButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				conncection.sendPacket(new PacketThrowCards(myID));
			}
		});
		
		messagesView = View.inflate(this, R.layout.messages, null);
		messagesDefaultView = messagesView.findViewById(R.id.messages_default_view);
		messagesScrollView = (ScrollView)messagesView.findViewById(R.id.messages_scroll);
		messagesTextView = (TextView)messagesView.findViewById(R.id.messages);
		availabeActionsView = (LinearLayout)messagesView.findViewById(R.id.available_actions);
		
		messagesUltimoView = messagesView.findViewById(R.id.messages_ultimo_view);
		ultimoBackButton = (Button)messagesView.findViewById(R.id.ultimo_back_buton);
		ultimoTypeSpinner = (Spinner)messagesView.findViewById(R.id.ultimo_type_spinner);
		ultimoSuitvalueSpinner = (Spinner)messagesView.findViewById(R.id.ultimo_suitvalue_spinner);
		ultimoRoundSpinner = (Spinner)messagesView.findViewById(R.id.ultimo_round_spinner);
		announceButton = (Button)messagesView.findViewById(R.id.ultimo_announce_button);
		
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
		
		centerSpace.addView(messagesView);
		
		playedCardsView = new RelativeLayout(this);
		playedCardsView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		playedCardViews = new PlacedCardView[4];
		for (int i = 0; i < 4; i++)
		{
			playedCardViews[i] = new PlacedCardView(this, cardWidth, i);
			playedCardsView.addView(playedCardViews[i]);
		}
		centerSpace.addView(playedCardsView);
		
		statisticsView = (LinearLayout)View.inflate(this, R.layout.statistics, null);
		statisticsLinearLayout = (LinearLayout)statisticsView.findViewById(R.id.statistics_entries_list);
		centerSpace.addView(statisticsView);
		
		setContentView(game);
	}
	
	private ArrayAdapter<CharSequence> createAdapterForSpinner(int arrayResID)
	{
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, arrayResID, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
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
	private int cardsPlayed = 0;
	private Map<Card, View> cardToViewMapping = new HashMap<Card, View>();
	
	private String messages;
	
	private List<Card> cardsToSkart = new ArrayList<Card>();
	private boolean skarting = false;
	private boolean canPlaceCard = false;
	
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
		showCenterView(messagesView);
	}
	
	private void setCards(PlayerCards cards)
	{
		myCards = cards;
		throwButton.setVisibility(myCards.canBeThrown(false) ? View.VISIBLE : View.GONE);
		arrangeCards();
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
			Button bidButton = new Button(this);
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
	
	private void onSkartAccepted()
	{
		okButton.setOnClickListener(null);
		myCards.getCards().removeAll(cardsToSkart);
		cardsToSkart.clear();
		skarting = false;
		arrangeCards();
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
			Button callButton = new Button(this);
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
		
		Button ultimoButton = new Button(this);
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
			if (!ac.getAnnouncement().isShownToUser()) continue;
			
			Button announceButton = new Button(this);
			announceButton.setText(ResourceMappings.getAnnouncementContraContraName(ac));
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
		msg += ResourceMappings.getAnnouncementContraContraName(announcement);
		msg += "\n";
		displayMessage(msg);
	}

	private void playCard(int player, Card card)
	{
		showCenterView(playedCardsView);
		
		int pos = getPositionFromPlayerID(player);
		
		if (player == myID)
		{
			myCards.removeCard(card);
			View cardView = cardToViewMapping.remove(card);
			myCardsView0.removeView(cardView);
			myCardsView1.removeView(cardView);
			
			if (myCards.getCards().size() == CARDS_PER_ROW)
			{
				arrangeCards();
			}
		}
		
		playedCardViews[pos].setImageResource(getBitmapResForCard(card));
		playedCardViews[pos].bringToFront();
		
		cardsPlayed++;
	}
	
	private void onTurn(int player, PacketTurn.Type type)
	{
		int pos = getPositionFromPlayerID(player);
		
		myCardsView0.setBackgroundColor(Color.TRANSPARENT);
		for (TextView nameView : playerNameViews)
		{
			if (nameView == null) continue;
			nameView.setBackgroundColor(Color.TRANSPARENT);
		}
		
		if (pos == 0)
		{
			myCardsView0.setBackgroundColor(Color.MAGENTA);
		}
		else
		{
			for (TextView nameView : playerNameViews)
			{
				if (nameView == null) continue;
				nameView.setBackgroundColor(Color.TRANSPARENT);
			}
			playerNameViews[pos].setBackgroundColor(Color.MAGENTA);
		}

		
		if (type == PacketTurn.Type.PLAY_CARD)
		{
			showCenterView(playedCardsView);
			
			canPlaceCard = false;
			if (cardsPlayed % 4 != 0)
			{
				if (player == myID)
				{
					canPlaceCard = true;
				}
			}
			else
			{
				canPlaceCard = false;
				final int dir = getPositionFromPlayerID(player);
				new Handler().postDelayed(new Runnable()
				{
					public void run()
					{
						for (ImageView cardView : playedCardViews)
						{
							/*AnimationSet animSet = new AnimationSet(true);
							
							Animation currentAnim = cardView.getAnimation();
							animSet.addAnimation(currentAnim);
							
							float tx = 0;
							float ty = 0;
							if (dir == 0)
							{
								ty = playedCardsView.getHeight() / 2;
							}
							else if (dir == 1)
							{
								tx = playedCardsView.getWidth() / 2;
							}
							else if (dir == 2)
							{
								ty = -playedCardsView.getHeight() / 2;
							}
							else if (dir == 3)
							{
								tx = -playedCardsView.getWidth() / 2;
							}
							Animation takeAnim = new TranslateAnimation(0, tx, 0, ty);
							animSet.addAnimation(takeAnim);
							animSet.setDuration(1000);
							
							cardView.startAnimation(animSet);*/
							
							//((BitmapDrawable)cardView.getDrawable()).getBitmap().recycle();
							cardView.setImageBitmap(null);
						}
						canPlaceCard = true;
					}
				}, 400);
			}
		}
	}
	
	private void showStatistics(List<PacketAnnouncementStatistics.Entry> selfEntries, List<Entry> opponentEntries)
	{
		canPlaceCard = false;
		
		statisticsLinearLayout.removeAllViews();
		
		appendHeaderToStatistics(R.string.statictics_self, R.string.statictics_points);
		appendEntriesToStatistics(selfEntries);
		
		appendHeaderToStatistics(R.string.statictics_opponent, R.string.statictics_points);
		appendEntriesToStatistics(opponentEntries);
		
		showCenterView(statisticsView);
	}
	
	private void appendHeaderToStatistics(int res0, int res1)
	{
		View headerView = View.inflate(this, R.layout.statistics_header, null);
		TextView nameView = (TextView)headerView.findViewById(R.id.statistics_announcement_name);
		TextView pointsView = (TextView)headerView.findViewById(R.id.statistics_announcement_points);
		
		nameView.setText(res0);
		pointsView.setText(res1);
		
		statisticsLinearLayout.addView(headerView);
	}
	
	private void appendEntriesToStatistics(List<PacketAnnouncementStatistics.Entry> entries)
	{
		for (PacketAnnouncementStatistics.Entry entry : entries)
		{
			View entryView = View.inflate(this, R.layout.statistics_entry, null);
			TextView nameView = (TextView)entryView.findViewById(R.id.statistics_announcement_name);
			TextView pointsView = (TextView)entryView.findViewById(R.id.statistics_announcement_points);
			
			nameView.setText(ResourceMappings.getAnnouncementContraContraName(entry.getAnnouncementContra()));
			pointsView.setText(entry.getPoints() + "");
			pointsView.setTextColor(entry.getPoints() > 0 ? Color.BLACK : Color.RED);
			
			statisticsLinearLayout.addView(entryView);
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
				cardView.setImageResource(getBitmapResForCard(card));
			
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
							Animation a = new TranslateAnimation(0, 0, 0, -30);
							a.setDuration(300);
							a.setFillAfter(true);
							v.startAnimation(a);
						}
						else
						{
							cardsToSkart.remove(card);
							selectedForSkart = false;
							Animation a = new TranslateAnimation(0, 0, -30, 0);
							a.setDuration(300);
							a.setFillAfter(true);
							v.startAnimation(a);
						}
					}
					else if (canPlaceCard)
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
	
	private void showCenterView(View v)
	{
		int count = centerSpace.getChildCount();
		for (int i = 0; i < count; i++)
		{
			centerSpace.getChildAt(i).setVisibility(View.GONE);
		}
		v.setVisibility(View.VISIBLE);
	}
	
	private void displayMessage(String msg)
	{
		messages += msg;
		messagesTextView.setText(messages);
		messagesScrollView.fullScroll(View.FOCUS_DOWN);
	}
	
	private int getBitmapResForCard(Card card)
	{
		int id;
		if (ResourceMappings.cardToImageResource.containsKey(card))
		{
			id = ResourceMappings.cardToImageResource.get(card);
		}
		else
		{
			throw new RuntimeException(card + " has no image");
		}

		return id;
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
			if (packet.getPlayer() == myID)
			{
				onSkartAccepted();
			}
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
		
		if (p instanceof PacketAvailabeAnnouncements)
		{
			PacketAvailabeAnnouncements packet = ((PacketAvailabeAnnouncements)p);
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
		
		if (p instanceof PacketTurn)
		{
			PacketTurn packet = ((PacketTurn)p);
			onTurn(packet.getPlayer(), packet.getType());
		}
		
		if (p instanceof PacketAnnouncementStatistics)
		{
			PacketAnnouncementStatistics packet = ((PacketAnnouncementStatistics)p);
			showStatistics(packet.getSelfEntries(), packet.getOpponentEntries());
		}
		
		if (p instanceof PacketPoints)
		{
			PacketPoints packet = ((PacketPoints)p);
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

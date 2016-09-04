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
import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;

public class GameActivtiy extends Activity implements PacketHandler
{
	private int cardWidth;
	
	private TextView[] playerNameViews;
	private LinearLayout myCardsView0;
	private LinearLayout myCardsView1;
	private FrameLayout center_space;
	private Button okButton;
	
	private View biddingView;
	private ScrollView biddingScrollView;
	private TextView biddingTextView;
	private LinearLayout availabeBidsView;
	
	private RelativeLayout played_cards;
	private PlacedCardView[] playedCardViews;
	
	private LinearLayout statisticsView;
	private ScrollView statisticsScrollView;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		ResourceMappings.init(this);
			
		cardWidth = getWindowManager().getDefaultDisplay().getWidth() / 6;
		
		View game = View.inflate(this, R.layout.game, null);
		
		center_space = (FrameLayout)game.findViewById(R.id.center_space);
		
		playerNameViews = new TextView[]
		{
				null,
				(TextView)game.findViewById(R.id.playername_1),
				(TextView)game.findViewById(R.id.playername_2),
				(TextView)game.findViewById(R.id.playername_3),
		};
		
		biddingView = View.inflate(this, R.layout.bidding, null);
		biddingScrollView = (ScrollView)biddingView.findViewById(R.id.bidding_scroll);
		biddingTextView = (TextView)biddingView.findViewById(R.id.bidding_text);
		availabeBidsView = (LinearLayout)biddingView.findViewById(R.id.available_bids);
				
		myCardsView0 = (LinearLayout)game.findViewById(R.id.my_cards_0);
		myCardsView1 = (LinearLayout)game.findViewById(R.id.my_cards_1);
		
		okButton = (Button)game.findViewById(R.id.ok_button);
		
		played_cards = new RelativeLayout(this);
		played_cards.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		playedCardViews = new PlacedCardView[4];
		for (int i = 0; i < 4; i++)
		{
			playedCardViews[i] = new PlacedCardView(this, cardWidth, i);
			played_cards.addView(playedCardViews[i]);
		}
		center_space.addView(biddingView);
		
		statisticsView = (LinearLayout)View.inflate(this, R.layout.statistics, null);
		statisticsScrollView = (ScrollView)statisticsView.findViewById(R.id.statistics_scrollview);
		
		setContentView(game);
		
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
	
	private Connection conncection;
	private List<String> playerNames;
	private PlayerCards myCards;
	private int myID = -1;
	private int cardsPlayed = 0;
	private Map<Card, View> cardToViewMapping = new HashMap<Card, View>();
	
	private List<Card> cardsToSkart = new ArrayList<Card>();
	private boolean skarting = false;
	
	private void onStartGame(List<String> playerNames, int myID)
	{
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
	}
	
	private void setCards(PlayerCards cards)
	{
		myCards = cards;
		arrangeCards();
	}
	
	private void showAvailableBids(List<Integer> bids)
	{
		availabeBidsView.removeAllViews();
		for (final int bid : bids)
		{
			Button bidButton = new Button(this);
			bidButton.setText(bid + "");
			bidButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					availabeBidsView.removeAllViews();
					conncection.sendPacket(new PacketBid(bid, myID));
				}
			});
			availabeBidsView.addView(bidButton);
		}
	}
	
	private void onBid(int player, int bid)
	{
		String bidText = biddingTextView.getText().toString();
		if (bidText == null) bidText = "";
		bidText += playerNames.get(player);
		bidText += " licitalt: ";
		bidText += bid;
		bidText += "\n";
		biddingTextView.setText(bidText);
		biddingScrollView.scrollTo(0, biddingScrollView.getHeight());
	}
	
	private void onGotCardsFromTalon(List<Card> cards)
	{
		myCards.getCards().addAll(cards);
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
		skarting = false;
		arrangeCards();
	}
	
	private void showAvailableCalls(List<Card> calls)
	{
		availabeBidsView.removeAllViews();
		for (final Card card : calls)
		{
			Button callButton = new Button(this);
			callButton.setText(card.toString());
			callButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					availabeBidsView.removeAllViews();
					conncection.sendPacket(new PacketCall(card, myID));
				}
			});
			availabeBidsView.addView(callButton);
		}
	}
	
	private void onCall(int player, Card card)
	{
		String bidText = biddingTextView.getText().toString();
		if (bidText == null) bidText = "";
		bidText += playerNames.get(player);
		bidText += " hivott: ";
		bidText += card;
		bidText += "\n";
		biddingTextView.setText(bidText);
	}
	
	private void onAnnounce(int player, Announcement a)
	{
		String bidText = biddingTextView.getText().toString();
		if (bidText == null) bidText = "";
		bidText += playerNames.get(player);
		bidText += " bemondta: ";
		bidText += a == null ? "passz" : a.getClass().getSimpleName();
		bidText += "\n";
		biddingTextView.setText(bidText);
	}
	
	private void playCard(int player, Card card)
	{
		center_space.removeAllViews();
		center_space.addView(played_cards);
		
		int pos = getPositionFromPlayerID(player);
		
		if (player == myID)
		{
			myCards.removeCard(card);
			View cardView = cardToViewMapping.remove(card);
			myCardsView0.removeView(cardView);
			myCardsView1.removeView(cardView);
		}
		
		playedCardViews[pos].setImageBitmap(getBitmapForCard(card));
		playedCardViews[pos].bringToFront();
		
		cardsPlayed++;
	}
	
	private void onMyTurn(PacketTurn.Type type)
	{
		if (type == PacketTurn.Type.BID)
		{
		}
		if (type == PacketTurn.Type.CHANGE)
		{
		}
		if (type == PacketTurn.Type.CALL)
		{
		}
		if (type == PacketTurn.Type.ANNOUNCE)
		{
			availabeBidsView.removeAllViews();
			for (final Announcement a : Announcements.getAll())
			{
				Button announceButton = new Button(this);
				announceButton.setText(a.getClass().getSimpleName());
				announceButton.setOnClickListener(new OnClickListener()
				{
					public void onClick(View v)
					{
						conncection.sendPacket(new PacketAnnounce(a, myID));
					}
				});
				availabeBidsView.addView(announceButton);
			}
			
			okButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					conncection.sendPacket(new PacketAnnounce(null, myID));
				}
			});
		}
		if (type == PacketTurn.Type.PLAY_CARD)
		{
			if (center_space.getChildAt(0) != played_cards)
			{
				center_space.removeAllViews();
				center_space.addView(played_cards);
			}
		}
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
			if (cardsPlayed % 4 == 0)
			{
				final int dir = getPositionFromPlayerID(player);
				Handler handler = new Handler();
				handler.postDelayed(new Runnable()
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
								ty = played_cards.getHeight() / 2;
							}
							else if (dir == 1)
							{
								tx = played_cards.getWidth() / 2;
							}
							else if (dir == 2)
							{
								ty = -played_cards.getHeight() / 2;
							}
							else if (dir == 3)
							{
								tx = -played_cards.getWidth() / 2;
							}
							Animation takeAnim = new TranslateAnimation(0, tx, 0, ty);
							animSet.addAnimation(takeAnim);
							animSet.setDuration(1000);
							
							cardView.setAnimation(animSet);*/
							
							cardView.setImageBitmap(getBitmapForCard(null));
						}
					}
				}, 1500);
			}
		}
	}
	
	private void showStatistics(List<PacketAnnouncementStatistics.Entry> entries)
	{
		center_space.removeAllViews();
		center_space.addView(statisticsView);
		
		for (PacketAnnouncementStatistics.Entry entry : entries)
		{
			View entryView = View.inflate(this, R.layout.statistics_entry, null);
			TextView nameView = (TextView)entryView.findViewById(R.id.statistics_announcement_name);
			ImageView isSuccesfulView = (ImageView)entryView.findViewById(R.id.statistics_is_successful);
			TextView pointsView = (TextView)entryView.findViewById(R.id.statistics_announcement_points);
			
			nameView.setText(entry.getAnnouncement().getClass().getSimpleName());
			isSuccesfulView.setImageResource(ResourceMappings.announcementResultToImage.get(entry.getResult()));
			pointsView.setText(entry.getPoints() + "");
			
			System.out.println(entry.getAnnouncement().getClass().getSimpleName());
			
			statisticsScrollView.addView(entryView);
		}
	}

	public void handlePacket(Packet p)
	{
		if (p instanceof PacketStartGame)
		{
			PacketStartGame packet = ((PacketStartGame)p);
			onStartGame(packet.getNames(), packet.getID());
		}
		
		if (p instanceof PacketPlayerCards)
		{
			PacketPlayerCards packet = ((PacketPlayerCards)p);
			setCards(packet.getPlayerCards());
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
			if (packet.getPlayer() == myID)
			{
				onMyTurn(packet.getType());
			}
			
			onTurn(packet.getPlayer(), packet.getType());
		}
		
		if (p instanceof PacketAnnouncementStatistics)
		{
			PacketAnnouncementStatistics packet = ((PacketAnnouncementStatistics)p);
			showStatistics(packet.getEntries());
		}
		if (p instanceof PacketPoints)
		{
			PacketPoints packet = ((PacketPoints)p);
		}
		if (p instanceof PacketSkartTarock)
		{
			PacketSkartTarock packet = ((PacketSkartTarock)p);
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
	
	private void arrangeCards()
	{
		cardToViewMapping.clear();
		myCardsView0.removeAllViews();
		myCardsView1.removeAllViews();
		int cardsBottom = (int)Math.ceil(myCards.getCards().size() / 2F);
		for (int i = 0; i < myCards.getCards().size(); i++)
		{
			final Card card = myCards.getCards().get(i);
			
			ImageView cardView = new ImageView(GameActivtiy.this);
			cardView.setAdjustViewBounds(true);
			cardView.setImageBitmap(getBitmapForCard(card));
			
			int padding = 10;
			cardView.setPadding(padding, padding, padding, padding);
			cardView.setLayoutParams(new LinearLayout.LayoutParams(cardWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
			final LinearLayout parentView = i < cardsBottom ? myCardsView0 : myCardsView1;
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
					else
					{
						conncection.sendPacket(new PacketPlayCard(card, myID));
						System.out.println(card);
					}
				}
			});
		}
	}
	
	private Bitmap getBitmapForCard(Card card)
	{
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmp = Bitmap.createBitmap(174, 289, conf);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();
		paint.setTextSize(10);
		paint.setColor(Color.GREEN);
		if (card != null) canvas.drawText(card.toString(), 0, 0, paint);
		
		if (card == null) return null;
		
		int id = 0;
		if (ResourceMappings.cardToImageResource.containsKey(card))
		{
			id = ResourceMappings.cardToImageResource.get(card);
		}
		bmp = BitmapFactory.decodeResource(getResources(), id);
		return bmp;
	}
	
	private int getPositionFromPlayerID(int id)
	{
		return (id - myID + 4) % 4;
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
}

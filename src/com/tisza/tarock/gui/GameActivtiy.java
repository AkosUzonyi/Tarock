package com.tisza.tarock.gui;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tisza.tarock.R;
import com.tisza.tarock.announcement.Announcement;
import com.tisza.tarock.announcement.Announcements;
import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.card.SuitCard;
import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.game.AnnouncementContra;
import com.tisza.tarock.game.PhaseEnum;
import com.tisza.tarock.message.EventHandler;
import com.tisza.tarock.message.action.Action;
import com.tisza.tarock.message.action.ActionAnnounce;
import com.tisza.tarock.message.action.ActionAnnouncePassz;
import com.tisza.tarock.message.action.ActionBid;
import com.tisza.tarock.message.action.ActionCall;
import com.tisza.tarock.message.action.ActionChange;
import com.tisza.tarock.message.action.ActionPlayCard;
import com.tisza.tarock.message.action.ActionReadyForNewGame;
import com.tisza.tarock.message.action.ActionThrowCards;
import com.tisza.tarock.message.event.EventActionFailed.Reason;
import com.tisza.tarock.message.event.EventAnnouncementStatistics;
import com.tisza.tarock.message.event.EventAnnouncementStatistics.Entry;
import com.tisza.tarock.net.Connection;
import com.tisza.tarock.net.PacketHandler;
import com.tisza.tarock.net.packet.Packet;
import com.tisza.tarock.net.packet.PacketAction;
import com.tisza.tarock.net.packet.PacketEvent;
import com.tisza.tarock.net.packet.PacketLogin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivtiy extends Activity implements PacketHandler, EventHandler
{
	public static final String LOG_TAG = "Tarokk";
	
	private static final int DELAY = 1600;
	private static final int CARDS_PER_ROW = 6;
	private int cardWidth;
	
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
	private PlayedCardView[] playedCardViews;
	
	private View statisticsView;
	private TextView statisticsGamepointsSelf;
	private TextView statisticsGamepointsOpponent;
	private LinearLayout statisticsSelfEntriesView;
	private LinearLayout statisticsOpponentEntriesView;
	private TextView statisticsSumPointsView;
	private TextView[] statisticsPointsNameViews = new TextView[4];
	private TextView[] statisticsPointsValueViews = new TextView[4];
	
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
		setContentView(R.layout.game);
		
		centerSpace = (FrameLayout)findViewById(R.id.center_space);
		
		playerNameViews = new TextView[]
		{
				null,
				(TextView)findViewById(R.id.player_name_1),
				(TextView)findViewById(R.id.player_name_2),
				(TextView)findViewById(R.id.player_name_3),
		};
		
		playerMessageViews = new TextView[]
		{
				(TextView)findViewById(R.id.player_message_0),
				(TextView)findViewById(R.id.player_message_1),
				(TextView)findViewById(R.id.player_message_2),
				(TextView)findViewById(R.id.player_message_3),
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
				doAction(new ActionThrowCards());
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
				
				doAction(new ActionAnnounce(new AnnouncementContra(announcement, 0)));
			}
		});
		
		playedCardsView = new RelativeLayout(this);
		playedCardsView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		playedCardViews = new PlayedCardView[4];
		for (int i = 0; i < 4; i++)
		{
			playedCardViews[i] = new PlayedCardView(this, cardWidth, i);
			playedCardsView.addView(playedCardViews[i]);
		}
		centerSpace.addView(playedCardsView);
		
		layoutInflater.inflate(R.layout.statistics, centerSpace);
		statisticsView = findViewById(R.id.statistics_view);
		statisticsGamepointsSelf = (TextView)findViewById(R.id.statistics_gamepoints_self);
		statisticsGamepointsOpponent = (TextView)findViewById(R.id.statistics_gamepoints_opponent);
		statisticsSelfEntriesView = (LinearLayout)findViewById(R.id.statistics_self_entries_list);
		statisticsOpponentEntriesView = (LinearLayout)findViewById(R.id.statistics_opponent_entries_list);
		statisticsSumPointsView = (TextView)findViewById(R.id.statistics_sum_points);
		statisticsPointsNameViews[0] = (TextView)findViewById(R.id.statistics_points_name_0);
		statisticsPointsNameViews[1] = (TextView)findViewById(R.id.statistics_points_name_1);
		statisticsPointsNameViews[2] = (TextView)findViewById(R.id.statistics_points_name_2);
		statisticsPointsNameViews[3] = (TextView)findViewById(R.id.statistics_points_name_3);
		statisticsPointsValueViews[0] = (TextView)findViewById(R.id.statistics_points_value_0);
		statisticsPointsValueViews[1] = (TextView)findViewById(R.id.statistics_points_value_1);
		statisticsPointsValueViews[2] = (TextView)findViewById(R.id.statistics_points_value_2);
		statisticsPointsValueViews[3] = (TextView)findViewById(R.id.statistics_points_value_3);
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
	
	private PhaseEnum gamePhase;
	
	private String messages;
	
	private List<Card> cardsToSkart = new ArrayList<Card>();
	private boolean skarting = false;

	private boolean waitingForTakeAnimation;
	
	public void startGame(int myID, List<String> playerNames)
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
	
	public void cardsChanged(PlayerCards cards)
	{
		myCards = cards;
		arrangeCards();
	}

	public void phaseChanged(PhaseEnum phase)
	{
		gamePhase = phase;
		
		throwButton.setVisibility(View.GONE);
		
		if (phase == PhaseEnum.BIDDING)
		{
			showCenterView(messagesView);
			
			if (myCards.canBeThrown(false))
			{
				throwButton.setVisibility(View.VISIBLE);
			}
		}
		else if (phase == PhaseEnum.CALLING)
		{
			showCenterView(messagesView);
			
			if (myCards.canBeThrown(false))
			{
				throwButton.setVisibility(View.VISIBLE);
			}
		}
		else if (phase == PhaseEnum.CHANGING)
		{
			showCenterView(messagesView);
			
			if (myCards.canBeThrown(true))
			{
				throwButton.setVisibility(View.VISIBLE);
			}
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

	public void cardsThrown(int player, PlayerCards thrownCards)
	{
		String msg = getResources().getString(R.string.message_throw_cards);
		displayPlayerActionMessage(player, msg);
		displayMessage(getResources().getString(R.string.press_ok) + "\n");
	}
	
	public void availableBids(List<Integer> bids)
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
					doAction(new ActionBid(bid));
				}
			});
			availabeActionsView.addView(bidButton);
		}
	}
	
	public void bid(int player, int bid)
	{
		String msg = ResourceMappings.bidToName.get(bid);
		displayPlayerActionMessage(player, R.string.message_bid, msg);
	}
	
	public void cardsFromTalon(List<Card> cards)
	{
		higlightAllName();
		
		myCards.getCards().addAll(cards);
		throwButton.setVisibility(myCards.canBeThrown(true) ? View.VISIBLE : View.GONE);
		skarting = true;
		arrangeCards();
		
		okButton.setVisibility(View.VISIBLE);
		okButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				doAction(new ActionChange(cardsToSkart));
			}
		});
	}
	
	public void changeDone(int player)
	{
		setHiglighted(player, false);
		if (player == myID)
		{
			okButton.setVisibility(View.GONE);
			cardsToSkart.clear();
			skarting = false;
		}
	}
	
	public void skartTarokk(int[] counts)
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

	public void availableCalls(List<Card> calls)
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
					doAction(new ActionCall(card));
				}
			});
			availabeActionsView.addView(callButton);
		}
	}
	
	public void call(int player, Card card)
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
	
	public void availableAnnouncements(List<AnnouncementContra> announcements)
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
					doAction(new ActionAnnounce(ac));
				}
			});
			availabeActionsView.addView(announceButton);
		}
		
		okButton.setVisibility(View.VISIBLE);
		okButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				okButton.setVisibility(View.GONE);
				availabeActionsView.removeAllViews();
				doAction(new ActionAnnouncePassz());
			}
		});
	}

	public void announce(int player, AnnouncementContra announcement)
	{
		setUltimoViewVisible(false);
		
		String msg = ResourceMappings.getAnnouncementContraName(announcement);
		displayPlayerActionMessage(player, R.string.message_announce, msg);
	}

	public void passz(int player)
	{
		setUltimoViewVisible(false);
		
		String msg = getResources().getString(R.string.passz);
		displayPlayerActionMessage(player, R.string.message_announce, msg);
	}

	public void cardPlayed(int player, Card card)
	{
		int pos = getPositionFromPlayerID(player);
		final PlayedCardView playedCardView = playedCardViews[pos];
		
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
	
	public void cardsTaken(final int winnerPlayer)
	{
		waitingForTakeAnimation = true;
		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				for (PlayedCardView playedCardView : playedCardViews)
				{
					playedCardView.animateTake(getPositionFromPlayerID(winnerPlayer));
					waitingForTakeAnimation = false;
				}
			}
		}, DELAY);
	}
	
	public void turn(int player)
	{
		if (gamePhase != PhaseEnum.CHANGING)
		{
			highlightName(player);
		}
	}
	
	public void statistics(int selfGamePoints, int opponentGamePoints, List<EventAnnouncementStatistics.Entry> selfEntries, List<Entry> opponentEntries, int sumPoints, int[] points)
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
			pointsView.setText(points[i] + "");
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
	
	private void appendEntriesToStatistics(ViewGroup viewToAppend, List<EventAnnouncementStatistics.Entry> entries)
	{
		for (EventAnnouncementStatistics.Entry entry : entries)
		{
			View entryView = layoutInflater.inflate(R.layout.statistics_entry, viewToAppend, false);
			TextView nameView = (TextView)entryView.findViewById(R.id.statistics_announcement_name);
			TextView pointsView = (TextView)entryView.findViewById(R.id.statistics_sum_points);
			
			nameView.setText(ResourceMappings.getAnnouncementContraName(entry.getAnnouncementContra()));
			pointsView.setText(entry.getPoints() + "");
			pointsView.setTextColor(entry.getPoints() >= 0 ? Color.BLACK : Color.RED);
			
			viewToAppend.addView(entryView);
		}
	}

	public void pendingNewGame()
	{
		okButton.setVisibility(View.VISIBLE);
		okButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				okButton.setVisibility(View.GONE);
				doAction(new ActionReadyForNewGame());
			}
		});
	}
	
	public void wrongAction(Reason type)
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
			
			ImageView cardView = new ImageView(this);
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
						doAction(new ActionPlayCard(card));
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
			public void onAnimationStart(Animation animation)
			{
			}
			
			public void onAnimationRepeat(Animation animation)
			{
			}
			
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

	public void handlePacket(Packet p)
	{
		if (p instanceof PacketEvent)
		{
			((PacketEvent)p).getEvent().handle(this);
		}
	}
	
	private void doAction(Action action)
	{
		conncection.sendPacket(new PacketAction(action));
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

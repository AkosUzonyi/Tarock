import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApiService } from '../_services/api.service';
import { Action, Chat, Game, GameSession, GameState } from '../_models/game-objects';
import { AuthService } from '../_services/auth.service';

@Component({
  selector: 'app-game-session',
  templateUrl: './game-session.component.html',
  styleUrls: ['./game-session.component.css'],
})
export class GameSessionComponent implements OnInit, OnDestroy {
  gameSessionId: number;
  gameSession: GameSession | null = null;

  seat: number | null = null;

  actions: Action[] = [];
  nextActionOrdinal = 0;
  game: Game | null = null;
  gameState: GameState | null = null;

  cardsToFold: string[] = [];
  cardTable: (string | null)[] = new Array(4);

  actionSubscription: Subscription | null = null;
  chatSubscription: Subscription | null = null;
  userSubscription: Subscription | null = null;

  chats: Chat[] = [];
  lastChatTime: number = 0;
  chatInputContent: string = "";

  constructor(
    private apiService: ApiService,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {
    this.gameSessionId = Number(route.snapshot.paramMap.get('id'));
  }

  ngOnInit() {
    this.updateGameSession();
    this.userSubscription = this.authService.getUserObservable().subscribe(user => this.calculateSeat());
  }

  ngOnDestroy() {
    if (this.gameSession?.state === 'lobby')
      this.apiService.leaveGameSession(this.gameSessionId).subscribe();
    this.actionSubscription?.unsubscribe();
    this.chatSubscription?.unsubscribe();
    this.userSubscription?.unsubscribe();
  }

  start() {
    if (this.gameSession !== null)
      this.apiService.startGameSession(this.gameSession.id).subscribe(() => this.updateGameSession());
  }

  private getCurrentGameId() {
    let gameId = this.gameSession?.currentGameId ?? null;
    if (gameId === null)
      throw 'no game is in progress';
    return gameId;
  }

  executeAction(action: string) {
    this.apiService.postAction(this.getCurrentGameId(), action).subscribe();
  }

  sendChat() {
    if (this.gameSession === null)
      return;
    this.apiService.postChat(this.gameSession.id, this.chatInputContent).subscribe();
    this.chatInputContent = "";
  }

  private pollChats() {
    if (this.gameSession === null)
      return;
    this.chatSubscription?.unsubscribe();
    this.chatSubscription =
    this.apiService.getChat(this.gameSession.id, this.lastChatTime)
    .subscribe(newChats => {
      this.chats = this.chats.concat(newChats);
      if (this.chats.length >= 1)
        this.lastChatTime = this.chats[this.chats.length - 1].time + 1;
      this.pollChats();
    });
  }

  clickCard(event : Event) {
    let element = event.target as Element;
    let card = element.getAttribute('data-card')!;
    switch (this.gameState?.phase) {
      case 'folding':
        element.classList.toggle('card-selected');
        let indexOfCard = this.cardsToFold.indexOf(card);
        if (indexOfCard < 0)
          this.cardsToFold.push(card);
        else
          this.cardsToFold.splice(indexOfCard, 1);
        break;
      case 'gameplay':
        this.executeAction('play:' + card);
        break;
    }
  }

  pressOK() {
    switch (this.gameState?.phase) {
      case 'announcing':
        this.executeAction('announce:passz');
        break;
      case 'folding':
        this.executeAction('fold:' + this.cardsToFold.join(','));
        break;
      case 'end':
      case 'interrupted':
        this.executeAction('newgame:');
        break;
    }
  }

  private updateGameSession() {
    this.apiService.getGameSession(this.gameSessionId).subscribe(gameSession => {
      let updateGame = this.gameSession?.currentGameId !== gameSession.currentGameId;
      this.gameSession = gameSession;
      if (this.gameSession?.state === 'lobby')
        this.apiService.joinGameSession(this.gameSessionId).subscribe();
      this.chats = [];
      this.lastChatTime = this.gameSession.createTime;
      this.pollChats();
      if (updateGame)
        this.updateGame();
    });
  }

  private updateGame() {
    this.apiService.getGame(this.getCurrentGameId()).subscribe(game => {
      this.game = game;
      this.actions = [];
      this.nextActionOrdinal = 0;
      this.calculateSeat();
      this.updateGameState();
      this.pollActions();
    });
  }

  private calculateSeat() {
    this.seat = null;

    const user = this.authService.getUser();
    if (this.game === null || user === null)
      return;

    this.game.players.forEach((player, i) => {
      if (player.user.id === user.id)
        this.seat = i;
    });
  }

  private pollActions() {
    this.actionSubscription?.unsubscribe();
    this.actionSubscription =
    this.apiService.getActions(this.getCurrentGameId(), this.nextActionOrdinal)
    .subscribe(newActions => {
      this.actions = this.actions.concat(newActions);
      if (this.actions.length >= 1)
        this.nextActionOrdinal = this.actions[this.actions.length - 1].ordinal + 1;
      //this.updateGameSession(); //TODO
      this.updateGameState();
      this.pollActions();
    });
  }

  private rotateLeft(list: any[], n: number) {
    for (let i = 0; i < n; i++)
      list.push(list.shift());
  }

  private updateGameState() {
    this.apiService.getGameState(this.getCurrentGameId())
      .subscribe(s => {
        this.gameState = s;
        this.cardTable = this.gameState.currentTrick;
        if (this.seat !== null)
          this.rotateLeft(this.cardTable, this.seat);
      });
  }
}

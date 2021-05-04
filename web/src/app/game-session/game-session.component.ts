import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription, timer } from 'rxjs';
import { ApiService } from '../_services/api.service';
import { Action, Chat, Game, GameSession, GameState, GameStatePlayerInfo } from '../_models/dto';
import { AuthService } from '../_services/auth.service';
import { GameTranslateService } from '../_services/game-translate.service';

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
  playerInfosRotated: GameStatePlayerInfo[] | null = null;

  cardsToFold: string[] = [];

  trickTaking: boolean = false;
  trickTaken: boolean = false;
  takeTrickSubscription: Subscription | null = null;

  actionSubscription: Subscription | null = null;
  chatSubscription: Subscription | null = null;
  userSubscription: Subscription | null = null;
  updateGameSessionTimeout: any = null;

  chats: Chat[] = [];
  lastChatTime: number = 0;
  chatInputContent: string = "";

  constructor(
    private apiService: ApiService,
    private route: ActivatedRoute,
    private authService: AuthService,
    public gameTranslateService: GameTranslateService,
  ) {
    this.gameSessionId = Number(route.snapshot.paramMap.get('id'));
  }

  ngOnInit() {
    this.userSubscription = this.authService.getUserObservable().subscribe(user => {
      if (this.game === null)
        return;

      this.calculateSeat();
      this.updateGameState();
    });

    this.updateGameSession();
    this.userSubscription = this.authService.getUserObservable().subscribe(user => this.calculateSeat());
  }

  ngOnDestroy() {
    if (this.gameSession?.state === 'lobby')
      this.apiService.leaveGameSession(this.gameSessionId).subscribe();
    this.actionSubscription?.unsubscribe();
    this.chatSubscription?.unsubscribe();
    this.userSubscription?.unsubscribe();
    this.takeTrickSubscription?.unsubscribe();
    clearTimeout(this.updateGameSessionTimeout);
  }

  start() {
    if (this.gameSession !== null)
      this.apiService.startGameSession(this.gameSession.id).subscribe(() => this.updateGameSession());
  }

  private getCurrentGameId() {
    const gameId = this.gameSession?.currentGameId ?? null;
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
    const element = event.target as Element;
    const card = element.getAttribute('data-card')!;
    switch (this.gameState?.phase) {
      case 'folding':
        element.classList.toggle('card-selected');
        const indexOfCard = this.cardsToFold.indexOf(card);
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

  private updateGameSession() {
    this.apiService.getGameSession(this.gameSessionId).subscribe(gameSession => {
      const updateGame = this.gameSession?.currentGameId !== gameSession.currentGameId;
      this.gameSession = gameSession;
      if (this.gameSession?.state === 'lobby') {
        this.apiService.joinGameSession(this.gameSessionId).subscribe();
        this.updateGameSessionTimeout = setTimeout(() => this.updateGameSession(), 1000);
      }
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
      this.updateGameState();
      this.pollActions();
    });
  }

  private rotateLeft(list: any[], n: number) {
    const result = []
    for (let i = 0; i < list.length; i++)
      result.push(list[(i + n) % list.length]);
    return result;
  }

  private updateGameState() {
    this.apiService.getGameState(this.getCurrentGameId()).subscribe(gameState => {
      this.gameState = gameState;
      this.playerInfosRotated = this.rotateLeft(this.gameState.playerInfos, this.seat ?? 0);

      let trickEmpty = this.gameState.playerInfos.every(playerInfo => playerInfo.currentTrickCard === null);
      if (!trickEmpty) {
        this.takeTrickSubscription?.unsubscribe();
        this.trickTaken = false;
        this.trickTaking = false;
      }
      if (trickEmpty && !this.trickTaking && !this.trickTaken) {
        this.trickTaking = true;
        this.takeTrickSubscription = timer(2000).subscribe(() => {
          this.trickTaking = false;
          this.trickTaken = true;
        });
      }
    }, error => {
      if (error.status == 410)
        this.updateGameSession();
    });
  }
}

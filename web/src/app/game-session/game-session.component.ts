import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription, timer } from 'rxjs';
import { animate, style, transition, trigger } from '@angular/animations';
import { ApiService } from '../_services/api.service';
import { Action, Chat, GameSession, Game as Game, GamePlayerInfo } from '../_models/dto';
import { AuthService } from '../_services/auth.service';
import { GameTranslateService } from '../_services/game-translate.service';

@Component({
  selector: 'app-game-session',
  templateUrl: './game-session.component.html',
  styleUrls: ['./game-session.component.css'],
  animations: [
    trigger('actionBubbleAnimation', [
      transition(':leave', [
        style({ opacity: '1' }),
        animate('0.5s linear', style({ opacity: '0' })),
      ]),
    ]),
  ],
})
export class GameSessionComponent implements OnInit, OnDestroy {
  gameSessionId: number;
  gameSession: GameSession | null = null;

  seat: number | null = null;

  actions: Action[] = [];
  nextActionOrdinal = 0;
  game: Game | null = null;
  playersRotated: GamePlayerInfo[] | null = null;
  actionBubbleContent: (string | null)[] = Array(4).fill(null);

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
      this.calculateSeat();
      this.updateGame();
    });

    this.updateGameSession();
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

  foldCards() {
    this.executeAction('fold:' + this.cardsToFold.join(','));
    this.cardsToFold = [];
  }

  sendChat() {
    if (this.gameSession === null || this.chatInputContent.length === 0)
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

  clickCard(card: string) {
    switch (this.game?.phase) {
      case 'folding':
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
      const isNewGame = this.gameSession?.currentGameId !== gameSession.currentGameId;
      this.gameSession = gameSession;
      if (this.gameSession?.state === 'lobby') {
        this.apiService.joinGameSession(this.gameSessionId).subscribe();
        this.updateGameSessionTimeout = setTimeout(() => this.updateGameSession(), 1000);
      }
      this.chats = [];
      this.lastChatTime = this.gameSession.createTime;
      this.pollChats();
      if (isNewGame) {
        this.actions = [];
        this.nextActionOrdinal = 0;
        this.cardsToFold = [];
        this.updateGame();
        this.pollActions(true);
      }
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

  private pollActions(init: boolean) {
    this.actionSubscription?.unsubscribe();
    this.actionSubscription =
    this.apiService.getActions(this.getCurrentGameId(), this.nextActionOrdinal)
    .subscribe(newActions => {
      this.actions = this.actions.concat(newActions);

      if (!init) {
        for (let action of newActions) {
          const pos = this.getPositionFromSeat(action.seat);
          this.actionBubbleContent[pos] = action.action;
          setTimeout(() => {
            if (this.actionBubbleContent[pos] === action.action)
              this.actionBubbleContent[pos] = null;
          }, 2500);
        }
      }

      if (this.actions.length >= 1)
        this.nextActionOrdinal = this.actions[this.actions.length - 1].ordinal + 1;
      this.updateGame();
      this.pollActions(false);
    });
  }

  private getPositionFromSeat(seat: number): number {
    return (seat - (this.seat ?? 0) + 4) % 4;
  }

  private rotateLeft(list: any[], n: number) {
    const result = []
    for (let i = 0; i < list.length; i++)
      result.push(list[(i + n) % list.length]);
    return result;
  }

  private updateGame() {
    const gameId = this.gameSession?.currentGameId ?? null;
    if (gameId === null) {
      this.game = null;
      return;
    }

    this.apiService.getGame(this.getCurrentGameId()).subscribe(game => {
      this.game = game;
      this.calculateSeat();
      this.playersRotated = this.rotateLeft(this.game.players, this.seat ?? 0);

      let trickEmpty = this.game.players.every(playerInfo => playerInfo.currentTrickCard === null);
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

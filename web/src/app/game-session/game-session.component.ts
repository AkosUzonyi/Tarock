import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../api.service';
import { Action, GameSession, GameState } from '../game-objects';

@Component({
  selector: 'app-game-session',
  templateUrl: './game-session.component.html',
  styleUrls: ['./game-session.component.css'],
})
export class GameSessionComponent implements OnInit {
  gameSessionId: number;
  gameSession: GameSession | null = null;

  userId = 4;
  seat: number | null = null;

  actions: Action[] = []
  nextActionOrdinal = 0;
  gameState: GameState | null = null;

  cardsToFold: string[] = []

  constructor(private apiService: ApiService, route: ActivatedRoute) {
    this.gameSessionId = Number(route.snapshot.paramMap.get('id'));
  }

  ngOnInit() {
    this.updateGameSession();
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
      if (updateGame) {
        this.actions = [];
        this.nextActionOrdinal = 0;
        this.updateSeat();
        this.updateGameState();
        this.pollActions();
      }
    });
  }

  private updateSeat() {
    this.apiService.getGame(this.getCurrentGameId()).subscribe(game => {
      this.seat = null;
      game.players.forEach((player, i) => {
        if (player.user.id == this.userId)
          this.seat = i;
        console.log(this.seat);
      });
    });
  }

  private pollActions() {
    this.apiService.getActions(this.getCurrentGameId(), this.nextActionOrdinal).subscribe(newActions => {
      this.actions = this.actions.concat(newActions);
      if (this.actions.length >= 1)
        this.nextActionOrdinal = this.actions[this.actions.length - 1].ordinal + 1;
      this.updateGameSession(); //TODO
      this.updateGameState();
      this.pollActions();
    });
  }

  private updateGameState() {
    this.apiService.getGameState(this.getCurrentGameId())
      .subscribe(s => this.gameState = s);
  }
}

import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ApiService } from '../_services/api.service';
import { CreateGameSessionDialogComponent } from '../create-game-session-dialog/create-game-session-dialog.component';
import { GameSession, User } from '../_models/dto';
import { interval, Observable, Subscription } from 'rxjs';
import { AuthService } from '../_services/auth.service';

@Component({
  selector: 'app-game-session-list',
  templateUrl: './game-session-list.component.html',
  styleUrls: ['./game-session-list.component.css']
})
export class GameSessionListComponent implements OnInit {
  gameSessions: GameSession[] = [];

  private updateListSubscription: Subscription | null = null;

  constructor(private apiService: ApiService, private dialog: MatDialog, private authService: AuthService) { }

  ngOnInit(): void {
    this.updateList();
    this.updateListSubscription = interval(2000).subscribe(() => this.updateList());
  }

  ngOnDestroy() {
    this.updateListSubscription?.unsubscribe();
  }

  private gameSessionContainsUser(gameSession: GameSession, user: User | null): boolean {
    return gameSession.players.findIndex(p => p.user.id == user?.id) >= 0;
  }

  isMyGameSession(gameSession: GameSession): boolean {
    return this.gameSessionContainsUser(gameSession, this.authService.getUser());
  }

  private compareGameSessions(gs0: GameSession, gs1: GameSession): number {
    var myUser = this.authService.getUser();
    if (myUser && this.gameSessionContainsUser(gs0, myUser) != this.gameSessionContainsUser(gs1, myUser))
      return this.gameSessionContainsUser(gs0, myUser) ? -1 : 1;

    if (gs0.state != gs1.state)
      return gs0.state == 'lobby' ? -1 : 1;

    var onlineCount0 = 0;
    var onlineCount1 = 0;
    var realPlayerCount0 = 0;
    var realPlayerCount1 = 0;

    gs0.players.forEach(p =>{
      if (p.user.isOnline && !p.user.isBot)
        onlineCount0++;
      if (!p.user.isBot)
        realPlayerCount0++;
    })
    gs1.players.forEach(p =>{
      if (p.user.isOnline && !p.user.isBot)
        onlineCount1++;
      if (!p.user.isBot)
        realPlayerCount1++;
    })


    if (onlineCount0 != onlineCount1)
      return onlineCount1 - onlineCount0;

    if (realPlayerCount0 != realPlayerCount1)
      return realPlayerCount1 - realPlayerCount0;

		return gs1.id - gs0.id;
  }

  private updateList() {
    this.apiService.getGameSessions()
      .subscribe(g => this.gameSessions = g.sort((gs0, gs1) => this.compareGameSessions(gs0, gs1)));
  }

  createGame() {
    const dialogRef = this.dialog.open(CreateGameSessionDialogComponent);
    dialogRef.afterClosed().subscribe(() => this.updateList());
  }

  deleteGameSession(id: number) {
    this.apiService.deleteGameSession(id).subscribe(() => this.updateList());
  }
}

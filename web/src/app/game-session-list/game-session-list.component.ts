import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { GameSession } from '../game-objects';

@Component({
  selector: 'app-game-session-list',
  templateUrl: './game-session-list.component.html',
  styleUrls: ['./game-session-list.component.css']
})
export class GameSessionListComponent implements OnInit {
  gameSessions: GameSession[] = [];

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.apiService.getGameSessions()
      .subscribe(g => this.gameSessions = g);
  }
}

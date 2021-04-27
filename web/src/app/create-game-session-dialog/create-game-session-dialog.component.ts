import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ApiService } from '../_services/api.service';
import { DoubleRoundType, GameType } from '../_models/dto';

@Component({
  selector: 'app-create-game-session-dialog',
  templateUrl: './create-game-session-dialog.component.html',
  styleUrls: ['./create-game-session-dialog.component.css']
})
export class CreateGameSessionDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<CreateGameSessionDialogComponent>,
    private apiService: ApiService
  ) { }

  gameType: GameType = localStorage.getItem('defaultGameType') as GameType ?? 'paskievics';
  doubleRoundType: DoubleRoundType = localStorage.getItem('defaultDoubleRoundType') as DoubleRoundType ?? 'none';

  ngOnInit(): void {
    this.dialogRef.disableClose = false;
  }

  create() {
    localStorage.setItem('defaultGameType', this.gameType);
    localStorage.setItem('defaultDoubleRoundType', this.doubleRoundType);
    this.apiService.createGameSession(this.gameType, this.doubleRoundType).subscribe();
    this.dialogRef.close();
  }
}

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

  gameType: GameType = 'paskievics';
  doubleRoundType: DoubleRoundType = 'none';

  ngOnInit(): void {
    this.dialogRef.disableClose = false;
  }

  create() {
    this.apiService.createGameSession(this.gameType, this.doubleRoundType).subscribe();
    this.dialogRef.close();
  }

}

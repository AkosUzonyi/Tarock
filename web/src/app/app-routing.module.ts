import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GameSessionListComponent } from './game-session-list/game-session-list.component';
import { GameSessionComponent } from './game-session/game-session.component';

const routes: Routes = [
  { path: 'gameSessions', component: GameSessionListComponent },
  { path: 'gameSessions/:id', component: GameSessionComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

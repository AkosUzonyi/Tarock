import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GameSessionListComponent } from './game-session-list/game-session-list.component';

const routes: Routes = [
  {path: 'gameSessions', component: GameSessionListComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

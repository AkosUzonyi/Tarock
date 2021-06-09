import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GameSessionListComponent } from './game-session-list/game-session-list.component';
import { GameSessionComponent } from './game-session/game-session.component';
import { LoginComponent } from './login/login.component';

const routes: Routes = [
  { path: '', redirectTo: '/gameSessions', pathMatch: 'full' },
  { path: 'gameSessions', component: GameSessionListComponent },
  { path: 'gameSessions/:id', component: GameSessionComponent },
  { path: 'login', component: LoginComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { GameSessionListComponent } from './game-session-list/game-session-list.component';
import { HttpClientModule } from '@angular/common/http';
import { GameSessionComponent } from './game-session/game-session.component';

@NgModule({
  declarations: [
    AppComponent,
    GameSessionListComponent,
    GameSessionComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }

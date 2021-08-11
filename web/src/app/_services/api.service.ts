import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Action, Chat, DoubleRoundType, GameSession, Game, GameType, User, LoginResult } from '../_models/dto';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})

export class ApiService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) { }

  login(provider: string, token: string): Observable<LoginResult> {
    const body = { 'provider': provider, 'token': token };
    return this.http.post<LoginResult>(`${this.baseUrl}/auth/login`, body);
  }

  getGameSessions(): Observable<GameSession[]> {
    return this.http.get<GameSession[]>(`${this.baseUrl}/gameSessions`);
  }

  getGameSession(id: number): Observable<GameSession> {
    return this.http.get<GameSession>(`${this.baseUrl}/gameSessions/${id}`);
  }

  createGameSession(type: GameType, doubleRoundType: DoubleRoundType): Observable<number> {
    const body = {'type': type, 'doubleRoundType': doubleRoundType};
    return this.http.post<HttpResponse<any>>(`${this.baseUrl}/gameSessions`, body, { observe: 'response' }).pipe(
      map((response) => {
        const arr = response.headers.get('Location')!.split('/');
        return Number(arr[arr.length - 1]);
      })
    );
  }

  deleteGameSession(id: number): Observable<{}> {
    return this.http.delete(`${this.baseUrl}/gameSessions/${id}`);
  }

  joinGameSession(id: number): Observable<{}> {
    return this.http.post(`${this.baseUrl}/gameSessions/${id}/join`, null);
  }

  leaveGameSession(id: number): Observable<{}> {
    return this.http.post(`${this.baseUrl}/gameSessions/${id}/leave`, null);
  }

  startGameSession(id: number): Observable<{}> {
    return this.http.post(`${this.baseUrl}/gameSessions/${id}/start`, null);
  }

  getChat(gameSessionId: number, from?: number): Observable<Chat[]> {
    const params: any = {}
    if (from !== undefined)
      params.from = String(from);
    return this.http.get<Chat[]>(`${this.baseUrl}/gameSessions/${gameSessionId}/chat`, {params: params});
  }

  postChat(gameSessionId: number, message: string): Observable<{}> {
    const body = {'message': message};
    return this.http.post(`${this.baseUrl}/gameSessions/${gameSessionId}/chat`, body);
  }

  getActions(gameId: number, from?: number): Observable<Action[]> {
    const params: any = {}
    if (from !== undefined)
      params.from = String(from);
    return this.http.get<Action[]>(`${this.baseUrl}/games/${gameId}/actions`, {params: params});
  }

  postAction(gameId: number, action: string): Observable<{}> {
    const body = {'action': action};
    return this.http.post(`${this.baseUrl}/games/${gameId}/actions`, body);
  }

  getGame(id: number): Observable<Game> {
    return this.http.get<Game>(`${this.baseUrl}/games/${id}`);
  }
}

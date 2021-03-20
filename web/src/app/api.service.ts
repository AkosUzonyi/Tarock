import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GameSession } from './game-objects';

@Injectable({
  providedIn: 'root'
})

export class ApiService {
  private baseUrl = 'api';

  constructor(private http: HttpClient) { }

  getGameSessions(): Observable<GameSession[]> {
    return this.http.get<GameSession[]>('localhost:8000/gameSessions');
  }
}

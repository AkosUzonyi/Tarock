export type GameType = 'paskievics' | 'illusztralt' | 'magas' | 'zebi';
export type DoubleRoundType = 'none' | 'peculating' | 'stacking' | 'multiplying';
export type GameSessionState = 'lobby' | 'game' | 'deleted';
export type Phase = 'bidding' | 'folding' | 'calling' | 'announcing' | 'gameplay' | 'end' | 'interrupted';
export type Team = 'caller' | 'opponent';

export interface User {
  id: number,
  name: string,
  imgUrl: string,
  isOnline: boolean,
  isBot: boolean,
}

export interface LoginResult {
  token: string;
  user: User;
}

export interface Player {
  user: User,
  points: number,
}

export interface GameSession {
  id: number;
  type: GameType;
  doubleRoundType: DoubleRoundType;
  state: GameSessionState;
  players: [Player];
  currentGameId: number | null;
  createTime: number;
}

export interface Game {
  id: number;
  type: GameType;
  players: [Player];
  gameSessionId: number;
  createTime: number;
}

export interface Action {
  ordinal: number;
  action: string;
  seat: number;
  time: number;
}

export interface Chat {
  userID: number;
  message: string;
  time: number;
}

export interface GameState {
  cards: (string[] | null)[];
  phase: Phase;
  turn: [boolean];
  canThrowCards: boolean;
  teamInfo: [Team | null];
  availableActions: [string];
  tarockFoldCount: [number];
  callerTarockFold: [string];
  currentTrick: [string | null];
  previousTrick: [string] | null;
  previousTrickWinner: number | null;
  statistics: {
    callerCardPoints: number;
    opponentCardPoints: number;
    announcementResults: [{
      'announcement': string;
      'points': number;
      'team': Team;
    }],
    sumPoints: number;
    pointMultiplier: number;
  }
}

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
  players: Player[];
  currentGameId: number | null;
  createTime: number;
}

export interface Action {
  ordinal: number;
  action: string;
  seat: number;
  time: number;
}

export interface Chat {
  user: User;
  message: string;
  time: number;
}

export interface GameStatePlayerInfo {
  user: User;
  cards: string[];
  turn: boolean;
  team: "caller" | "opponent" | null;
  tarockFoldCount: number;
  visibleFoldedCards: string[];
  currentTrickCard: string | null;
  previousTrickCard: string | null;
  points: number;
}

export interface GameState {
  id: number;
  type: GameType;
  gameSessionId: number;
  createTime: number;
  phase: Phase;
  canThrowCards: boolean;
  availableActions: string[];
  previousTrickWinner: number | null;
  players: GameStatePlayerInfo[];
  statistics: {
    callerCardPoints: number;
    opponentCardPoints: number;
    callerAnnouncementResults: AnnouncementResult[];
    opponentAnnouncementResults: AnnouncementResult[];
    sumPoints: number;
    pointMultiplier: number;
  }
}

export interface AnnouncementResult {
  announcement: string;
  points: number;
}

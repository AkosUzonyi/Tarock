export interface User {
  id: number,
  name: string,
  imgUrl: string,
  isOnline: boolean,
  isBot: boolean,
}

export interface Player {
  user: User,
  points: number,
}

export interface GameSession {
  id: number;
  type: "paskievics"|"illusztralt"|"magas"|"zebi";
  doubleRoundType: "none"|"peculating"|"stacking"|"multiplying";
  state: "lobby"|"game"|"deleted";
  players: [Player];
  currentGameID: number;
  createTime: number;
}

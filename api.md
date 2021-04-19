# Tarock API

The tarock server provides an HTTP API under the following base URL:

        https://tarokk.net/api/v1

The endpoints are listed below.

## Overview

### Game session

A game session is a series of games played by at least 4 players. Game sessions are created by a user with the parameters they choose. Everybody is permitted to list all the game sessions that are not deleted. Game sessions have three states: LOBBY, GAME, DELETED. Initially when a game session is created it is in LOBBY state. The user who created the lobby is automatically added to the player list. While being in LOBBY state, anybody can join or leave. Any user who is currently in the lobby can start the game session at any time (if there are not enough players, bots are added automatically). Doing so changes the game session state to GAME, and a new game object is automatically created. The player list is now fixed, nobody can join or leave. Any user playing in the game session can delete it. Doing so changes the state to DELETED, and removes it from the list of game sessions. Every game session has a chat, where every user is permitted to send messages.

### Game

A "game" is one game (that starts from bidding and ends with the evaluation). Each game is created automatically, when a game session (being in lobby state) is started, or when a previous game ends and all the players are ready for a new game. Each game has exactly 4 players, even if he game session has more than 4 players. Each game has a reference to the beginner player as an index to the game session's player list. This player and the next 3 players are playing in the current game (wrapping around in case the list end is reached). The beginner player index is incremented for each new game in the session. Inside the game players are referenced by a seat number. The beginner player has seat number 0, and the rest of the players have 1, 2 and 3 in turn order. The game state is advanced by actions made by players.

## String representations of game objects

Strings are a sweet spot between human-readable and machine-readable data. So let's start by giving string IDs to some game objects.

### Cards

The strings representing suit cards always match the following regular expression:

        [a-d][1-5]

The letter a,b,c,d represent the suit: hearts, diamonds, spades, clubs respectively. The digit is equal to the point values of the card (ace, jack, rider, queen, king in order).

Tarock cards are represendted by a letter 't' followed by their number.

Examples:
- `a1`: Kőr ász
- `c4`: Pikk dáma
- `t6`: 6-os tarokk
- `t22`: Skíz

### Announcements

The string representation of announcements always match the following regular expression:

        [a-z]*(S[a-d])?(C[a-dt][0-9]+)?(T[0-8])?(K[1-6s])?

The first lowercase part is a human readable ID of the announcement, the possible values are listed below. The rest of the announcement string are composed of 4 optional parameters each starting with an uppercase letter. The parameters are:
- S: suit
- C: card
- T: trick
- K: contra level

The S, C and T parameters are only applicable to specific announcements as listed below. Their meaning should be self-explanatory. The K parameter is applicable to all announcements, but isn't mandatory. If it's followed by a digit, it means the contra level of the announcement. Otherwise it's followed by an 's', meaning the announcement is silent (only used for the end game statistics, it cannot be announced). If the contra level is 0 (not under any contra) the K parameter must not be used.

The possible values for the lowercase parts, and the parameters they take:
- jatek
- hkp
- nyolctarokk
- kilenctarokk
- trull
- negykiraly
- banda (S)
- dupla
- hosszudupla
- kezbevacak (T)
- szinesites
- volat
- kiralyultimo
- ketkiralyok
- haromkiralyok
- zaroparos
- xxifogas
- parosfacan
- ultimo (C, T)
- kisszincsalad (S)
- nagyszincsalad (S)

Most of these are hopefully self-explanatory, maybe except two:
- hkp: Hivatalból kontra parti
- kezbevacak: The collection of "Centrum", "Kismadár", "Nagymadár". The trick parameter is 4, 5, 6 respectively.

Examples:
- `jatek`: Játék
- `trullK2`: Rekontra trull
- `bandaSb`: Káró banda
- `ultimoCa5T7`: Kőr király uhu
- `nagyszincsaladSdK6`: Fedák sári treff nagyszíncsalád
- `ultimoCt1T8Ks`: Csendes pagát ultimó
- `kezbevacakT4`: Centrum

*Note: On the client side, the real names of the announcements can be easily constructed by concatenating the names of the objects referenced by the parameters before the announcement's name. That's why for example the `ketkiralyok` and `haromkiralyok` are different announcements - it would be logical to use the same base announcement id with a trick parameter, but then client side name conversion would be harder ("Uhu" and "Nagymadár" are not part of their names). Interesting fact: `ultimo` and `kezbevacak` should be resolved to an empty string, since the parameter names are enough.*

### Actions

Actions are inputs from a player that alters the game state. The following action types exists:
- bid
- fold
- call
- announce
- play
- newgame
- throw

The string representation of an action is one of these categories, followed by a colon, an then some extra data:
- bid:\
  0,1,2,3,p representing the bids, or pass.
- fold:\
  a card list to be fold, separated by commas
- call:\
  a card to be called
- announce:\
  an announcement to be announced, or "passz"
- play:\
  a card to be played
- newgame:\
  no extra data - the action is a signal that the user is ready for a new game to be started after the game finished
- throw:\
  no extra data - the player throws his cards, interrupting the game

Examples:
- `bid:3`
- `bid:p`
- `fold:a2,a4,c1`
- `fold:`
- `call:t20`
- `announce:bandaSaK1`
- `play:c4`
- `newgame:`
- `throw:`

## HTTP endpoints

Timestamps are unix timestamps in milliseconds.

The JSON schema of the requests and responses are in an unofficial but (hopefully) intuitive format. The fields marked with an asterisk ("?") are optional. The response body is only sent if the result code is successful. In case of errors, no response body is sent (this might change in the future).

After authentication, the requests in the same session are on the behalf of the authenticated user. Clients should not expect sessions to be valid TODO

### POST /auth/login

Logs in the user using an access token from facebook or google.

Request body:
```
{
        "provider": "facebook"|"google",
        "token": String
}
```

Response:
- 200: Login successful
- 400: Provider is not valid
- 401: The login failed due to invalid or expired access token

```
{
        "token": String,
        "user": User
}
```

### GET /users/{userID}

Return data about the given user.

Response:
- 200: Success
- 404: User does not exists

```
{
        "id": Int,
        "name": String,
        "imgUrl": String?,
        "isOnline": Bool,
        "isBot": Bool
}
```

### POST /keepAlive

Clients should send this request in every 10 seconds. This serves two purposes:
- Marks the currently logged in user online (for other users to see that you are connected to the game)
- Keeps a TCP connection alive, making requests faster (no TCP+TLS handshake needed)

### GET /gameSessions/{gameSessionID}

Returns data about the given game session.

Response:
- 200: Success
- 404: Game session does not exists

```
{
        "id": Int,
        "type": "paskievics"|"illusztralt"|"magas"|"zebi",
        "doubleRoundType": "none"|"peculating"|"stacking"|"multiplying"
        "state": "lobby"|"game"|"deleted",
        "players": [{
                "user": User,
                "points": Int
        }],
        "currentGameID": Int?
        "createTime": Long
}
```

### GET /gameSessions

Returns a list of all active (not deleted) game sessions. The format is a list of game session objects.

Response:
- 200: Success

### POST /gameSessions

Creates a new game session. The authenticated user is automatically placed in the lobby.

Request body:
```
{
        "type": "paskievics"|"illusztralt"|"magas"|"zebi",
        "doubleRoundType": "none"|"peculating"|"stacking"|"multiplying"
}
```

Response:
- 201: Game session created (Location header is set to /gameSessions/{gameSessionID})
- 400: Invalid arguments
- 401: Authentication is required

### DELETE /gameSessions/{gameSessionID}

Deletes the game session.

Response:
- 204: Game session deleted (or didn't even exist)
- 401: Authentication is required
- 403: The authenticated user does not play in the game session

### POST /gameSessions/{gameSessionID}/join

Joins the game session (it must be in lobby state).

Response:
- 204: Join successful
- 401: Authentication is required
- 404: Game session does not exists
- 409: The game session is not in lobby state

### POST /gameSessions/{gameSessionID}/leave

Leaves the game session (it must be in lobby state).

Response:
- 204: Leave successful
- 401: Authentication is required
- 404: Game session does not exists
- 409: The game session is not in lobby state

### POST /gameSessions/{gameSessionID}/start

Starts the game session (if it is in lobby state). If the lobby does not contain enough players, bots are added automatically.

Response:
- 204: Game session successfully started
- 401: Authentication is required
- 403: The authenticated user is not in the lobby
- 404: Game session does not exists
- 409: The game session is not in lobby state

### GET /gameSessions/{gameSessionID}/chat

Returns the chat messages sent to this game session. Maximum 100 messages returned.

Parameters:
- from: Return messages sent after this unix timestamp in milliseconds. If 0 or missing, return all messages. If this parameter is provided and there are no messages sent after this time, the connection is kept alive, until at least one message arrives (long polling).

Response:
- 200: Success
- 404: Game session does not exists
- 408: Request timed out. Clients should resend the request.

```
[{
        "user": User
        "message": String,
        "time": Long
}]
```

### POST /gameSessions/{gameSessionID}/chat

Sends a chat message to the game session.

Request body:
```
{
        "message": String
}
```

Response:
- 204: Message successfully sent
- 401: Authentication is required
- 404: Game session does not exists
- 413: The message length exceeds the 255 character limit

### GET /games/{gameID}

Returns some basic data about the given game.

Response:
- 200: Success
- 404: Game does not exists

```
{
        "id": Int,
        "type": "paskievics"|"illusztralt"|"magas"|"zebi",
        "gameSessionId": Int,
        "players": [{
                "user": User,
                "points": Int
        }],
        "createTime": Long,
}
```

### GET /games/{gameID}/actions

Return the actions that were executed in this game. Each action has an ordinal number. These ordinal numbers are monotonically increasing in time, but not guaranteed to be consecutive integers. The returned list is sorted by ordinal numbers.

The fold actions are always sent as `fold:`, omitting the folded cards, as they should not be visible to the users.

Parameters:
- from: Return the actions only from this ordinal number. If no such action exists yet, the connection if kept alive until at least one action is available (long polling). Usually clients should set this parameter to the last action they received previously plus one (to query the new actions the client hasn't received yet). If missing, return all actions (no long polling).

Response:
- 200: Success
- 404: Game does not exists
- 408: Request timed out. Clients should resend the request.

```
[{
        "ordinal": Int,
        "action": String,
        "seat": Int,
        "time": Long
}]
```

### POST /games/{gameID}/actions

Request body:
```
{
        "action": String
}
```

Response:
- 204: Action successful
- 400: The action string is invalid
- 401: Authentication is required
- 403: The authenticated user is not playing in this game
- 404: Game does not exists
- 413: The length of the action string exceeds the 255 character limit
- 422: The action does not comply with the game rules

### GET /games/{gameID}/state

Returns a lot of data describing the current state of the game. Most of the fields are redundant (given the actions executed in the game), their goal is to help the client applications to render the game without knowing much of the game rules. The state is updated only when an action is executed. The returned information is not the same for all players.

The fields in the response:
- phase: The current phase of the game
- canThrowCards: Whether throw button should be shown
- availableActions: A list of actions that are executable at this by the player (if it's an other player's turn, the list is empty). Only bid, call, and announce actions are listed this way. Used for displaying action buttons.
- previousTrickWinner: The seat number of the player, who won the last trick. Used for the taking animation, and positioning the taken cards.
- playerInfos: A four element list containing information about the players in seat order.
- playerInfos.user: The user of the player.
- playerInfos.card: The list of the cards the player hold in their hands. Only self cards are shown, the card list of other players are null (for kibices everything is null).
- playerInfos.turn: Whether the player name should be highlighted, indicating that it's their turn. Most of the time exactly one player has a true value here (except a few cases, for example folding).
- playerInfos.team: Used for coloring the name of the player, indicating which team they are in. Null means the authenticated player doesn't know which team the other player is in.
- playerInfos.tarockFoldCount: Count of folded tarocks of the player. 0 before folding.
- playerInfos.visibleFoldedCards: The list folded cards by the player visible to anybody. In case the caller player folds tarocks, those appear here.
- playerInfos.currentTrickCard: The cards played in the current trick by the player.
- playerInfos.previousTrickCard: The cards played in the previous trick by each player.
- statistics.callerCardPoints: The sum of the values of the card won by the caller team.
- statistics.opponentCardPoints: The sum of the values of the card won by the opponent team.
- statistics.announcements.announcement: An announcement contributing to the points.
- statistics.announcements.points: The points earned for the announcement by the team who announced it (should be shown with inverted sign if the player is in the other team)
- statistics.announcements.team: The team who announced the announcement (or fulfilled/failed it silently)
- statistics.sumPoints: The total points earned by the caller team in this game (should be shown with inverted sign if the player is in the opponent team)
- statistics.pointMultiplier: The point multiplier for this game from the double rounds. The points above are already multiplied by it.

Response:
- 200: Success
- 404: Game does not exists

```
{
        "phase": "bidding"|"folding"|"calling"|"announcing"|"gameplay"|"end"|"interrupted",
        "canThrowCards": Bool,
        "availableActions": [String],
        "previousTrickWinner": Int?,
        "playerInfos": [{
                "user": User,
                "cards": [Card],
                "turn": Bool,
                "team": "caller"|"opponent"|null,
                "tarockFoldCount": Int,
                "visibleFoldedCards": [Card],
                "currentTrickCard": Card?,
                "previousTrickCard": Card?,
        }],
        "statistics": {
                "callerCardPoints": Int,
                "opponentCardPoints": Int,
                "callerAnnouncementResults": [{
                        "announcement": Announcement,
                        "points": Int
                }],
                "opponentAnnouncementResults": [{
                        "announcement": Announcement,
                        "points": Int
                }],
                "sumPoints": Int,
                "pointMultiplier": Int,
        }
}
```

TODO

phase: finished,
team: declarer


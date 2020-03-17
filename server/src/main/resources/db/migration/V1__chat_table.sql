CREATE TABLE chat (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_session_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    message VARCHAR(1024) NOT NULL,
    time BIGINT NOT NULL,
    FOREIGN KEY(game_session_id) REFERENCES game_session(id),
    FOREIGN KEY(user_id) REFERENCES user(id)
);

INSERT INTO chat(game_session_id, user_id, message, time)
SELECT game.game_session_id, player.user_id, substr(action.action, 6), action.time
FROM action, game, player
WHERE
game.id = action.game_id and
player.game_session_id = game.game_session_id and
player.seat = action.seat and
action.action LIKE "chat:%";

DELETE FROM action
WHERE action LIKE "chat:%";

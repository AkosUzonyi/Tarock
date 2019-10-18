CREATE TABLE user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    facebook_id VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    img_url VARCHAR(255),
    registration_time BIGINT NOT NULL
);

CREATE TABLE friendship (
    id0 INTEGER,
    id1 INTEGER,
    PRIMARY KEY (id0, id1),
    FOREIGN KEY(id0) REFERENCES user(id),
    FOREIGN KEY(id1) REFERENCES user(id)
);

CREATE TABLE game_session (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type VARCHAR(255) NOT NULL,
    double_round_type VARCHAR(255) NOT NULL,
    current_game_id INTEGER,
    create_time BIGINT NOT NULL,
    FOREIGN KEY(current_game_id) REFERENCES game(id)
);

CREATE TABLE player (
    game_session_id INTEGER,
    seat TINYINT,
    user_id INTEGER,
    points INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (game_session_id, seat),
    FOREIGN KEY(game_session_id) REFERENCES game_session(id),
    FOREIGN KEY(user_id) REFERENCES user(id)
);

CREATE TABLE game (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_session_id INTEGER NOT NULL,
    beginner_player TINYINT NOT NULL,
    create_time BIGINT NOT NULL,
    FOREIGN KEY(game_session_id) REFERENCES game_session(id)
);

CREATE INDEX game_create_time
ON game(create_time);

CREATE TABLE deck_card (
    game_id INTEGER,
    ordinal SMALLINT,
    card VARCHAR(255) NOT NULL,
    PRIMARY KEY (game_id, ordinal),
    FOREIGN KEY(game_id) REFERENCES game(id)
);

CREATE TABLE action (
    game_id INTEGER,
    ordinal SMALLINT,
    seat TINYINT NOT NULL,
    action VARCHAR(1024) NOT NULL,
    time BIGINT NOT NULL,
    PRIMARY KEY (game_id, ordinal),
    FOREIGN KEY(game_id) REFERENCES game(id)
);

CREATE TABLE fcm_token (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    token varchar(255) NOT NULL UNIQUE,
    user_id INTEGER NOT NULL,
    FOREIGN KEY(user_id) REFERENCES user(id)
);

CREATE INDEX fcm_token_user_id
ON fcm_token(user_id);

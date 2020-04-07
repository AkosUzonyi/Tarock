CREATE TABLE user (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    img_url VARCHAR(255),
    registration_time BIGINT NOT NULL
);

CREATE TABLE idp_user (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    idp_service_id VARCHAR(255) NOT NULL,
    idp_user_id VARCHAR(255) NOT NULL,
    user_id INTEGER NOT NULL,
    UNIQUE (idp_service_id, idp_user_id),
    CONSTRAINT fk_idp_user_user_id FOREIGN KEY(user_id) REFERENCES user(id)
);

INSERT INTO user VALUES (1, "bot0", NULL, 1572114201000), (2, "bot1", NULL, 1572114201000), (3, "bot2", NULL, 1572114201000);

CREATE TABLE friendship (
    id0 INTEGER NOT NULL,
    id1 INTEGER NOT NULL,
    PRIMARY KEY (id0, id1),
    CONSTRAINT fk_friendship_user_id0 FOREIGN KEY(id0) REFERENCES user(id),
    CONSTRAINT fk_friendship_user_id1 FOREIGN KEY(id1) REFERENCES user(id)
);

CREATE TABLE game_session (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(255) NOT NULL,
    double_round_type VARCHAR(255) NOT NULL,
    double_round_data INTEGER NOT NULL,
    current_game_id INTEGER,
    create_time BIGINT NOT NULL
);

CREATE TABLE player (
    game_session_id INTEGER NOT NULL,
    seat TINYINT NOT NULL,
    user_id INTEGER NOT NULL,
    points INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (game_session_id, seat),
    CONSTRAINT fk_player_game_session_id FOREIGN KEY(game_session_id) REFERENCES game_session(id),
    CONSTRAINT fk_player_user_id FOREIGN KEY(user_id) REFERENCES user(id)
);

CREATE TABLE game (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    game_session_id INTEGER NOT NULL,
    beginner_player TINYINT NOT NULL,
    create_time BIGINT NOT NULL,
    CONSTRAINT fk_game_game_session_id FOREIGN KEY(game_session_id) REFERENCES game_session(id)
);

CREATE INDEX ix_game_create_time ON game(create_time);
ALTER TABLE game_session ADD CONSTRAINT fk_game_session_current_game_id FOREIGN KEY(current_game_id) REFERENCES game(id);

CREATE TABLE deck_card (
    game_id INTEGER NOT NULL,
    ordinal SMALLINT NOT NULL,
    card VARCHAR(255) NOT NULL,
    PRIMARY KEY (game_id, ordinal),
    CONSTRAINT fk_deck_card_game_id FOREIGN KEY(game_id) REFERENCES game(id)
);

CREATE TABLE action (
    game_id INTEGER NOT NULL,
    ordinal SMALLINT NOT NULL,
    seat TINYINT NOT NULL,
    action VARCHAR(1024) NOT NULL,
    time BIGINT NOT NULL,
    PRIMARY KEY (game_id, ordinal),
    CONSTRAINT fk_action_game_id FOREIGN KEY(game_id) REFERENCES game(id)
);

CREATE TABLE fcm_token (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_fcm_token_user_id FOREIGN KEY(user_id) REFERENCES user(id)
);

CREATE INDEX ix_fcm_token_user_id ON fcm_token(user_id);

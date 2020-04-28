ALTER TABLE player CHANGE seat ordinal TINYINT;

UPDATE action SET seat = (seat - (SELECT beginner_player FROM game WHERE game.id = action.game_id) + 4) % 4;

UPDATE deck_card SET ordinal = ordinal + 36 * 2 WHERE ordinal < 36;
UPDATE deck_card SET ordinal = (ordinal - 9 * (SELECT beginner_player FROM game WHERE game.id = deck_card.game_id)) % 36 WHERE ordinal >= 42;

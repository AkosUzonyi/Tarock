UPDATE action
SET action = REPLACE(action, "R", "T")
WHERE action LIKE "announce:%";

UPDATE action
SET action = REPLACE(action, "skart:", "fold:")
WHERE action LIKE "skart:%";

UPDATE action
SET action = CONCAT("announce:", SUBSTRING(action, 11), "K", SUBSTRING(action, 10, 1))
WHERE action REGEXP "^announce:[1-7s]";

UPDATE action
SET action = CONCAT("announce:", SUBSTRING(action, 11))
WHERE action LIKE "announce:0%";

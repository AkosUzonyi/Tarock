UPDATE action
SET action = REPLACE(action, "R", "T")
WHERE action LIKE "announce:%";

UPDATE action
SET action = REPLACE(action, "skart:", "fold:")
WHERE action LIKE "skart:%";

UPDATE action
SET action = REPLACE(action, "R", "T")
WHERE action LIKE "announce:%";

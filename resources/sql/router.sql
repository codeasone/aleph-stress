-- :name insert-submission
INSERT INTO submission (id, submitted)
  VALUES (:id, CURRENT_TIMESTAMP);

-- :name complete-submission
UPDATE
  submission
SET
  completed = CURRENT_TIMESTAMP
WHERE
  id = :id;

-- :name submission-complete?
SELECT
  completed
FROM
  submission
WHERE
  id = :id;

BEGIN;

WITH ordered AS (
    SELECT id,
           last_updated,
           lag(last_updated) OVER (ORDER BY last_updated) AS prev_time
    FROM   market_snapshots
)
DELETE FROM market_snapshots ms
    USING  ordered o
WHERE  ms.id = o.id
  AND  o.prev_time IS NOT NULL
  AND  o.last_updated - o.prev_time < INTERVAL '3.5 seconds';

COMMIT;

BEGIN;

ALTER TABLE market_snapshots DROP CONSTRAINT market_snapshots_pkey;
ALTER TABLE market_snapshots ALTER COLUMN id DROP NOT NULL;

UPDATE market_snapshots SET id = NULL;

WITH ordered AS (
    SELECT ctid,
           row_number() OVER (ORDER BY last_updated) AS new_id
    FROM   market_snapshots
)
UPDATE market_snapshots m
SET    id = o.new_id
FROM   ordered o
WHERE  m.ctid = o.ctid;

ALTER TABLE market_snapshots ALTER COLUMN id SET NOT NULL;
ALTER TABLE market_snapshots ADD PRIMARY KEY (id);

SELECT setval('market_snapshots_id_seq',
              (SELECT max(id) FROM market_snapshots),
              true);

COMMIT;
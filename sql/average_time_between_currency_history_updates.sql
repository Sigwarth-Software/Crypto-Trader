SELECT AVG(EXTRACT(EPOCH FROM diff)) AS average_seconds_between_updates
FROM (SELECT last_updated - LAG(last_updated)
                            OVER (PARTITION BY currency_code ORDER BY last_updated) AS diff
      FROM currency_history) subquery
WHERE diff IS NOT NULL;
SELECT
    FLOOR(EXTRACT(EPOCH FROM (NOW() - predictions.last_updated)) / 3600) AS hours_since_polled,
    ABS(AVG(percent_difference)) AS avg_difference
FROM
    predictions
GROUP BY
    hours_since_polled
ORDER BY
    hours_since_polled;

SELECT
    FLOOR(EXTRACT(EPOCH FROM (NOW() - predictions.last_updated)) / 60) AS minutes_since_polled,
    ABS(AVG(percent_difference)) AS avg_difference
FROM
    predictions
GROUP BY
    minutes_since_polled
ORDER BY
    minutes_since_polled;

SELECT
    currency_code,
    AVG(percent_difference) AS avg_difference
FROM
    predictions
WHERE
    percent_difference > 5
GROUP BY
    currency_code
ORDER BY
    avg_difference DESC;

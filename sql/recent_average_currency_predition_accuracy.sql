SELECT ABS(AVG(percent_difference)) as avg_difference, currency_code from predictions
WHERE last_updated > NOW() - INTERVAL '1 hour'
GROUP BY currency_code
ORDER BY avg_difference DESC;
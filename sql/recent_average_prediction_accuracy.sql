SELECT ABS(AVG(percent_difference)) as avg_difference from predictions
WHERE last_updated > NOW() - INTERVAL '1 hour';
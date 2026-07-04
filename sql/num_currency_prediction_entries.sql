SELECT currency_code, COUNT(currency_code) as count
FROM predictions
GROUP BY currency_code
ORDER BY count desc;
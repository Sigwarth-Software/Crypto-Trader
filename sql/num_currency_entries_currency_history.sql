SELECT currency_code, COUNT(currency_code) as count
FROM currency_history
GROUP BY currency_code
ORDER BY count desc;
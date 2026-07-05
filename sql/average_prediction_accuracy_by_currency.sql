SELECT ABS(AVG(percent_difference)) as avg_diff, currency_code
from predictions GROUP BY currency_code
                 order by avg_diff ASC;
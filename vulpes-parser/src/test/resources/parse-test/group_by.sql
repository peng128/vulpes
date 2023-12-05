SELECT
    e,
    b,
    SUM(a) AS t
FROM
    table1
WHERE
    a = d - 1
    AND (
        s > 3
        AND s < 4
    )
    OR g > 5
GROUP BY
    1,
    b
LIMIT
    10
SELECT
    aasd,
    b,
    CASE
        WHEN a = 1 THEN s
    END AS casefunction,
    CAST(d AS VARCHAR) AS t
FROM
    database.table1 AS t1
WHERE
    a = d - 1
    AND (
        s > 3
        AND s < 4
    )
    OR g > 5
ORDER BY
    j
LIMIT
    100
SELECT
    t2.h,
    b,
    CASE
        WHEN a = 1 THEN t1.s
    END AS casefunction,
    CAST(d AS VARCHAR) AS t
FROM
    table1 t1
    JOIN table2 t2 ON t1.id = t2.id
    JOIN table3 t3 ON t2.id = t3.id
WHERE
    a = d - 1
    AND (
        t1.s > 3
        AND t1.s < 4
    )
    OR t2.g > 5
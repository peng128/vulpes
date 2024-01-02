parser grammar SQL92Parser;
//TODO: 还差INTERVAL

options {
tokenVocab = SQL92Lexer;
}

//tokens {
//DELIMITER
//}

query: showSpecification | setSpecification | querySpecification EOF;

showSpecification: showCatalogs | showSchemas | showTables;

showCatalogs: SHOW CATALOG;

showSchemas: SHOW SCHEMA;

showTables: SHOW TABLE;

setSpecification: SET parameterName=IDENTIFIER EQUALS? parameterValue=IDENTIFIER;

introducer: UNDER_SCORE;

//5.2 token and separator
//TODO
//regularIdentifier : identifierBody;

identifierBody : CHARACTERS;//identifierStart (LETTER| DIGIT)*;

//delimitedIdentifier : DOUBLE_QUOTE delimitedIdentifierBody DOUBLE_QUOTE;

//delimitedIdentifierBody: (CHARACTERS | DOUBLE_QUOTE DOUBLE_QUOTE | nonReservedWord | reservedWord)+;

//delimitedIdentifierPart: nonDoubleQuoteCharacter | doubleQuoteSymbol;

//nonDoubleQuoteCharacter: LETTER | DIGIT;

//doubleQuoteSymbol: DOUBLE_QUOTE DOUBLE_QUOTE;

//Names and identifiers
//identifier: CHARACTERS | delimitedIdentifier;

sqlLanguageIdentifier: sqlLanguageIdentifierStart (UNDER_SCORE| sqlLanguageIdentifierPart)*;

sqlLanguageIdentifierStart: LETTER;

sqlLanguageIdentifierPart: LETTER | DIGIT;

//authorizationIdentifier: identifier;

tableName: IDENTIFIER (PERIOD IDENTIFIER)* | qualifiedLocalTableName;

qualifiedLocalTableName: MODULE PERIOD localTableName;

localTableName: qualifiedIdentifier;

domainName: qualifiedName;

schemaName: (catalogName(PERIOD))?unqualifiedSchemaName;

unqualifiedSchemaName: IDENTIFIER;

catalogName: IDENTIFIER;

qualifiedName: (schemaName(PERIOD))? qualifiedIdentifier;

qualifiedIdentifier: IDENTIFIER;

columnName: IDENTIFIER;

correlationName: IDENTIFIER;

moduleName: IDENTIFIER;

cursorName: IDENTIFIER;

procedureName: IDENTIFIER;
///.....
collationName: qualifiedName;
//.....
characterSetName: (schemaName PERIOD)? sqlLanguageIdentifier;

/**

                  <SQL statement name> ::=
                         <statement name>
                       | <extended statement name>

                  <statement name> ::= <identifier>

                  <extended statement name> ::=
                       [ <scope option> ] <simple value specification>

                  <dynamic cursor name> ::=
                         <cursor name>
                       | <extended cursor name>

                  <extended cursor name> ::=
                       [ <scope option> ] <simple value specification>

                  <descriptor name> ::=
                       [ <scope option> ] <simple value specification>

                  <scope option> ::=
                         GLOBAL
                       | LOCAL

                  <parameter name> ::= <colon> <identifier>

                  <constraint name> ::= <qualified name>

                  <collation name> ::= <qualified name>

                  <character set name> ::= [ <schema name> <period> ] <SQL language identifier>


                  <translation name> ::= <qualified name>

                  <form-of-use conversion name> ::= <qualified name>

                  <connection name> ::= <simple value specification>

                  <SQL-server name> ::= <simple value specification>

                  <user name> ::= <simple value specification>
                  */




//table reference
tableReference: tableName (AS? aliasName (LEFT_PAREN derivedColumnList RIGHT_PAREN)?)?
| LEFT_PAREN queryExpression RIGHT_PAREN AS? aliasName (LEFT_PAREN derivedColumnList RIGHT_PAREN)?;

aliasName: IDENTIFIER;

//derivedTable: tableSubquery;

derivedColumnList: columnNameList;

columnNameList: columnReference (COMMA columnReference)*;

//column reference
columnReference: (IDENTIFIER PERIOD)? IDENTIFIER;

parameterReference: AT_SIGNAL AT_SIGNAL columnReference;

qualifier: tableName| correlationName;

function: functionName LEFT_PAREN (valueExpression (COMMA valueExpression)*)? RIGHT_PAREN;

functionName: IDENTIFIER | functionWord;

functionWord: AVG | CAST | MAX | MIN | SUM | TRIM | COUNT ;

//row value constructor
rowValueConstructor: rowValueConstructorElement
| LEFT_PAREN rowValueConstructorList RIGHT_PAREN
|rowSubquery;

rowValueConstructorList: rowValueConstructorElement (COMMA rowValueConstructorElement)*;

rowValueConstructorElement: valueExpression | nullSpecification | defaultSpecification;

nullSpecification: NULL;

defaultSpecification: DEFAULT;

//table value constructor
tableValueConstructor: VALUES tableValueConstructorList;

tableValueConstructorList: rowValueConstructor (COMMA rowValueConstructor)*;

//table expression
tableExpression: fromClause whereClause?;

//from clause
fromClause: FROM queryExpression| joinedTable;

//joined table
joinedTable: tableReference (COMMA tableReference)?
| joinedTable CROSS_JOIN joinedTable
| joinedTable NATURAL? (joinType)? JOIN joinedTable (joinSpecification)?
| LEFT_PAREN joinedTable RIGHT_PAREN;

joinSpecification: joinCondition | namedColumnsJoin;

joinCondition : ON searchCondition;

namedColumnsJoin: USING LEFT_PAREN(joinColumnList)RIGHT_PAREN;

joinType: INNER | outerJoinType (OUTER)?;

outerJoinType: LEFT | RIGHT | FULL;

joinColumnList: columnNameList;

//whereClause
whereClause: WHERE searchCondition;

//group by clause
groupByClause: GROUP_BY groupingColumnReference (COMMA(groupingColumnReference))*;

groupingColumnReference: valueExpression (collateClause)?;

//having clause
havingClause: HAVING searchCondition;

//query specification
querySpecification: SELECT (setQuantifier)? selectList tableExpression? groupByClause? havingClause? orderByClause? limitClause?;

selectList: selectSublist (COMMA selectSublist)*;

selectSublist: valueExpression (AS? IDENTIFIER)? | (IDENTIFIER PERIOD)? ASTERISK;

//predicate
predicate: comparisonPredicate
|betweenPredicate
|inPredicate
|likePredicate
|nullPredicate
|quantifiedComparisonPredicate
|existsPredicate
|uniquePredicate
|matchPredicate
|overlapsPredicate;

//8.2 comparison predicate
comparisonPredicate: rowValueConstructor compOp rowValueConstructor;
compOp: EQUALS | NOT_EQUALS | LESS_THAN| GREATER_THAN | LESS_THAN_OR_EQUALS | GREATER_THAN_OR_EQUALS;

//8.3 between predicate
betweenPredicate: rowValueConstructor NOT? BETWEEN rowValueConstructor AND rowValueConstructor;

//8.4 in predicate
inPredicate: rowValueConstructor NOT? IN inPredicateValue;
inPredicateValue: tableSubquery | LEFT_PAREN inValueList RIGHT_PAREN;
inValueList: valueExpression (COMMA valueExpression);

//8.5 like predicate
likePredicate: matchValue NOT? LIKE pattern (ESCAPE escapeCharacter)?;
matchValue: LITERAL | valueExpression;
pattern: LITERAL | valueExpression;
escapeCharacter: LITERAL | valueExpression;

//8.6 null predicate
nullPredicate: rowValueConstructor IS NOT? NULL;

//8.7 quantified comparison predicate
quantifiedComparisonPredicate: rowValueConstructor compOp (ALL | (SOME | ANY)) tableSubquery;

//8.8 exists predicate
existsPredicate : EXISTS tableSubquery;

//8.9 unique predicate
uniquePredicate: UNIQUE tableSubquery;

//8.10 match predicate
matchPredicate: rowValueConstructor MATCH UNIQUE? (PARTIAL | FULL)? tableSubquery;

//8.11 overlaps predicate
overlapsPredicate: rowValueConstructor OVERLAPS rowValueConstructor;

//8.12 search condition
searchCondition: booleanTerm | searchCondition OR booleanTerm;

booleanTerm: booleanFactor | booleanTerm AND booleanFactor;

booleanFactor: NOT? booleanTest;

booleanTest: booleanPrimary (IS NOT? truthValue)?;

truthValue: TRUE | FALSE | UNKNOWN;

booleanPrimary: predicate | LEFT_PAREN searchCondition RIGHT_PAREN;

//10.5  <collate clause>
collateClause : COLLATE collationName;

//6.9 case expression
//TODO: coalese | nullif
caseExpression: (CASE valueExpression (WHEN valueExpression THEN valueExpression)+ (ELSE valueExpression)? END)
| (CASE (WHEN searchCondition THEN valueExpression)+ (ELSE valueExpression)? END);

//6.10 cast specification
// TODO CHARACTRERS change to data type
castSpecification: CAST LEFT_PAREN (valueExpression | NULL) AS dataType RIGHT_PAREN;

//6.11 value expression - modify
valueExpression : LITERAL | DIGITS
| columnReference
| parameterReference
| function
| castSpecification
| caseExpression
| dateConstant
| intervalExpression
| valueExpression (PLUS | MINUS | ASTERISK | SOLIDUS) valueExpression
| LEFT_PAREN valueExpression RIGHT_PAREN;

dateConstant: DATE LITERAL;

intervalExpression: INTERVAL LITERAL timeunit = (YEAR | MONTH | DAY | HOUR | MINUTE | SECOND);

//TODO
//valueExpressionPrimary: columnReference;
/*
         <value expression primary> ::=
                <unsigned value specification>
              | <column reference>
              | <set function specification>
              | <scalar subquery>
              | <case expression>
              | <left paren> <value expression> <right paren>
              | <cast specification>
              */
//6.12 numeric value expression
numericValueExpression: MINUS? DIGITS
| function
| columnReference
| numericValueExpression PLUS numericValueExpression
| numericValueExpression MINUS numericValueExpression
| numericValueExpression ASTERISK numericValueExpression
| numericValueExpression SOLIDUS numericValueExpression
| LEFT_PAREN numericValueExpression RIGHT_PAREN;

//term: factor|
//term ASTERISK factor|
//term SOLIDUS factor;
//
//factor: SIGN? numericPrimary;
//
//numericPrimary: numericValueFunction;

//6.6 numeric value function
//TODO
numericValueFunction: DIGIT;

//13.1
orderByClause: ORDER BY sortSpecificationList (COMMA sortSpecificationList)* ;

sortSpecificationList: valueExpression collateClause? (ASC|DESC)?;

//other
limitClause: LIMIT DIGITS;

//7.11
scalarSubquery: subquery;

rowSubquery: subquery;

tableSubquery: subquery;

subquery: LEFT_PAREN queryExpression RIGHT_PAREN;

//7.10 query expression
queryExpression:
LEFT_PAREN queryExpression RIGHT_PAREN
| left = queryExpression (UNION|EXCEPT|INTERSECT) (ALL) ? (correspondingSpec)? right = queryExpression
| tableReference
| joinedTable
| simpleTable;

simpleTable: querySpecification
| tableValueConstructor
| explicitTable;

explicitTable: TABLE tableName;

correspondingSpec: CORRESPONDING (BY LEFT_PAREN correspondingColumnList RIGHT_PAREN)?;

correspondingColumnList: columnNameList;

//10.4 character set specification
characterSetSpecification: characterSetName;


//other
setQuantifier : DISTINCT | ALL;

//functionWord: AVG | CAST | MAX | MIN | SUM | TRIM ;
//
//nonReservedWord: ADA | C | CATALOG_NAME | CHARACTER_SET_CATALOG | CHARACTER_SET_NAME
//| CHARACTER_SET_SCHEMA | CLASS_ORIGIN | COBOL | COLLATION_CATALOG
//| COLLATION_NAME | COLLATION_SCHEMA | COLUMN_NAME | COMMAND_FUNCTION | COMMITTED
//| CONDITION_NUMBER | CONNECTION_NAME | CONSTRAINT_CATALOG | CONSTRAINT_NAME
//| CONSTRAINT_SCHEMA | CURSOR_NAME| DATA | DATETIME_INTERVAL_CODE | DATETIME_INTERVAL_PRECISION
//| DYNAMIC_FUNCTION| FORTRAN| LENGTH
//| MESSAGE_LENGTH | MESSAGE_OCTET_LENGTH | MESSAGE_TEXT | MORE
//| MUMPS | NAME | NULLABLE | NUMBER| PASCAL | PLI
//| REPEATABLE | RETURNED_LENGTH | RETURNED_OCTET_LENGTH | RETURNED_SQLSTATE
//| ROW_COUNT| SCALE | SCHEMA_NAME | SERIALIZABLE | SERVER_NAME | SUBCLASS_ORIGIN
//| TABLE_NAME | TYPE| UNCOMMITTED | UNNAMED;
//
//reservedWord:ABSOLUTE | ACTION | ADD | ALL | ALLOCATE | ALTER | AND
//| ANY | ARE | AS | ASC| ASSERTION | AT | AUTHORIZATION | AVG
//| BEGIN | BETWEEN | BIT | BIT_LENGTH | BOTH | BY| CASCADE | CASCADED | CASE | CAST | CATALOG
//| CHAR | CHARACTER | CHAR_LENGTH| CHARACTER_LENGTH | CHECK | CLOSE | COALESCE | COLLATE | COLLATION
//| COLUMN | COMMIT | CONNECT | CONNECTION | CONSTRAINT| CONSTRAINTS | CONTINUE
//| CONVERT | CORRESPONDING | COUNT | CREATE | CROSS | CURRENT
//| CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_USER | CURSOR
//| DATE | DAY | DEALLOCATE | DEC | DECIMAL | DECLARE | DEFAULT | DEFERRABLE
//| DEFERRED | DELETE | DESC | DESCRIBE | DESCRIPTOR | DIAGNOSTICS
//| DISCONNECT | DISTINCT | DOMAIN | DOUBLE | DROP
//| ELSE | END | END_EXEC | ESCAPE | EXCEPT | EXCEPTION
//| EXEC | EXECUTE | EXISTS| EXTERNAL | EXTRACT
//| FALSE | FETCH | FIRST | FLOAT | FOR | FOREIGN | FOUND | FROM | FULL
//| GET | GLOBAL | GO | GOTO | GRANT | GROUP| HAVING | HOUR
//| IDENTITY | IMMEDIATE | IN | INDICATOR | INITIALLY | INNER | INPUT
//| INSENSITIVE | INSERT | INT | INTEGER | INTERSECT | INTERVAL | INTO | IS
//| ISOLATION| JOIN| KEY| LANGUAGE | LAST | LEADING | LEFT | LEVEL | LIKE | LOCAL | LOWER
//| MATCH | MAX | MIN | MINUTE | MODULE | MONTH
//| NAMES | NATIONAL | NATURAL | NCHAR | NEXT | NO | NOT | NULL| NULLIF | NUMERIC
//| OCTET_LENGTH | OF | ON | ONLY | OPEN | OPTION | OR| ORDER | OUTER
//| OUTPUT | OVERLAPS| PAD | PARTIAL | POSITION | PRECISION | PREPARE | PRESERVE | PRIMARY
//| PRIOR | PRIVILEGES | PROCEDURE | PUBLIC
//| READ | REAL | REFERENCES | RELATIVE | RESTRICT | REVOKE | RIGHT
//| ROLLBACK | ROWS| SCHEMA | SCROLL | SECOND | SECTION | SELECT | SESSION | SESSION_USER | SET
//| SIZE | SMALLINT | SOME | SPACE | SQL | SQLCODE | SQLERROR | SQLSTATE
//| SUBSTRING | SUM | SYSTEM_USER| TABLE | TEMPORARY | THEN | TIME | TIMESTAMP | TIMEZONE_HOUR
//| TIMEZONE_MINUTE| TO | TRAILING | TRANSACTION | TRANSLATE | TRANSLATION | TRIM | TRUE
//| UNION | UNIQUE | UNKNOWN | UPDATE | UPPER | USAGE | USER | USING
//| VALUE | VALUES | VARCHAR | VARYING | VIEW
//| WHEN | WHENEVER | WHERE | WITH | WORK | WRITE| YEAR| ZONE;
//TODO characters 不是数据类型
dataType: CHARACTERS | CHARACTER | CHAR | VARCHAR| BIT | VARYING | NATIONAL | NUMERIC | DECIMAL | DEC
| INTEGER | INT | SMALLINT | FLOAT | REAL | DOUBLE | PRECISION | DATE | TIME | TIMESTAMP;

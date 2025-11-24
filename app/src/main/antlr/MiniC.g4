grammar MiniC;

// ----------------------
// Entry point
// ----------------------
program
    : (globalDecl | functionDecl)* EOF
    ;

// ----------------------
// Globals
// ----------------------
globalDecl
    : typeSpec IDENT arraySize? ';'
    ;

// ----------------------
// Functions
// ----------------------
functionDecl
    : typeSpec IDENT '(' paramList? ')' block
    ;

paramList
    : param (',' param)*
    ;

param
    : typeSpec IDENT
    ;

// ----------------------
// Blocks and statements
// ----------------------
block
    : '{' statement* '}'
    ;

statement
    : varDecl
    | assignment
    | expr ';'          // 関数呼び出しや式文
    | ifStmt
    | whileStmt
    | forStmt
    | block
    | returnStmt
    ;

// ----------------------
// Declarations
// ----------------------
varDecl
    : typeSpec IDENT arraySize? ('=' expr)? ';'
    ;

typeSpec
    : baseType pointer?
    ;

baseType
    : VOID
    | INT
    | SHORT
    | CHAR
    ;

pointer
    : '*'
    ;

arraySize
    : '[' INTEGER ']'
    ;

// ----------------------
// Assignments
// ----------------------
assignment
    : lvalue '=' expr ';'
    ;

lvalue
    : IDENT                          #lvVar
    | IDENT '[' expr ']'             #lvArrayElem
    | '*' expr                       #lvPtrDeref
    ;

// ----------------------
// Control flow
// ----------------------
ifStmt
    : 'if' '(' expr ')' statement ('else' statement)?
    ;

whileStmt
    : 'while' '(' expr ')' statement
    ;

forStmt
    : 'for' '(' forInit? ';' expr? ';' forUpdate? ')' statement
    ;

forInit
    : varDecl
    | assignment
    ;

forUpdate
    : assignment
    ;

returnStmt
    : 'return' expr? ';'
    ;
// ----------------------
// Expressions
// ----------------------
expr
    : expr op=('*'|'/') expr             #mulDiv
    | expr op=('+'|'-') expr             #addSub
    | expr op=('<'|'>'|'<='|'>='|'=='|'!=') expr  #compare
    | '(' expr ')'                       #paren
    | '-' expr                           #unaryNeg
    | '&' IDENT                          #addressOf
    | '*' expr                           #deref
    | IDENT '[' expr ']'                 #arrayAccess
    | IDENT '(' (expr (',' expr)*)? ')'  #funcCall
    | IDENT                              #varRef
    | INTEGER                            #intLit
    ;

// ----------------------
// Lexer rules
// ----------------------
IDENT : [a-zA-Z_][a-zA-Z_0-9]* ;
INTEGER : [0-9]+ ;
WS    : [ \t\r\n]+ -> skip ;
VOID  : 'void' ;
CHAR  : 'char' ;
SHORT : 'short' ;
INT   : 'int' ;

# What is this?
A compiler for the subset of C language, which produces CASL2 assembly language.
It is not aimed to be used in practical use, of course.
It is written for the purpose of trying to educate junior engineers who have not understand the basic behavior of C language.
You need a CASL2/COMET environment such as https://www.officedaytime.com/dcaslj/ in order to run the produced CASL2 code.
And this compiler is not thoroughly implemented.
The current implementation covers only the range that produces the code which is needed for me to explain the basic behavior of the stack frame of C language.

# Limitation due to the conformance with CASL2
- Symbol length is limited to 8
- Symbol names are case insensitive
- '_' is not allowed in symbol names.
- 'PROG' or 'prog' is reserved.

# Differences from C
- Local variable declarations are not limited to the beginning of functions.
  However, the scope of them is separated for each function, not for each block.
- There is only int as a type, which is 16bit signed integer, according to the nature of CASL2.
  (However, the range check is not implemented...)
- Of course, it doesn't support struct or union...
- no break statement, no contniue statement,
- Many operators such as % , &&, ... are not implemented yet...
- There are too many differences to enumerate.... Please refer MiniC.g4.

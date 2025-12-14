# What is this?

A compiler for the subset of C language, which produces CASL2 assembly language.
It is not aimed to be used in practical use, of course.
It is written for the purpose of educating junior engineers who have not understand the basic behavior of C language.
You need a CASL2/COMET environment such as https://www.officedaytime.com/dcaslj/ in order to run the produced CASL2 code.
And this complier is not thouroughly implemented.
The current implementation covers only the range that produces the code which is needed for me to explain the basic behavior of the stack frame of C language.

# Limitation due to the conformance with CASL2

- Symbol length is limited to 8
- Symbol names are case insensitive
- '_' is not allowed in symbol names.
- 'PROG' or 'prog' is reserved.

# Differences from C
- Local variable declarations are not limited to the beginning of functions.
  However, the scope of them is separated for each function, not for each block.
  


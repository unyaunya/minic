#include "str.c"

/*
 * Variables for IN/OUT macro
 */
int ilen;
int ibuf[256];
int olen;
int obuf[256];

void puts(int* s) {
    olen = strcpy(obuf, s) - obuf;
    _out();
}

void putn(int n) {
    olen = sputn(obuf, n) - obuf;
    _out();
}
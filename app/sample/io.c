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

int* gets(int* s) {
    _in();
    ibuf[ilen] = '\0';
    strcpy(s, ibuf);
    return s;
}

void putn(int n) {
    olen = sputn(obuf, n) - obuf;
    _out();
}
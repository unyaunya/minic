#include "muldiv.c"

int strlen(int* s) {
    int n = 0;
    while(s[n] != '\0') {
        n = n + 1;
    }
    return n;
}

int* strcpy(int* dst, int* src) {
    int n = 0;
    while(src[n] != '\0') {
        dst[n] = src[n];
        n = n + 1;
    }
    dst[n] = '\0';
    return dst + n;
}

int* sputn(int* dst, int n) {
    int buff[7];
    int idx;
    int sgn;
    int ch;
    int i;
    int d;

    if (n == -32768) {
        return strcpy(dst, "-32768");
    } else if (n == 0) {
        return strcpy(dst, "0");
    } else {
        if(n < 0) {
            sgn = 1;
            dst[0] = '-';
            dst = dst + 1;
            n = -n;
        } else {
            sgn = 0;
        }
        d = 10000;
        int lz = 1;
        for(i = 0; i < 5; i=i+1) {
            ch = div(n, d);
            if (ch != 0) {
                lz = 0;
            }
            if (lz == 0) {
                dst[0] = '0' + ch;
                dst = dst + 1;
            }
            n = n - mul(d, ch);
            d = div(d, 10);
        }
        return dst;
    }
}
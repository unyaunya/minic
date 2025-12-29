#include "muldiv.c"

int atoi(int* s) {
    int n;
    int sign;

    while (*s == ' ' || *s == '\t') {
        s = s + 1;
    }

    sign = 1;
    if (*s == '-') {
        sign = -1;
        s = s + 1;
    } else if (*s == '+') {
        s = s + 1;
    }

    n = 0;
    while (*s >= '0' && *s <= '9') {
        n = mul(n, 10) + (*s - '0');
        s = s + 1;
    }

    return mul(sign, n);
}